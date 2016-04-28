package es.jyago.hermes.fitbit;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.FitbitApiClientAgentOauth2;
import com.fitbit.api.common.model.body.DataPeriod;
import com.fitbit.api.common.model.devices.Device;
import com.fitbit.api.common.model.timeseries.IntradayData;
import com.fitbit.api.common.model.timeseries.IntradaySummary;
import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import com.fitbit.api.common.model.user.UserInfo;
import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.healthLog.HealthLog;
import es.jyago.hermes.heartLog.HeartLog;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.util.ImageUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;


public class HermesFitbitControllerOauth2 {

    private static final String[] scopes = new String[]{
        "activity",
        "nutrition",
        "heartrate",
        "location",
        "nutrition",
        "profile",
        "settings",
        "sleep",
        "social",
        "weight"};

    // TODO: Completar métodos, añadir comentarios y JavaDoc.
    private static final Logger LOG = Logger.getLogger(HermesFitbitControllerOauth2.class.getName());

    public static final String OAUTH2_CODE = "code";
    private static final int TOO_MANY_REQUESTS = 429;

    private final IFitbitFacade fitbitFacade;
    private final FitbitApiClientAgentOauth2 apiClientAgent;

    public HermesFitbitControllerOauth2(IFitbitFacade fitbitFacade) {
        this.fitbitFacade = fitbitFacade;
        apiClientAgent = new FitbitApiClientAgentOauth2(fitbitFacade.getPerson().getFitbitUserId(),
                fitbitFacade.getPerson().getFitbitAccessToken(),
                fitbitFacade.getPerson().getFitbitRefreshToken(),
                Constants.getInstance().getConfigurationValueByKey("FitbitOauth2ClientId"),
                Constants.getInstance().getConfigurationValueByKey("FitbitClientConsumerSecret"));
    }

    public void refreshFitbitTokens() throws HermesFitbitException {
        try {
            LOG.log(Level.INFO, "refreshTokens() - Refrescar el token de acceso de Fitbit");
            apiClientAgent.refreshToken();
            // FIXME: Por seguridad, para que quede en el log. Quitar o poner con Level FINE o CONFIG más adelante.
            LOG.log(Level.INFO, "refreshTokens() - Tokens refrescados:\nAccess Token....: {0}\nRefresh Token...: {1}", new Object[]{apiClientAgent.getTokens().getAccessToken(), apiClientAgent.getTokens().getRefreshToken()});
            fitbitFacade.getPerson().setFitbitAccessToken(apiClientAgent.getTokens().getAccessToken());
            fitbitFacade.getPerson().setFitbitRefreshToken(apiClientAgent.getTokens().getRefreshToken());
            fitbitFacade.updatePerson();
            FitbitResetRequestsScheduledTask.setUsedRequests(fitbitFacade.getPerson().getFitbitUserId(), 1);
        } catch (FitbitAPIException ex) {
            LOG.log(Level.SEVERE, "refreshTokens() - Error al refrescar los tokens. Los tokens actuales de la persona son:\nAccess Token....: {0}\nRefresh Token...: {1}", new Object[]{fitbitFacade.getPerson().getFitbitAccessToken(), fitbitFacade.getPerson().getFitbitRefreshToken()});
            throw new HermesFitbitException("Fitbit.error.authorization");
        }
    }

    private String getCallBackURL(HttpServletRequest request, String nextPage) {
        StringBuilder url = new StringBuilder();
        boolean productionMode = request.getServerPort() == 80;

        url.append(request.getScheme());
        if (productionMode) {
            url.append("s");
        }
        url.append("://").append(request.getServerName());
        if (!productionMode) {
            url.append(":").append(request.getServerPort());
        }
        url.append(request.getContextPath()).append(nextPage);

        return url.toString();
    }

    public String getAuthorizeURL(HttpServletRequest request, String nextPage) throws HermesFitbitException {
        String callbackUrl = getCallBackURL(request, nextPage);

        LOG.log(Level.FINEST, "getAuthorizeURL() - URL de vuelta: {0}", callbackUrl);

        try {
            fitbitFacade.getPerson().setFitbitAccessToken(null);
            fitbitFacade.getPerson().setFitbitRefreshToken(null);
            return apiClientAgent.getAuthorizationURL(scopes, callbackUrl);
        } catch (FitbitAPIException ex) {
            LOG.log(Level.SEVERE, "getAuthorizeURL() - Error al obtener la URL de autorización de Fitbit", ex);
            throw new HermesFitbitException("Fitbit.error.authorization");
        }
    }

