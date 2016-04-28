package es.jyago.hermes.fitbit;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.joda.time.LocalDate;

@Singleton
@Startup
public class FitbitScheduledTask implements IFitbitFacade {

    private static final Logger LOG = Logger.getLogger(FitbitScheduledTask.class.getName());

    @Inject
    private PersonFacade personFacade;

    private Person person;

    @PostConstruct
    public void onStartup() {
        LOG.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de sincronización con Fitbit");
    }

    // Las sincronizaciones automáticas con Fitbit se harán cada hora a las XX:05 y a las XX:35.
    // Por otro lado estarán las sincronizaciones para las peticiones restantes 'FitbitRemainsScheduledTask'
    @Schedule(hour = "*", minute = "5/35", persistent = false)
    public void run() {
        Date today = new Date();
        LOG.log(Level.INFO, "run() - Sincronización automática con Fitbit a las {0}", Constants.dfTime.format(today));

        // Se invoca la sincronización de todas las personas registradas.
        for (Person current : personFacade.findAll()) {
            person = current;
            // Comprobamos si tiene credenciales de Fitbit.
            if (person.hasFitbitCredentials()) {
                try {
                    synchronizePerson(person);
                } catch (HermesException | ParseException ex) {
                    LOG.log(Level.SEVERE, "run() - Error al sincronizar automáticamente los datos de Fitbit de la persona " + person.toString(), ex.getMessage());
                }
            }
        }
    }

    private void synchronizePerson(Person person) throws HermesException, ParseException {
        LOG.log(Level.INFO, "synchronizePerson() - Sincronización automática con Fitbit de la persona {0}", person.toString());
        HermesFitbitControllerOauth2 hermesFitbitController = new HermesFitbitControllerOauth2(this);
        // La sincronización se hará hasta el último día de sincronización de la pulsera de Fitbit.
        Date lastFitbitSynchronization = hermesFitbitController.getLastSyncDate();
        if (lastFitbitSynchronization != null) {
            // La fecha de inicio de sincronización será la más antigua entre la última registrada para la persona y la última sincronización de la pulsera de Fitbit.
            // Si la persona no tuviera ninguna sincronización, tomamos la fecha de la última sincronización de Fitbit, como fecha de partida.
            Date personLastSynchronization = person.getLastFitbitSynchronization();
            if (personLastSynchronization == null) {
                personLastSynchronization = new Date(lastFitbitSynchronization.getTime());
            }
            Date startDate = personLastSynchronization.before(lastFitbitSynchronization) ? personLastSynchronization : lastFitbitSynchronization;

            // Obtenemos el número de peticiones restantes.
            int remainingRequests = FitbitResetRequestsScheduledTask.getRemainingRequests(person.getFitbitUserId());

            // Calculamos el número de días que han de sincronizarse.
            LocalDate localStartDate = new LocalDate(startDate);

            int requestsNeeded = hermesFitbitController.getNeededRequestsBetweenDates(startDate, startDate);

            LOG.log(Level.INFO, "synchronizePerson() - Quedan {0} peticiones a Fitbit disponibles. Necesitamos {1} peticiones", new Object[]{remainingRequests, requestsNeeded});

            // Comprobamos si tenemos un número suficiente de peticiones.
            if (remainingRequests < requestsNeeded) {
                // No hay peticiones suficientes para la sincronización completa.
                // Hacemos la petición del máximo posible.
                lastFitbitSynchronization = (localStartDate.plusDays(remainingRequests / Constants.FitbitServices.values().length)).toDate();
            }

            hermesFitbitController.synchronize(startDate, lastFitbitSynchronization);
        }
    }

    @Override
    public Person getPerson() {
        return person;
    }

    @Override
    public void updatePerson() {
        person = personFacade.edit(person);
    }
}
