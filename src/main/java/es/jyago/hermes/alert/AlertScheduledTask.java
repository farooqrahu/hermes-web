package es.jyago.hermes.alert;

import es.jyago.hermes.bean.LocaleBean;
import es.jyago.hermes.email.Email;
import es.jyago.hermes.fitbit.HermesFitbitControllerOauth2;
import es.jyago.hermes.fitbit.HermesFitbitException;
import es.jyago.hermes.fitbit.IFitbitFacade;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.mail.MessagingException;
import org.joda.time.LocalTime;

@Singleton
@Startup
public class AlertScheduledTask implements IFitbitFacade {

    private static final Logger LOG = Logger.getLogger(AlertScheduledTask.class.getName());
    @Inject
    private PersonFacade personFacade;

    private Person person;

    @PostConstruct
    public void onStartup() {
        LOG.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de comprobación de alertas");
    }

    // Las comprobaciones de las condiciones de las alertas se harán cada hora,
    // para poder notificar a cada person a la hora que haya establecido.
    @Schedule(hour = "*", minute = "0", persistent = false)
    public void run() {
        LOG.log(Level.INFO, "run() - Comprobación de las alertas");

        for (Person person : personFacade.findAll()) {
            this.person = person;
            if (isNotificationTime()) {
                HermesFitbitControllerOauth2 hermesFitbitController = new HermesFitbitControllerOauth2(this);
                try {
                    // Comprobamos si se puede sincronizar correctamente.
                    hermesFitbitController.refreshFitbitTokens();
                } catch (HermesFitbitException ex) {
                    LOG.log(Level.SEVERE, "run() - Error al comprobar si se puede sincronizar los datos con Fitbit: {0}", ex.getMessage());
                    if (person.isAlertIfUnableToSynchronize()) {
                        try {
                            // No es posible sincronizar con Fitbit -> Se envía una notificación., si así lo tiene configurado el usuario.
                            LOG.log(Level.FINE, "run() - Se notifica por e-mail que no ha sido posible la sincronización");
                            Email.generateAndSendEmail(person.getEmail(), LocaleBean.getBundle().getString("Fitbit.error.noAuthorization"), LocaleBean.getBundle().getString("EmailUnableToSynchronizeNotificationText"));
                        } catch (MessagingException e) {
                            LOG.log(Level.SEVERE, "run() - Error al enviar el e-mail de la alerta", e);
                        }
                    } else {
                        LOG.log(Level.FINE, "run() - No se notifica que no ha sido posible la sincronización, porque tiene desactivados los avisos");
                    }
                }

                // Comprobamos si la persona tiene alertas definidas.
                if (person.getAlertList() != null) {
                    // Se analizan todas las alertas registradas.
                    for (Alert alert : person.getAlertList()) {
                        // Comprobamos las condiciones de activación de la alerta.
                        if (checkConditions(alert)) {
                            try {
                                // Se cumplen las condiciones de la alerta -> Se envía la notificación.
                                Email.generateAndSendEmail(person.getEmail(), alert.getName(), LocaleBean.getBundle().getString("EmailNotificationText"));
                            } catch (MessagingException ex) {
                                LOG.log(Level.SEVERE, "run() - Error al enviar el e-mail de la alerta", ex);
                            }
                        }
                    }
                }
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

    private boolean checkConditions(Alert alert) {
        // Comprobamos si está activa, si el usuario tiene e-mail y si se verifican las condiciones de disparo.
        return alert.getActive()
                && person.getEmail() != null
                && person.getEmail().length() > 0
                && alert.checkTrigger();
    }

    private boolean isNotificationTime() {
        LocalTime now = new LocalTime();
        LocalTime alertNotificationTime = person.getAlertNotificationsTime() != null ? new LocalTime(person.getAlertNotificationsTime()) : null;

        // Comprobamos si tiene definida una hora de notificaciones y si ya es la hora de notificar.
        return alertNotificationTime != null && alertNotificationTime.getHourOfDay() == now.getHourOfDay();
    }
}
