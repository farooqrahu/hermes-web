/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.fitbit;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.FitbitAPIEntityCache;
import com.fitbit.api.client.FitbitApiClientAgent;
import com.fitbit.api.client.FitbitApiCredentialsCache;
import com.fitbit.api.client.FitbitApiCredentialsCacheMapImpl;
import com.fitbit.api.client.FitbitApiEntityCacheMapImpl;
import com.fitbit.api.client.FitbitApiSubscriptionStorage;
import com.fitbit.api.client.FitbitApiSubscriptionStorageInMemoryImpl;
import com.fitbit.api.client.LocalUserDetail;
import com.fitbit.api.client.service.FitbitAPIClientService;
import com.fitbit.api.common.model.devices.Device;
import com.fitbit.api.common.model.timeseries.IntradaySummary;
import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.FitbitUser;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.ImageUtil;
import es.jyago.hermes.util.HermesException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * @author Jorge Yago
 */
public class HermesFitbitController {

    // TODO: Completar métodos, añadir comentarios y JavaDoc.
    private static final Logger log = Logger.getLogger(HermesFitbitController.class.getName());

    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_VERIFIER = "oauth_verifier";

    // JYFR: PRUEBA Le damos una validez de un mes a la cookie.
    private static final int APP_USER_COOKIE_TTL = 2592000;
    // JYFR: PRUEBA
    private static final String APP_USER_COOKIE_NAME = "fitbitClientUid";
    // JYFR: PRUEBA
    private static final String NOTIFICATION_UPDATES_SUBSCRIBER_ID = "1";

    private static final ResourceBundle hermesFitbit = ResourceBundle.getBundle("HermesFitbit");

    private Person person;
    private LocalUserDetail localUserDetail;

//    private FitbitAPIEntityCache entityCache;
    private FitbitApiCredentialsCache credentialsCache;
//    private FitbitApiSubscriptionStorage subscriptionStore;
    private FitbitAPIClientService<FitbitApiClientAgent> apiClientService;
    private APIResourceCredentials resourceCredentials;

    public HermesFitbitController(Person person) {
        this.person = person;
        initApiClientService(false);
    }

    // FIXME: ¿Tiene sentido este constructor con una persona vacía?
    public HermesFitbitController() {
        this(new Person());
    }

