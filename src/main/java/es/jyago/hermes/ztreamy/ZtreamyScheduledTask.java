/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.ztreamy;

import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.activityLog.ActivityLogHermesZtreamyFacade;
import es.jyago.hermes.healthLog.HealthLog;
import es.jyago.hermes.healthLog.HealthLogHermesZtreamyFacade;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.sleepLog.SleepLogHermesZtreamyFacade;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class ZtreamyScheduledTask {

    private static final Logger log = Logger.getLogger(ZtreamyScheduledTask.class.getName());
    @EJB
    private es.jyago.hermes.person.PersonFacade personFacade;
    @EJB
    private es.jyago.hermes.activityLog.ActivityLogFacade activityLogFacade;
    @EJB
    private es.jyago.hermes.sleepLog.SleepLogFacade sleepLogFacade;
    @EJB
    private es.jyago.hermes.healthLog.HealthLogFacade healthLogFacade;

    @PostConstruct
    public void onStartup() {
        log.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de envío de datos por Ztreamy");
    }

    // Los envíos por Ztreamy se harán a las 7:00, 7:30, 8:00, 8:30 y 9:00, es decir, 5 envíos a partir de las 7:00 cada media hora.
    @Schedule(hour = "7-9", minute = "0/30", persistent = false)
    public void run() {
        // Los parámetros de configuración de Ztreamy estarán en la tabla de configuración.
        String url = Constants.getConfigurationValueByKey("ZtreamyUrl");
        for (Person person : personFacade.findAll()) {
            int totalActivityLog = 0;
            int totalSleepLog = 0;
            int totalHealthLog = 0;
            // Enviaremos los datos de pasos disponibles, que no hayan sido enviados por Ztreamy.
            // La agregación será por minutos, según reunión en JARCA 2015
            List<ActivityLog> activityLogList = person.getActivityLogPendingToSendToZtreamy(Constants.TimeAggregations.Minutes.toString());
            if (activityLogList != null && !activityLogList.isEmpty()) {
                // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
                //                   lista de eventos con un único dato cada uno.
                //                   También se limita el envío a únicamente los que tengan información distinta de cero.

//                    ActivityLogHermesZtreamyFacade activityLogZtreamy = new ActivityLogHermesZtreamyFacade(activityLogList, person);
//                    log.log(Level.INFO, "run() - Envío automático de los datos de pasos de {0} por Ztreamy", person.getFullName());
//                    if (activityLogZtreamy.send()) {
//                        for (ActivityLog activityLog : activityLogList) {
//                            activityLog.setSendDate(new Date());
//                            activityLogFacade.getEntityManager().persist(activityLog);
//                        }
//                    }
                log.log(Level.INFO, "run() - Envío automático de los datos de pasos de {0} por Ztreamy", person.getFullName());
//                for (ActivityLog activityLog : activityLogList) {
//                    if (activityLog.getTotal() > 0) {
//                        try {
//                            ActivityLogHermesZtreamyFacade activityLogZtreamy = new ActivityLogHermesZtreamyFacade(activityLog, person, url);
//                            if (activityLogZtreamy.send()) {
//                                log.log(Level.INFO, "run() - Datos de pasos del día {0} enviados correctamente", Constants.df.format(activityLog.getDateLog()));
//                                activityLog.setSendDate(new Date());
//                                activityLogFacade.getEntityManager().persist(activityLog);
//                                totalActivityLog++;
//                            }
//                        } catch (HermesException | IOException ex) {
//                            log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de pasos de " + person.toString(), ex);
//                        }
//                    } else {
//                        // No contiene información útil para el envío.
//                        // Le asignamos como fecha de envío (1/1/1970) para que no vuelvan a ser considerados para el envío.
//                        log.log(Level.INFO, "run() - Datos de pasos del día {0} NO APTOS para el envío", Constants.df.format(activityLog.getDateLog()));
//                        activityLog.setSendDate(new Date(0));
//                        activityLogFacade.getEntityManager().persist(activityLog);
//                    }
//                }

                List<ActivityLog> filteredActivityLogList = new ArrayList<>();

                for (ActivityLog activityLog : activityLogList) {
                    if (activityLog.getTotal() > 0) {
                        filteredActivityLogList.add(activityLog);
                    } else {
                        // No contiene información útil para el envío.
                        // Le asignamos como fecha de envío (1/1/1970) para que no vuelvan a ser considerados para el envío.
                        log.log(Level.INFO, "run() - Datos de pasos del día {0} NO APTOS para el envío", Constants.df.format(activityLog.getDateLog()));
                        activityLog.setSendDate(new Date(0));
                        activityLogFacade.getEntityManager().persist(activityLog);
                    }
                }

                try {
                    ActivityLogHermesZtreamyFacade activityLogZtreamy = new ActivityLogHermesZtreamyFacade(filteredActivityLogList, person, url);
                    if (activityLogZtreamy.send()) {
                        for (ActivityLog activityLog : filteredActivityLogList) {
                            log.log(Level.INFO, "run() - Datos de pasos del día {0} enviados correctamente", Constants.df.format(activityLog.getDateLog()));
                            activityLog.setSendDate(new Date());
                            activityLogFacade.getEntityManager().persist(activityLog);
                            totalActivityLog++;
                        }
                    }
                } catch (HermesException | IOException ex) {
                    log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de pasos de " + person.toString(), ex);
                }

                log.log(Level.INFO, "run() - Se han enviado {0} datos de pasos de la persona {1}", new Object[]{totalActivityLog, person.getFullName()});
            }

            // Enviaremos los datos de sueño pendientes de enviar.
            List<SleepLog> sleepLogList = person.getSleepLogPendingToSendToZtreamy();
            if (sleepLogList != null && !sleepLogList.isEmpty()) {
                // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
                //                   lista de eventos con un único dato cada uno.
                //                   También se limita el envío a únicamente los que tengan información distinta de cero.

//                    SleepLogHermesZtreamyFacade sleepLogZtreamy = new SleepLogHermesZtreamyFacade(sleepLogList, person);
//                    log.log(Level.INFO, "run() - Envío automático de los datos de sueño de {0} por Ztreamy", person.getFullName());
//                    if (sleepLogZtreamy.send()) {
//                        for (SleepLog sleepLog : sleepLogList) {
//                            sleepLog.setSendDate(new Date());
//                            sleepLogFacade.getEntityManager().persist(sleepLog);
//                        }
//                    }
                log.log(Level.INFO, "run() - Envío automático de los datos de sueño de {0} por Ztreamy", person.getFullName());
//                for (SleepLog sleepLog : sleepLogList) {
//                    if (sleepLog.getMinutesInBed() > 0) {
//                        try {
//                            SleepLogHermesZtreamyFacade sleepLogZtreamy = new SleepLogHermesZtreamyFacade(sleepLog, person, url);
//                            if (sleepLogZtreamy.send()) {
//                                log.log(Level.INFO, "run() - Datos de sueño del día {0} enviados correctamente", Constants.df.format(sleepLog.getDateLog()));
//                                sleepLog.setSendDate(new Date());
//                                sleepLogFacade.getEntityManager().persist(sleepLog);
//                                totalSleepLog++;
//                            }
//                        } catch (HermesException | IOException ex) {
//                            log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de sueño de " + person.toString(), ex);
//                        }
//                    } else {
//                        // No contiene información útil para el envío.
//                        // Le asignamos como fecha de envío (1/1/1970) para que no vuelvan a ser considerados para el envío.
//                        log.log(Level.INFO, "run() - Datos de sueño del día {0} NO APTOS para el envío", Constants.df.format(sleepLog.getDateLog()));
//                        sleepLog.setSendDate(new Date(0));
//                        sleepLogFacade.getEntityManager().persist(sleepLog);
//                    }
//                }

                List<SleepLog> filteredSleepLogList = new ArrayList<>();

                for (SleepLog sleepLog : sleepLogList) {
                    if (sleepLog.getMinutesInBed() > 0) {
                        filteredSleepLogList.add(sleepLog);
                    } else {
                        // No contiene información útil para el envío.
                        // Le asignamos como fecha de envío (1/1/1970) para que no vuelvan a ser considerados para el envío.
                        log.log(Level.INFO, "run() - Datos de sueño del día {0} NO APTOS para el envío", Constants.df.format(sleepLog.getDateLog()));
                        sleepLog.setSendDate(new Date(0));
                        sleepLogFacade.getEntityManager().persist(sleepLog);
                    }
                }

                try {
                    SleepLogHermesZtreamyFacade sleepLogZtreamy = new SleepLogHermesZtreamyFacade(filteredSleepLogList, person, url);
                    if (sleepLogZtreamy.send()) {
                        for (SleepLog sleepLog : filteredSleepLogList) {
                            log.log(Level.INFO, "run() - Datos de sueño del día {0} enviados correctamente", Constants.df.format(sleepLog.getDateLog()));
                            sleepLog.setSendDate(new Date());
                            sleepLogFacade.getEntityManager().persist(sleepLog);
                            totalSleepLog++;
                        }
                    }
                } catch (HermesException | IOException ex) {
                    log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de sueño de " + person.toString(), ex);
                }

                log.log(Level.INFO, "run() - Se han enviado {0} datos de sueño de la persona {1}", new Object[]{totalSleepLog, person.getFullName()});
            }

            // Enviaremos los datos de ritmo cardíaco disponibles, que no hayan sido enviados por Ztreamy.
            // La agregación será por minutos, según reunión en JARCA 2015
            List<HealthLog> healthLogList = person.getHealthLogPendingToSendToZtreamy(Constants.TimeAggregations.Minutes.toString());
            if (healthLogList != null && !healthLogList.isEmpty()) {
                // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
                //                   lista de eventos con un único dato cada uno.
                //                   También se limita el envío a únicamente los que tengan información distinta de cero.

//                    HealthLogHermesZtreamyFacade healthLogZtreamy = new HealthLogHermesZtreamyFacade(healthLogList, person);
//                    log.log(Level.INFO, "run() - Envío automático de los datos de ritmo cardíaco de {0} por Ztreamy", person.getFullName());
//                    if (healthLogZtreamy.send()) {
//                        for (HealthLog healthLog : healthLogList) {
//                            healthLog.setSendDate(new Date());
//                            healthLogFacade.getEntityManager().persist(healthLog);
//                        }
//                    }
                log.log(Level.INFO, "run() - Envío automático de los datos de ritmo cardíaco de {0} por Ztreamy", person.getFullName());
//                for (HealthLog healthLog : healthLogList) {
//                    if (healthLog.getAverage() > 0) {
//                        try {
//                            HealthLogHermesZtreamyFacade healthLogZtreamy = new HealthLogHermesZtreamyFacade(healthLog, person, url);
//                            if (healthLogZtreamy.send()) {
//                                log.log(Level.INFO, "run() - Datos de ritmo cardíaco del día {0} enviados correctamente", Constants.df.format(healthLog.getDateLog()));
//                                healthLog.setSendDate(new Date());
//                                healthLogFacade.getEntityManager().persist(healthLog);
//                                totalHealthLog++;
//                            }
//                        } catch (HermesException | IOException ex) {
//                            log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de ritmo cardíaco de " + person.toString(), ex);
//                        }
//                    } else {
//                        // No contiene información útil para el envío.
//                        // Le asignamos como fecha de envío (1/1/1970) para que no vuelvan a ser considerados para el envío.
//                        log.log(Level.INFO, "run() - Datos de ritmo cardíaco del día {0} NO APTOS para el envío", Constants.df.format(healthLog.getDateLog()));
//                        healthLog.setSendDate(new Date(0));
//                        healthLogFacade.getEntityManager().persist(healthLog);
//                    }
//                }
                List<HealthLog> filteredHealthLogList = new ArrayList<>();

                for (HealthLog healthLog : healthLogList) {
                    if (healthLog.getAverage() > 0) {
                        filteredHealthLogList.add(healthLog);
                    } else {
                        // No contiene información útil para el envío.
                        // Le asignamos como fecha de envío (1/1/1970) para que no vuelvan a ser considerados para el envío.
                        log.log(Level.INFO, "run() - Datos de ritmo cardíaco del día {0} NO APTOS para el envío", Constants.df.format(healthLog.getDateLog()));
                        healthLog.setSendDate(new Date(0));
                        healthLogFacade.getEntityManager().persist(healthLog);
                    }
                }

                try {
                    HealthLogHermesZtreamyFacade healthLogZtreamy = new HealthLogHermesZtreamyFacade(filteredHealthLogList, person, url);
                    if (healthLogZtreamy.send()) {
                        for (HealthLog healthLog : filteredHealthLogList) {
                            log.log(Level.INFO, "run() - Datos de ritmo cardíaco del día {0} enviados correctamente", Constants.df.format(healthLog.getDateLog()));
                            healthLog.setSendDate(new Date());
                            healthLogFacade.getEntityManager().persist(healthLog);
                            totalHealthLog++;
                        }
                    }
                } catch (HermesException | IOException ex) {
                    log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de ritmo cardíaco de " + person.toString(), ex);
                }

                log.log(Level.INFO, "run() - Se han enviado {0} datos de ritmo cardíaco de la persona {1}", new Object[]{totalHealthLog, person.getFullName()});
            }

        }
    }

    // 
    // Con esta función sólo sincronizaría lo del día anterior, pero si no hubiera datos, no se enviarían nunca. Es decir, requiere que el usuario sea muy activo sincronizando con Fitbit.
    // Con la función de arriba, se enviarán todos los datos no enviados hasta ayer, con lo que siempre se acabarán enviando los datos, aunque sean antiguos.
    //
//    public void run() {
//        for (Person person : personFacade.findAll()) {
//            // Comprobamos si tiene credenciales de Fitbit.
//            if (person.hasFitbitCredentials()) {
//                try {
//                    // Enviamos todos los datos del día actual por Ztreamy.
//                    LocalDate today = new LocalDate();
//                    LocalDate yesterday = today.minusDays(1);
//
//                    // Enviaremos los datos de pasos de ayer, ya que los de hoy, al enviarse de madrugada, no tiene sentido.
//                    ActivityLog activityLog = person.getActivityLog(yesterday.toDate(), Constants.TimeAggregations.Hours.toString());
//                    ActivityLogHermesZtreamyFacade activityLogZtreamy = new ActivityLogHermesZtreamyFacade(activityLog, person);
//                    log.log(Level.INFO, "run() - Envío automático de los datos de pasos de {0} del día {1} por Ztreamy", new Object[]{person.getFullName(), Constants.dfTime.format(yesterday.toDate())});
//                    if (activityLogZtreamy.send()) {
//                        activityLog.setSendDate(new Date());
//                        activityLogFacade.getEntityManager().persist(activityLog);
//                    }
//
//                    // Enviaremos los datos de sueño de hoy.
//                    SleepLog sleepLog = person.getSleepLog(today.toDate());
//                    SleepLogHermesZtreamyFacade sleepLogZtreamy = new SleepLogHermesZtreamyFacade(sleepLog, person);
//                    log.log(Level.INFO, "run() - Envío automático de los datos de sueño de {0} del día {1} por Ztreamy", new Object[]{person.getFullName(), Constants.dfTime.format(today.toDate())});
//                    if (sleepLogZtreamy.send()) {
//                        sleepLog.setSendDate(new Date());
//                        sleepLogFacade.getEntityManager().persist(sleepLog);
//                    }
//
//                    // Enviaremos los datos de ritmo cardíaco de ayer, ya que los de hoy, al enviarse de madrugada, no tiene sentido.
//                    HealthLog healthLog = person.getHealthLog(yesterday.toDate(), Constants.TimeAggregations.Hours.toString());
//                    HealthLogHermesZtreamyFacade healthLogZtreamy = new HealthLogHermesZtreamyFacade(healthLog, person);
//                    log.log(Level.INFO, "run() - Envío automático de los datos de ritmo cardíaco de {0} del día {1} por Ztreamy", new Object[]{person.getFullName(), Constants.dfTime.format(yesterday.toDate())});
//                    if (healthLogZtreamy.send()) {
//                        healthLog.setSendDate(new Date());
//                        healthLogFacade.getEntityManager().persist(healthLog);
//                    }
//                } catch (HermesException ex) {
//                    log.log(Level.WARNING, "run() - No se enviarán automáticamente por Ztreamy los datos de {0}", person.toString());
//                } catch (IOException ex) {
//                    log.log(Level.SEVERE, "run() - Error al enviar por Ztreamy automáticamente los datos de " + person.toString(), ex);
//                }
//            }
//        }
//    }
}
