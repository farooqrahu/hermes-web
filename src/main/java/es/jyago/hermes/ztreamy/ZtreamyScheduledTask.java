package es.jyago.hermes.ztreamy;

import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.activityLog.ActivityLogHermesZtreamyFacade;
import es.jyago.hermes.healthLog.HealthLog;
import es.jyago.hermes.healthLog.HealthLogHermesZtreamyFacade;
import es.jyago.hermes.heartLog.HeartLog;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.sleepLog.SleepLogHermesZtreamyFacade;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class ZtreamyScheduledTask {

    private static final Logger LOG = Logger.getLogger(ZtreamyScheduledTask.class.getName());

    @Inject
    private es.jyago.hermes.activityLog.ActivityLogFacade activityLogFacade;
    @Inject
    private es.jyago.hermes.sleepLog.SleepLogFacade sleepLogFacade;
    @Inject
    private es.jyago.hermes.healthLog.HealthLogFacade healthLogFacade;
    // JYFR - Inicio
    // 18-04-2016: Los datos de contexto se enviarán mediante la aplicación de SmartCitizen.
//    @Inject
//    private es.jyago.hermes.contextLog.ContextLogFacade contextLogFacade;
    // JYFR - Fin

    @PostConstruct
    public void onStartup() {
        LOG.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de envío de datos por Ztreamy");
    }

// Los envíos por Ztreamy se harán a las 6:00, 6:30, 7:00, 7:30, 8:00, 8:30 y 9:00, es decir, 5 envíos a partir de las 7:00 cada media hora.
//    @Schedule(hour = "6-9", minute = "0/30", persistent = false)
    // Los envíos por Ztreamy se harán cada 15 minutos.
    @Schedule(minute = "*/15", hour = "*", persistent = false)
    public void run() {

        // Los parámetros de configuración de Ztreamy estarán en la tabla de configuración.
        String url = Constants.getInstance().getConfigurationValueByKey("ZtreamyUrl");
        if (url == null || url.equals("")) {
            // Si no hubiera ninguna URL asignada, ponemos por defecto la de Madrid.
            url = "http://hermes1.gast.it.uc3m.es:9100/collector/publish";
        }

        LOG.log(Level.INFO, "run() - Envío de datos por Ztreamy: {0}", url);
        // Enviaremos los datos de pasos disponibles, que no hayan sido enviados por Ztreamy.
        // La agregación será por minutos, según reunión en JARCA 2015
        // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
        //                   lista de eventos con un único dato cada uno.
        // JYFR: 26-02-2016: Se adaptan los envíos por Ztreamy de datos de Fitbit y de aplicación Android.

        ///////////
        // PASOS //
        ///////////
        for (ActivityLog al : activityLogFacade.findNotSent()) {
            if (!al.isSent()) {
                try {
                    ActivityLogHermesZtreamyFacade activityLogZtreamy = new ActivityLogHermesZtreamyFacade(al, al.getPerson(), url);
                    if (activityLogZtreamy.send()) {
                        LOG.log(Level.INFO, "run() - Datos de pasos del día {0} enviados correctamente", Constants.df.format(al.getDateLog()));
                        for (StepLog stepLog : al.getStepLogList()) {
                            stepLog.setSent(true);
                        }
                        al.setSent(true);
                        activityLogFacade.getEntityManager().persist(al);
                    }
                } catch (HermesException | IOException ex) {
                    LOG.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de pasos de " + al.getPerson().toString(), ex);
                }
            }
        }

        ///////////
        // SUEÑO //
        ///////////
        for (SleepLog sl : sleepLogFacade.findNotSent()) {
            try {
                SleepLogHermesZtreamyFacade sleepLogZtreamy = new SleepLogHermesZtreamyFacade(sl, sl.getPerson(), url);
                if (sleepLogZtreamy.send()) {
                    LOG.log(Level.INFO, "run() - Datos de sueño del día {0} enviados correctamente", Constants.df.format(sl.getDateLog()));
                    sl.setSent(true);
                    sleepLogFacade.getEntityManager().persist(sl);
                }
            } catch (HermesException | IOException ex) {
                LOG.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de sueño de " + sl.getPerson().toString(), ex);
            }
        }

        ////////////////////
        // RITMO CARDIACO //
        ////////////////////
        for (HealthLog hl : healthLogFacade.findNotSent()) {
            try {
                HealthLogHermesZtreamyFacade healthLogZtreamy = new HealthLogHermesZtreamyFacade(hl, hl.getPerson(), url);
                if (healthLogZtreamy.send()) {
                    LOG.log(Level.INFO, "run() - Datos de ritmo cardíaco del día {0} enviados correctamente", Constants.df.format(hl.getDateLog()));
                    for (HeartLog heartLog : hl.getHeartLogList()) {
                        heartLog.setSent(true);
                    }
                    hl.setSent(true);
                    healthLogFacade.getEntityManager().persist(hl);
                }
            } catch (HermesException | IOException ex) {
                LOG.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de ritmo cardíaco de " + hl.getPerson().toString(), ex);
            }
        }

        // JYFR - Inicio
        // 18-04-2016: Los datos de contexto se enviarán mediante la aplicación de SmartCitizen.
//        //////////////
//        // CONTEXTO //
//        //////////////
//        for (ContextLog cl : contextLogFacade.findNotSent()) {
//            try {
//                ContextLogHermesZtreamyFacade contextLogZtreamy = new ContextLogHermesZtreamyFacade(cl, cl.getPerson(), url);
//                if (contextLogZtreamy.send()) {
//                    LOG.log(Level.INFO, "run() - Datos de contexto del día {0} enviados correctamente", Constants.df.format(cl.getDateLog()));
//                    for (ContextLogDetail cld : cl.getContextLogDetailList()) {
//                        cld.setSent(true);
//                    }
//                    cl.setSent(true);
//                    contextLogFacade.getEntityManager().persist(cl);
//                }
//            } catch (HermesException | IOException ex) {
//                LOG.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de contexto de " + cl.getPerson().toString(), ex);
//            }
//        }
        // JYFR- Fin
    }
}
