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
public class FitbitRemainsScheduledTask implements IFitbitFacade {

    private static final Logger LOG = Logger.getLogger(FitbitRemainsScheduledTask.class.getName());

    @Inject
    private PersonFacade personFacade;

    private Person person;

    @PostConstruct
    public void onStartup() {
        LOG.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de sincronización con Fitbit para aprovechar el resto de peticiones antes del reinicio de la cuota");
    }

    // Las sincronizaciones automáticas con Fitbit se harán a las 6:45, 7:15, 7:45, 8:15 y 8:45, es decir, 5 sincronizaciones a partir de las 6:45 cada media hora.
//    @Schedule(hour = "6-8", minute = "45/15")
    @Schedule(hour = "*", minute = "55", persistent = false)
    public void run() {
        Date today = new Date();
        LOG.log(Level.INFO, "run() - Sincronización automática con Fitbit a las {0}, para aprovechar las peticiones no usadas, antes del reinicio de la cuota", Constants.dfTime.format(today));

        // Se invoca la sincronización de todas las personas registradas.
        for (Person current : personFacade.findAll()) {
            person = current;
            // Comprobamos si tiene credenciales de Fitbit.
            if (person.hasFitbitCredentials()) {
                try {
                    synchronizePerson(person);
                } catch (HermesException | ParseException ex) {
                    LOG.log(Level.SEVERE, "run() - Error al sincronizar automáticamente los datos antiguos de Fitbit de la persona " + person.toString(), ex.getMessage());
                }
            }
        }
    }

    private void synchronizePerson(Person person) throws HermesException, ParseException {
        LOG.log(Level.INFO, "synchronizePerson() - Sincronización automática con Fitbit de la persona {0} para recoger datos antiguos", person.toString());
        HermesFitbitControllerOauth2 hermesFitbitController = new HermesFitbitControllerOauth2(this);

        int remainingRequests = FitbitResetRequestsScheduledTask.getRemainingRequests(person.getFitbitUserId());
        // Si quedan peticiones disponibles, nos vamos al pasado y recuperamos más datos de la persona.

        if (remainingRequests > 0) {
            Date firstSynchronization = person.getFirstFitbitSynchronization();
            if (firstSynchronization != null && firstSynchronization.after(Constants.FITBIT_RELEASE_DATE)) {
                LOG.log(Level.INFO, "synchronizePerson() - La sincronización actual más antigua de la persona {0} es {1}", new Object[]{person.toString(), Constants.df.format(firstSynchronization)});
                LocalDate firstSynchronizationLocalDate = new LocalDate(firstSynchronization);
                // Dejamos un margen de días, para no llegar al límite, por si el usuario está realizando operaciones en el momento de una sincronización automática.
                LocalDate pastDate = firstSynchronizationLocalDate.minusDays((remainingRequests / Constants.FitbitServices.values().length) - 5);
                hermesFitbitController.synchronize(pastDate.toDate(), firstSynchronization);
            } else {
                LOG.log(Level.WARNING, "synchronizePerson() - Aún no se tiene constancia de ninguna sincronización de la persona {0}", person.toString());
            }
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
