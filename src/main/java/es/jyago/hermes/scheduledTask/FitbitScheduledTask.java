/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.scheduledTask;

import com.fitbit.api.common.model.timeseries.IntradayData;
import com.fitbit.api.common.model.timeseries.IntradaySummary;
import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.fitbit.HermesFitbitController;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.joda.time.Days;
import org.joda.time.LocalDate;

@Singleton
@Startup
public class FitbitScheduledTask {

    private static final Logger log = Logger.getLogger(FitbitScheduledTask.class.getName());
    @EJB
    private es.jyago.hermes.person.PersonFacade ejbFacade;

    private HermesFitbitController hermesFitbitController;

    // Periodo de sucesivas repeticiones.
    private static final long HOURLY = 3600000;
    // Temporizador para la sincronización horaria, en caso de que no se disponga de peticiones suficientes con la sincronización programada.
    private static Timer hourlyTimer;
    // Bandera para indicar si se debe aplazar la sincronización. Se ejecutará una tarea programada para intentar la sincronización posteriormente.
    private static boolean putOff;

    @PostConstruct
    public void onStartup() {
        log.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de sincronización con Fitbit");
    }

    // La hora de sincronización automática será las 08:00.
    @Schedule(hour = "8", minute = "0")
    public void run() {
        // Se invoca la sincronización de todas las personas registradas.
        synchronize();
        if (putOff) {
            // Obtenemos el tiempo de espera hasta el siguiente intento de sincronización, cuando no se ha podido completar la tarea de sincronización principal.
            long waitTime;
            try {
                waitTime = hermesFitbitController.getWaitTime();
            } catch (HermesException ex) {
                log.log(Level.SEVERE, "run() - Error al obtener el tiempo de espera hasta el siguiente intento de sincronización", ex);
                // Si no se pudiera obtener el tiempo de espera de Fitbit, calculamos el tiempo hasta la siguiente hora,
                // ya que Fitbit, según la documentación, renueva el cupo de peticiones cada hora.
                Calendar tillOClock = Calendar.getInstance();
                int minutes = 60 - tillOClock.get(Calendar.MINUTE);
                waitTime = minutes * 60000;

                log.log(Level.INFO, "run() - Sincronización con Fitbit aplazada hasta dentro de {0} minutos", minutes);
            }
            // Hasta que la sincronización esté completa, se crea una tarea programada que se repetirá cada hora
            // (es el intervalo de renovación de peticiones de Fitbit) para seguir solicitando los datos.
            hourlyTimer = new Timer();
            ScheduledTask st = new ScheduledTask();
            hourlyTimer.schedule(st, waitTime, HOURLY);
        }
    }

    private void synchronize() {
        // Inicialmente, indicamos que no será necesario aplazar la sincronización.
        putOff = false;

        for (Person person : ejbFacade.findAll()) {
            // Comprobamos si tiene credenciales de Fitbit.
            if (person.hasFitbitCredentials()) {
                try {
                    if (!synchronizePerson(person)) {
                        putOff = true;
                        break;
                    }
                } catch (HermesException | ParseException ex) {
                    log.log(Level.SEVERE, "synchronizePerson() - Error al sincronizar automáticamente los datos de Fitbit de " + person.toString(), ex);
                    // En este caso no establecemos un aplazamiento, porque no es un caso de sobrepasar los límites de peticiones permitidas por Fitbit.
                }
            }
        }
    }