    public void completeAuthorization(String code) throws HermesFitbitException {
        try {
            apiClientAgent.createAccessTokenAndRefreshToken(code);
            fitbitFacade.getPerson().setFitbitAccessToken(apiClientAgent.getTokens().getAccessToken());
            fitbitFacade.getPerson().setFitbitRefreshToken(apiClientAgent.getTokens().getRefreshToken());
        } catch (FitbitAPIException ex) {
            LOG.log(Level.SEVERE, "completeAuthorization() - Error al completar la petición de tokens de acceso y de refresco", ex);
            throw new HermesFitbitException("Fitbit.error.authorization");
        }
    }

    private List<IntradaySummary> getIntradayData(Date startDate, Date endDate, TimeSeriesResourceType type) throws HermesException {
        LOG.log(Level.INFO, "getIntradayData() - Sincronizando de los datos por día de la persona {0} desde {1} hasta {2}", new Object[]{fitbitFacade.getPerson().getFullName(), Constants.df.format(startDate), Constants.df.format(endDate)});
        List<IntradaySummary> intradaySummaryList = new ArrayList();

        LocalDate localStartDate = new LocalDate(startDate);
        LocalDate localEndDate = new LocalDate(endDate);
        int days = Days.daysBetween(localStartDate, localEndDate).getDays() + 1;

        try {
            LocalDate currentDate = new LocalDate(localStartDate);

            // Vamos consultando los datos de Fitbit, para el tipo solicitado, desde la fecha de inicio hasta la fecha de fin, día a día.
            for (int i = 0; i < days; i++) {
                IntradaySummary intradaySummary = apiClientAgent.getIntradayTimeSeries(currentDate, type, DataPeriod.ONE_DAY);
                // Descartamos los registros sin información.
                if (intradaySummary.getSummary().getValue() == null && intradaySummary.getIntradayDataset().getDataset().isEmpty()) {
                    continue;
                }
                intradaySummaryList.add(intradaySummary);
                currentDate = new LocalDate(currentDate.plusDays(1));
            }
        } catch (FitbitAPIException ex) {
            if (ex.getStatusCode() == TOO_MANY_REQUESTS) {
                int remainingTime = (60 - Calendar.getInstance().get(Calendar.MINUTE));
                LOG.log(Level.INFO, "getIntradayData() - Se ha alcanzado el límite de peticiones a Fitbit hasta dentro de {0} minutos", remainingTime);
                throw new HermesException("Fitbit.warning.reachedRequestLimit", remainingTime);
            } else {
                LOG.log(Level.SEVERE, "getIntradayData() - Error al obtener los datos de los días solicitados", ex.getMessage());
                throw new HermesException("Fitbit.error.requestFailed");
            }
        }

        return intradaySummaryList;
    }

    public void synchronize(Date startDate, Date endDate) throws HermesException {
        try {
            LOG.log(Level.INFO, "synchronize() - Sincronización con Fitbit de la persona {0} desde {1} hasta {2}", new Object[]{fitbitFacade.toString(), Constants.df.format(startDate), Constants.df.format(endDate)});

            // Comprobamos si hay suficientes peticiones disponibles.
            checkEnoughRemainingRequests(startDate, endDate);

            ///////////
            // PASOS //
            ///////////
            LOG.log(Level.INFO, "synchronize() - Procesando los pasos");
            // Recogemos los datos de Fitbit en el rango indicado.
            List<ActivityLog> activityLogList = synchronizeStepsData(startDate, endDate);
            fitbitFacade.getPerson().addActivityLogList(activityLogList);

            ///////////
            // SUEÑO //
            ///////////
            LOG.log(Level.INFO, "synchronize() - Procesando el sueño");
            // Recogemos los datos de Fitbit en el rango indicado.
            List<SleepLog> sleepLogList = synchronizeSleepData(startDate, endDate);
            fitbitFacade.getPerson().addSleepLogList(sleepLogList);

            ////////////////////
            // RITMO CARDÍACO //
            ////////////////////
            LOG.log(Level.INFO, "synchronize() - Procesando el ritmo cardíaco");
            // Recogemos los datos de Fitbit en el rango indicado.
            List<HealthLog> healthLogList = synchronizeHeartRateData(startDate, endDate);
            fitbitFacade.getPerson().addHealthLogList(healthLogList);

            fitbitFacade.updatePerson();

            FitbitResetRequestsScheduledTask.setUsedRequests(fitbitFacade.getPerson().getFitbitUserId(), activityLogList.size() + sleepLogList.size() + healthLogList.size());
        } catch (HermesException ex) {
            // Si hubiera cualquier problema, por lo menos salvamos los nuevos tokens obtenidos de Fitbit.
            fitbitFacade.getPerson().setFitbitAccessToken(apiClientAgent.getTokens().getAccessToken());
            fitbitFacade.getPerson().setFitbitRefreshToken(apiClientAgent.getTokens().getRefreshToken());
            fitbitFacade.updatePerson();
        }
    }