    public String getAuthorizeURL(HttpServletRequest request, String nextPage) throws HermesException {
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

        initApiClientService(true);

        try {
            return apiClientService.getResourceOwnerAuthorizationURL(localUserDetail, url + nextPage);
        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, "getAuthorizeURL() - Error al obtener la URL de autorización de Fitbit", ex);
            throw new HermesException("Fitbit.error.authorization");
        }
    }

    private void initApiClientService(boolean restore) {
        // FIXME: ¿Si no tienemos id, cancelar?
        if (person.getFitbitUserId() == null || person.getFitbitUserId().length() == 0) {
            person.setFitbitUserId(String.valueOf(Math.abs(new Random(System.currentTimeMillis()).nextInt())));
            resourceCredentials = new APIResourceCredentials(person.getFitbitUserId(), null, null);
        } else {
            // FIXME: ¿Sería la forma más correcta?
            resourceCredentials = new APIResourceCredentials(person.getFitbitUserId(), null, null);
            if (!restore) {
                resourceCredentials.setAccessToken(person.getFitbitAccessToken());
                resourceCredentials.setAccessTokenSecret(person.getFitbitAccessTokenSecret());
            }
        }

        // FIXME: Ver si todo es necesario.
        localUserDetail = new LocalUserDetail(person.getFitbitUserId());

        credentialsCache = new FitbitApiCredentialsCacheMapImpl();
        credentialsCache.saveResourceCredentials(localUserDetail, resourceCredentials);
        FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
        FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();

        apiClientService = new FitbitAPIClientService(
                new FitbitApiClientAgent(
                        hermesFitbit.getString("apiBaseUrl"),
                        hermesFitbit.getString("fitbitSiteBaseUrl"),
                        credentialsCache),
                hermesFitbit.getString("clientConsumerKey"),
                hermesFitbit.getString("clientConsumerSecret"),
                credentialsCache,
                entityCache,
                subscriptionStore);
    }

    public void completeAuthorization(String tempTokenReceived, String tempTokenVerifier) throws HermesException {
        resourceCredentials = apiClientService.getResourceCredentialsByTempToken(tempTokenReceived);

        if (resourceCredentials == null) {
            log.log(Level.SEVERE, "completeAuthorization() - Error al obtener las credenciales a partir del token temporal: {0}", tempTokenReceived);
            throw new HermesException("Fitbit.error.noCredentials");
        } else {
            // Solicitaremos las credenciales de acceso si no está autorizado aún.
            if (!resourceCredentials.isAuthorized()) {
                // Verificamos el 'token' temporal obtenido para poder solicitar los 'tokens' de acceso.
                resourceCredentials.setTempTokenVerifier(tempTokenVerifier);
                try {
                    // Obtenemos los 'tokens' de acceso para el usuario.
                    apiClientService.getTokenCredentials(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                } catch (FitbitAPIException ex) {
                    log.log(Level.SEVERE, "completeAuthorization() - Error al solicitar la autorización de Fitbit", ex);
                    throw new HermesException("Fitbit.error.noAuthorization");
                }
            }
        }
    }

    public List<IntradaySummary> getIntradayData(Date startDate, Date endDate) throws HermesException {
        FitbitUser fitbitUser = new FitbitUser(resourceCredentials.getLocalUserId());
        List<IntradaySummary> intradaySummaryList = new ArrayList();

        LocalDate localStartDate = new LocalDate(startDate);
        LocalDate localEndDate = new LocalDate(endDate);
        int days = Days.daysBetween(localStartDate, localEndDate).getDays();

        checkEnoughRemainingRequests(days);
        try {
            for (int i = 0; i <= days; i++) {
                LocalDate currentDate = new LocalDate(localStartDate.plusDays(i));

                IntradaySummary intraDayTimeSeries = apiClientService.getClient().getIntraDayTimeSeries(
                        localUserDetail,
                        fitbitUser,
                        TimeSeriesResourceType.STEPS,
                        currentDate);

                intradaySummaryList.add(intraDayTimeSeries);
            }
        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, "getIntradayData() - Error al obtener los datos de los días solicitados", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }

        return intradaySummaryList;
    }

    public void transferUserInfoToPerson(Person person) throws HermesException {
        try {
            UserInfo userInfo = apiClientService.getClient().getUserInfo(localUserDetail);
            // Transferimos la información personal de Fitbit a la ficha del usuario.
            person.setFirstName(userInfo.getNickname());
            try {
                person.setPhoto(ImageUtil.getPhotoImageAsByteArray(new URL(userInfo.getAvatar())));
            } catch (MalformedURLException ex) {
                log.log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la foto del usuario de Fitbit", ex);
                throw new HermesException("Fitbit.error.requestFailed");
            } catch (IOException ex) {
                log.log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la foto del usuario de Fitbit", ex);
                throw new HermesException("Fitbit.error.requestFailed");
            }
            // TODO: Ver qué se hace si falla la obtención de los datos de Fitbit!!!
            // Transferimos las credenciales de Fitbit.
            person.setComments(userInfo.getAboutMe());
            person.setFitbitAccessToken(resourceCredentials.getAccessToken());
            person.setFitbitAccessTokenSecret(resourceCredentials.getAccessTokenSecret());
            person.setFitbitUserId(resourceCredentials.getResourceId());

        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la información del usuario de Fitbit", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }
    }

    public boolean isResourceCredentialsSet() {
        return resourceCredentials != null;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        initApiClientService(false);
    }

    private void checkEnoughRemainingRequests(int requestsNeeded) throws HermesException {
        // Obtenemos los límites actuales del usuario.
        int remainingRequests = getRemainingRequests();

        try {
            if (remainingRequests < requestsNeeded) {
                DateTime timeToReset = apiClientService.getClientRateLimitStatus().getResetTime();
                // No hay suficientes peticiones disponibles.
                log.log(Level.WARNING, "enoughRemainingRequests() - Se ha llegado al límite de peticiones por hora a Fitbit");
                throw new HermesException("Fitbit.warning.ReachedRequestLimit", Constants.dfTime.format(timeToReset.getMillis()));
            }
        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, "enoughRemainingRequests() - Error al comprobar si se ha alcanzado el límite de peticiones a Fitbit", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }
    }

    public int getRemainingRequests() {
        int remainingRequests = 0;

        try {
            // Obtenemos los límites actuales del usuario.
            remainingRequests = apiClientService.getClientRateLimitStatus().getRemainingHits();
        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, "enoughRemainingRequests() - Error al comprobar si se ha alcanzado el límite de peticiones a Fitbit", ex);
        }

        return remainingRequests;
    }

    /**
     * Obtiene la fecha de la última sincronización de la pulsera de Fitbit.
     *
     * @return Fecha de la última sincronización en Fitbit.
     * @throws HermesException
     */
    public Date getLastSyncDate() throws HermesException {
        // Suponemos que las personas sólo tienen un dispositivo.
        String syncDate;
        try {
            List<Device> devicesList = apiClientService.getClient().getDevices(localUserDetail);
            if (devicesList.size() > 0) {
                syncDate = devicesList.get(0).getLastSyncTime();
            } else {
                // Si no tiene vinculado ningún dispositivo, no se puede sincronizar.
                log.log(Level.SEVERE, "getLastSyncDate() - No tiene dispositivos Fitbit");
                throw new HermesException("Fitbit.error.noDevice");
            }
            return Constants.dfFitbitFull.parse(syncDate);
        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, "getLastSyncDate() - Error al obtener la fecha de la última sincronización de la pulsera de Fitbit", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        } catch (ParseException ex) {
            log.log(Level.SEVERE, "getLastSyncDate() - Error al convertir la fecha de la última sincronización de la pulsera de Fitbit", ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }
    }

    /**
     * Obtiene el tiempo de espera hasta la siguiente renovación del cupo de
     * peticiones a la API de Fitbit.
     *
     * @return Tiempo restante hasta que se puedan realizar de nuevo peticiones
     * a la API de Fitbit, en milisegundos.
     * @throws HermesException
     */
    public long getWaitTime() throws HermesException {
        try {
            return apiClientService.getClientRateLimitStatus().getResetTime().getMillis() - (new Date()).getTime();
        } catch (FitbitAPIException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new HermesException("Fitbit.error.requestFailed");
        }
    }
}