    private boolean synchronizePerson(Person person) throws HermesException, ParseException {
        log.log(Level.INFO, "synchronizePerson() - Sincronización automática con Fitbit de la persona {0}", person.toString());
        hermesFitbitController = new HermesFitbitController(person);
        // La sincronización se hará hasta el último día de sincronización de la pulsera de Fitbit.
        Date lastFitbitSynchronization = hermesFitbitController.getLastSyncDate();
        // La fecha de inicio de sincronización será la más antigua entre la última registrada para la persona
        // y la última sincronización de la pulsera de Fitbit.
        // Si la persona no tuviera ninguna sincronización, tomamos como fecha de partida el primer día del año actual.
        Date personLastSynchronization = person.getLastSynchronization();
        if (personLastSynchronization == null) {
            Calendar firstDayOfYear = Calendar.getInstance();
            firstDayOfYear.set(Calendar.MONTH, 0);
            firstDayOfYear.set(Calendar.DAY_OF_MONTH, 1);
            personLastSynchronization = firstDayOfYear.getTime();
        }
        Date startDate = personLastSynchronization.before(lastFitbitSynchronization) ? personLastSynchronization : lastFitbitSynchronization;

        boolean personCompleted = true;

        // Obtenemos el número de peticiones restantes.
        int remainingRequests = hermesFitbitController.getRemainingRequests();

        // Calculamos el número de días que han de sincronizarse.
        LocalDate localStartDate = new LocalDate(startDate);
        LocalDate localEndDate = new LocalDate(lastFitbitSynchronization);

        int days = Days.daysBetween(localStartDate, localEndDate).getDays();

        if (remainingRequests < days) {
            // No hay peticiones suficientes para la sincronización completa.
            personCompleted = false;
            // Hacemos la petición del máximo posible.
            lastFitbitSynchronization = (localStartDate.plusDays(remainingRequests)).toDate();
        }

        log.log(Level.INFO, "synchronizePerson() - Obteniendo datos desde {0} hasta {1}", new Object[]{Constants.dfTime.format(startDate), Constants.dfTime.format(lastFitbitSynchronization)});

        // Hacemos la petición de sincronización teniendo en cuenta el número de peticiones restantes.
        List<IntradaySummary> listIntradaySummary = hermesFitbitController.getIntradayData(startDate, lastFitbitSynchronization);

        HashSet<ActivityLog> hashSetActivityLog = new HashSet<>(person.getActivityLogCollection());
        for (IntradaySummary intradaySummary : listIntradaySummary) {
            ActivityLog activityLog = new ActivityLog();

            activityLog.setDate(Constants.dfFitbit.parse(intradaySummary.getSummary().getDateTime()));
            activityLog.setStepLogCollection(new ArrayList());
            activityLog.setPerson(person);

            for (IntradayData intradayData : intradaySummary.getIntradayDataset().getDataset()) {
                StepLog stepLog = new StepLog();
                stepLog.setActivityLog(activityLog);
                stepLog.setTimeLog(Constants.dfTime.parse(intradayData.getTime()));
                stepLog.setSteps((int) intradayData.getValue());
                activityLog.getStepLogCollection().add(stepLog);
            }
            activityLog.calculateTotal();
            hashSetActivityLog.remove(activityLog);
            hashSetActivityLog.add(activityLog);
        }
        person.getActivityLogCollection().clear();
        // JYFR - PRUEBA
//        updatePerson(person);
        ejbFacade.getEntityManager().persist(person);
        ejbFacade.getEntityManager().flush();
        person.getActivityLogCollection().addAll(hashSetActivityLog);
        updatePerson(person);

        return personCompleted;
    }

    private void updatePerson(Person person) {
        if (person != null) {
            ejbFacade.edit(person);
            // FIXME: Ver si puedo quitar el 'flush' y ponerlo como el de 'PersonController' ¿Ver si tienen diferente getEntityManager().getFlushMode()?
            ejbFacade.getEntityManager().flush();
        }
    }

    private boolean isPutOff() {
        return putOff;
    }

    private class ScheduledTask extends TimerTask {

        @Override
        public void run() {
            synchronize();

            if (!isPutOff()) {
                // La sincronización se ha completado.
                // Terminamos el temporizador.
                FitbitScheduledTask.hourlyTimer.cancel();
                FitbitScheduledTask.hourlyTimer.purge();
            }
        }
    }
}