    private List<ActivityLog> synchronizeStepsData(Date startDate, Date endDate) throws HermesException {
        LOG.log(Level.INFO, "synchronizeStepsData() - Sincronizando datos de los pasos de la persona {0} desde {1} hasta {2}", new Object[]{fitbitFacade.getPerson().getFullName(), Constants.df.format(startDate), Constants.df.format(endDate)});

        List<ActivityLog> activityLogList = new ArrayList<>();

        try {
            List<IntradaySummary> intradaySummaryList = getIntradayData(startDate, endDate, TimeSeriesResourceType.STEPS);

            for (IntradaySummary intradaySummary : intradaySummaryList) {
                ActivityLog activityLog = new ActivityLog();

                activityLog.setPerson(fitbitFacade.getPerson());
                activityLog.setDateLog(Constants.dfFitbit.parse(intradaySummary.getSummary().getDateTime()));
                activityLog.setTotal(Integer.parseInt(intradaySummary.getSummary().getValue()));
                activityLog.setStepLogList(new ArrayList());
                activityLog.setSent(false);

                // Comprobamos si trae datos de los pasos.
                if (intradaySummary.getIntradayDataset() != null) {
                    for (IntradayData intradayData : intradaySummary.getIntradayDataset().getDataset()) {
                        StepLog stepLog = new StepLog();
                        stepLog.setActivityLog(activityLog);
                        stepLog.setTimeLog(Constants.dfTime.parse(intradayData.getTime()));
                        stepLog.setSteps((int) intradayData.getValue());
                        stepLog.setSent(false);
                        activityLog.getStepLogList().add(stepLog);
                    }
                }

                activityLogList.add(activityLog);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, "synchronizeStepsData() - Error al convertir los datos del registro de pasos", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }

        return activityLogList;
    }

    private List<SleepLog> synchronizeSleepData(Date startDate, Date endDate) throws HermesException {
        LOG.log(Level.INFO, "synchronizeSleepData() - Sincronizando datos de sueño de la persona {0} desde {1} hasta {2}", new Object[]{fitbitFacade.getPerson().getFullName(), Constants.df.format(startDate), Constants.df.format(endDate)});

        List<SleepLog> sleepLogList = new ArrayList<>();
        LocalDate localStartDate = new LocalDate(startDate);
        LocalDate localEndDate = new LocalDate(endDate);
        int days = Days.daysBetween(localStartDate, localEndDate).getDays() + 1;
        com.fitbit.api.common.model.sleep.Sleep sleepFitbit;

        try {
            LocalDate currentDate = new LocalDate(localStartDate);

            for (int i = 0; i < days; i++) {
                sleepFitbit = apiClientAgent.getSleep(currentDate);

                // Comprobamos si el objeto tiene datos.
                if (sleepFitbit != null && !sleepFitbit.getSleepLogs().isEmpty()) {
                    // FIXME: ¿Sólo el sueño principal?
                    // Recogemos el sueño principal. (Puede haber varios registros de sueño, si el usuario ha puesto la pulsera en este modo varias veces)
                    com.fitbit.api.common.model.sleep.SleepLog mainSleepLogFitbit = null;
                    for (com.fitbit.api.common.model.sleep.SleepLog currentSleepLog : sleepFitbit.getSleepLogs()) {
                        if (currentSleepLog.isMainSleep()) {
                            mainSleepLogFitbit = currentSleepLog;
                            break;
                        }
                    }
                    // Si tenemos registro de sueño principal, recogemos los datos.
                    if (mainSleepLogFitbit != null) {
                        SleepLog sleepLog = new SleepLog();
                        sleepLog.setPerson(fitbitFacade.getPerson());
                        sleepLog.setDateLog(currentDate.toDate());
                        LocalTime tempLocalTime = new LocalTime(Constants.dfFitbitFull.parse(mainSleepLogFitbit.getStartTime()));
                        sleepLog.setStartTime(tempLocalTime.toDateTimeToday().toDate());
                        tempLocalTime = tempLocalTime.plusMinutes(mainSleepLogFitbit.getTimeInBed());
                        sleepLog.setEndTime(tempLocalTime.toDateTimeToday().toDate());
                        sleepLog.setMinutesAsleep(mainSleepLogFitbit.getMinutesAsleep());
                        sleepLog.setMinutesInBed(mainSleepLogFitbit.getTimeInBed());
                        sleepLog.setAwakenings(mainSleepLogFitbit.getAwakeningsCount());
                        sleepLog.setSent(false);

                        sleepLogList.add(sleepLog);
                    }
                }

                currentDate = new LocalDate(currentDate.plusDays(1));
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, "synchronizeSleepData() - Error al convertir la fecha del registro de sueño", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        } catch (FitbitAPIException ex) {
            if (ex.getStatusCode() == TOO_MANY_REQUESTS) {
                int remainingTime = (60 - Calendar.getInstance().get(Calendar.MINUTE));
                LOG.log(Level.INFO, "synchronizeSleepData() - Se ha alcanzado el límite de peticiones a Fitbit hasta dentro de {0} minutos", remainingTime);
                throw new HermesException("Fitbit.warning.reachedRequestLimit", remainingTime);
            } else {
                LOG.log(Level.SEVERE, "synchronizeSleepData() - Error al obtener los datos de los días solicitados", ex.getMessage());
                throw new HermesException("Fitbit.error.requestFailed");
            }
        }

        return sleepLogList;
    }

    private List<HealthLog> synchronizeHeartRateData(Date startDate, Date endDate) throws HermesException {
        LOG.log(Level.INFO, "synchronizeHeartRateData() - Sincronizando datos de ritmo cardíaco de la persona {0} desde {1} hasta {2}", new Object[]{fitbitFacade.getPerson().getFullName(), Constants.df.format(startDate), Constants.df.format(endDate)});

        List<HealthLog> healthLogList = new ArrayList<>();

        try {
            List<IntradaySummary> intradaySummaryList = getIntradayData(startDate, endDate, TimeSeriesResourceType.HEART);

            for (IntradaySummary intradaySummary : intradaySummaryList) {
                HealthLog healthLog = new HealthLog();

                healthLog.setPerson(fitbitFacade.getPerson());
                healthLog.setDateLog(Constants.dfFitbit.parse(intradaySummary.getSummary().getDateTime()));
                healthLog.setHeartLogList(new ArrayList());
                healthLog.setSent(false);

                // Comprobamos si trae datos de pulso cardíaco.
                if (intradaySummary.getIntradayDataset() != null) {
                    List<IntradayData> intradayDataList = intradaySummary.getIntradayDataset().getDataset();
                    if (intradayDataList != null && !intradayDataList.isEmpty()) {
                        int amount = 0;
                        for (IntradayData intradayData : intradayDataList) {
                            HeartLog heartLog = new HeartLog();
                            heartLog.setHealthLog(healthLog);
                            heartLog.setTimeLog(Constants.dfTime.parse(intradayData.getTime()));
                            heartLog.setRate((int) intradayData.getValue());
                            heartLog.setSent(false);
                            healthLog.getHeartLogList().add(heartLog);
                            amount += heartLog.getRate();
                        }
                        healthLog.setAverage(amount / intradayDataList.size());
                        healthLogList.add(healthLog);
                    }
                }
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, "synchronizeHeartRateData() - Error al convertir los datos del registro de ritmo cardíaco", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }

        return healthLogList;
    }

    public void transferUserInfoToPerson() throws HermesException {

        try {
            UserInfo userInfo = apiClientAgent.getUserInfo();
            // Transferimos la información personal de Fitbit a la ficha del usuario.
            String fullName = userInfo.getFullName();
            String nickName = userInfo.getNickname();
            String displayName = userInfo.getDisplayName();

            if (fullName != null && !fullName.isEmpty()) {
                fitbitFacade.getPerson().setFullName(fullName);
            } else if (displayName != null && !displayName.isEmpty()) {
                fitbitFacade.getPerson().setFullName(displayName);
            } else if (nickName != null && !nickName.isEmpty()) {
                fitbitFacade.getPerson().setFullName(nickName);
            }

            try {
                fitbitFacade.getPerson().setPhoto(ImageUtil.getPhotoImageAsByteArray(new URL(userInfo.getAvatar())));
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la foto del usuario de Fitbit", ex);
                throw new HermesException("Fitbit.error.requestFailed");
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la foto del usuario de Fitbit", ex);
                throw new HermesException("Fitbit.error.requestFailed");
            }
            // TODO: Ver qué se hace si falla la obtención de los datos de Fitbit!!!
            // Transferimos las credenciales de Fitbit.
            fitbitFacade.getPerson().setComments(userInfo.getAboutMe());
            fitbitFacade.getPerson().setFitbitAccessToken(apiClientAgent.getTokens().getAccessToken());
            fitbitFacade.getPerson().setFitbitRefreshToken(apiClientAgent.getTokens().getRefreshToken());
            fitbitFacade.getPerson().setFitbitUserId(userInfo.getEncodedId());
            FitbitResetRequestsScheduledTask.setUsedRequests(fitbitFacade.getPerson().getFitbitUserId(), 1);
        } catch (FitbitAPIException ex) {
            if (ex.getStatusCode() == TOO_MANY_REQUESTS) {
                int remainingTime = (60 - Calendar.getInstance().get(Calendar.MINUTE));
                LOG.log(Level.INFO, "getSleepLogData() - Se ha alcanzado el límite de peticiones a Fitbit hasta dentro de {0} minutos", remainingTime);
                throw new HermesException("Fitbit.warning.reachedRequestLimit", remainingTime);
            } else {
                LOG.log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la información del usuario de Fitbit", ex);
                throw new HermesException("Fitbit.error.requestFailed");
            }
        }
    }

    /**
     * Obtiene la fecha de la última sincronización de la pulsera de Fitbit.
     *
     * @return Fecha de la última sincronización en Fitbit.
     * @throws es.jyago.hermes.fitbit.HermesFitbitException
     * @throws HermesException
     */
    public Date getLastSyncDate() throws HermesFitbitException, HermesException {
        LOG.log(Level.INFO, "getLastSyncDate() - Obtener la última sincronización con Fitbit");
        refreshFitbitTokens();

        // Suponemos que las personas sólo tienen un dispositivo.
        String syncDate;
        try {
            List<Device> devicesList = apiClientAgent.getDevices();
            if (!devicesList.isEmpty()) {
                syncDate = devicesList.get(0).getLastSyncTime();
            } else {
                // Si no tiene vinculado ningún dispositivo, no se puede sincronizar.
                LOG.log(Level.SEVERE, "getLastSyncDate() - No tiene dispositivos Fitbit");
                throw new HermesException("Fitbit.error.noDevice");
            }
            FitbitResetRequestsScheduledTask.setUsedRequests(fitbitFacade.getPerson().getFitbitUserId(), 1);

            return Constants.dfFitbitFull.parse(syncDate);
        } catch (FitbitAPIException ex) {
            if (ex.getStatusCode() == TOO_MANY_REQUESTS) {
                int remainingTime = (60 - Calendar.getInstance().get(Calendar.MINUTE));
                LOG.log(Level.INFO, "getLastSyncDate() - Se ha alcanzado el límite de peticiones a Fitbit hasta dentro de {0} minutos", remainingTime);
                throw new HermesException("Fitbit.warning.reachedRequestLimit", remainingTime);
            } else {
                LOG.log(Level.SEVERE, "getLastSyncDate() - Error al obtener la fecha de la última sincronización de la pulsera de Fitbit", ex);
                throw new HermesException("Fitbit.error.requestFailed");
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, "getLastSyncDate() - Error al convertir la fecha de la última sincronización de la pulsera de Fitbit", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }
    }

    public int getNeededRequestsBetweenDates(Date startDate, Date endDate) {
        // Calculamos el número de días que han de sincronizarse.
        LocalDate localStartDate = new LocalDate(startDate);
        LocalDate localEndDate = new LocalDate(endDate);

        // Para traernos los datos de Fitbit, tenemos que hacerlo día a día.
        // Como actualmente tenemos que pedir la información de pasos y de sueño,
        // tendremos que multiplicar el número de días por el número de servicios que queramos solicitar
        // para saber el número de peticiones que necesitaremos.
        int days = Days.daysBetween(localStartDate, localEndDate).getDays() + 1; // Porque el propio día cuenta, en caso de que quiera sincronizar el día de hoy.

        // El número de peticiones serán los días por el número de servicios a sincronizar.
        return days * Constants.FitbitServices.values().length;
    }

    public void checkEnoughRemainingRequests(Date startDate, Date endDate) throws HermesException {
        checkEnoughRemainingRequests(getNeededRequestsBetweenDates(startDate, endDate));
    }

    // FIXME: Con Oauth2.0 no está implementado aún por Fitbit.
//    private void checkEnoughRemainingRequests(int requestsNeeded) throws HermesException {
//        // Obtenemos los límites actuales del usuario.
//        int remainingRequests = getRemainingRequests();
//
//        LOG.log(Level.INFO, "checkEnoughRemainingRequests() - Quedan {0} peticiones a Fitbit disponibles. Necesitamos {1} peticiones", new Object[]{remainingRequests, requestsNeeded});
//
//        if (remainingRequests == 0) {
//            // No quedan peticiones.
//            int minutes = 60 - (new LocalTime()).getMinuteOfHour();
//            LOG.log(Level.WARNING, "enoughRemainingRequests() - Se ha superado el límite de peticiones a Fitbit por hora. Se debe esperar {0} minutos", minutes);
//            throw new HermesException("Fitbit.warning.reachedRequestLimit", minutes);
//        } else if (remainingRequests < requestsNeeded) {
//            // No hay suficientes peticiones disponibles.
//            LOG.log(Level.WARNING, "enoughRemainingRequests() - No hay petiociones suficientes. Se requieren {0} peticiones y se dispone de {1}", new Object[]{requestsNeeded, remainingRequests});
//            throw new HermesException("Fitbit.warning.notEnoughRequests", requestsNeeded, remainingRequests);
//        }
//    }
    private void checkEnoughRemainingRequests(int requestsNeeded) throws HermesException {
        // Obtenemos los límites actuales del usuario.
        int personRemainingRequests = FitbitResetRequestsScheduledTask.getRemainingRequests(fitbitFacade.getPerson().getFitbitUserId());

        LOG.log(Level.INFO, "checkEnoughRemainingRequests() - Quedan {0} peticiones a Fitbit disponibles. Necesitamos {1} peticiones", new Object[]{personRemainingRequests, requestsNeeded});

        if (personRemainingRequests == 0) {
            // No quedan peticiones.
            int minutes = 60 - (new LocalTime()).getMinuteOfHour();
            LOG.log(Level.WARNING, "enoughRemainingRequests() - Se ha superado el límite de peticiones a Fitbit por hora. Se debe esperar {0} minutos", minutes);
            throw new HermesException("Fitbit.warning.reachedRequestLimit", minutes);
        } else if (personRemainingRequests < requestsNeeded) {
            // No hay suficientes peticiones disponibles.
            LOG.log(Level.WARNING, "enoughRemainingRequests() - No hay petiociones suficientes. Se requieren {0} peticiones y se dispone de {1}", new Object[]{requestsNeeded, personRemainingRequests});
            throw new HermesException("Fitbit.warning.notEnoughRequests", requestsNeeded, personRemainingRequests);
        }
    }

    // FIXME: Con Oauth2.0 no está implementado aún por Fitbit.
//    public int getRemainingRequests() {
//
//        int remainingRequests = 0;
//        try {
//            // Obtenemos los límites actuales del usuario.
//            remainingRequests = apiClientAgent.getRateLimitStatus().getRemainingHits();
//        } catch (FitbitAPIException ex) {
//            LOG.log(Level.SEVERE, "enoughRemainingRequests() - Error al comprobar si se ha alcanzado el límite de peticiones a Fitbit", ex);
//        }
//
//        LOG.log(Level.INFO, "getRemainingRequests() - Hay disponibles {0} peticiones a Fitbit", remainingRequests);
//
//        return remainingRequests;
//    }
}
