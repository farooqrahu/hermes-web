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
import com.fitbit.api.common.model.timeseries.IntradaySummary;
import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.ApiSubscription;
import com.fitbit.api.model.FitbitUser;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.ImageUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * @author Jorge Yago
 */
public class HermesFitbitController {

    // JYFR: PRUEBA Le damos una validez de un mes a la cookie.
    private static final int APP_USER_COOKIE_TTL = 2592000;
    // JYFR: PRUEBA
    private static final String APP_USER_COOKIE_NAME = "fitbitClientUid";
    // JYFR: PRUEBA
    private static final String NOTIFICATION_UPDATES_SUBSCRIBER_ID = "1";

    public static final String OAUTH_TOKEN = "oauth_token";

    public static final String OAUTH_VERIFIER = "oauth_verifier";

    private static final ResourceBundle ampFitbit = ResourceBundle.getBundle("AMPFitbit");

    private final Person person;
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

    public String getAuthorizeURL(HttpServletRequest request, String nextPage) {
        populate();
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        initApiClientService(true);

        try {
            return apiClientService.getResourceOwnerAuthorizationURL(localUserDetail, url + nextPage);
        } catch (FitbitAPIException ex) {
            Logger.getLogger(HermesFitbitController.class.getName()).log(Level.SEVERE, "getAuthorizeURL() - Error al obtener la url de autorización de Fitbit", ex);
        }

        return null;
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
                        ampFitbit.getString("apiBaseUrl"),
                        ampFitbit.getString("fitbitSiteBaseUrl"),
                        credentialsCache),
                ampFitbit.getString("clientConsumerKey"),
                ampFitbit.getString("clientConsumerSecret"),
                credentialsCache,
                entityCache,
                subscriptionStore);
    }

    public boolean populate() {
        resourceCredentials = apiClientService.getResourceCredentialsByUser(localUserDetail);
        boolean isAuthorized = resourceCredentials != null && resourceCredentials.isAuthorized();
        boolean isSubscribed = false;

        if (isAuthorized) {
            List<ApiSubscription> subscriptions = Collections.emptyList();

            try {
                subscriptions = apiClientService.getClient().getSubscriptions(localUserDetail);
            } catch (FitbitAPIException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al suscribirse", e);
            }
            if (localUserDetail != null && subscriptions.size() > 0) {
                isSubscribed = true;
            }
        }

        return isSubscribed;
    }

    public void completeAuthorization(String tempTokenReceived, String tempTokenVerifier) {
        resourceCredentials = apiClientService.getResourceCredentialsByTempToken(tempTokenReceived);

        if (resourceCredentials == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "completeAuthorization() - Error al obtener las credenciales a partir del token temporal: {0}", tempTokenReceived);
        } else {
            // Solicitaremos las credenciales de acceso si no está autorizado aún.
            if (!resourceCredentials.isAuthorized()) {
                // Verificamos el 'token' temporal obtenido para poder solicitar los 'tokens' de acceso.
                resourceCredentials.setTempTokenVerifier(tempTokenVerifier);
                try {
                    // Obtenemos los 'tokens' de acceso para el usuario.
                    apiClientService.getTokenCredentials(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                } catch (FitbitAPIException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "completeAuthorization() - Error al solicitar la autorización de Fitbit", e);
                    // TODO: Modificar para que el error para que salga por pantalla.
                    // request.setAttribute("errors", Collections.singletonList(e.getMessage()));
                }
            }
        }
    }

    public List<IntradaySummary> getIntradayData(Date startDate, Date endDate) throws FitbitAPIException {
        FitbitUser fitbitUser = new FitbitUser(resourceCredentials.getLocalUserId());
        List<IntradaySummary> intradaySummaryList = new ArrayList();

        LocalDate localStartDate = new LocalDate(startDate);
        LocalDate localEndDate = new LocalDate(endDate);
        int days = Days.daysBetween(localStartDate, localEndDate).getDays();

        for (int i = 0; i <= days; i++) {
            LocalDate currentDate = new LocalDate(localStartDate.plusDays(i));

            IntradaySummary intraDayTimeSeries = apiClientService.getClient().getIntraDayTimeSeries(
                    localUserDetail,
                    fitbitUser,
                    TimeSeriesResourceType.STEPS,
                    currentDate);

            intradaySummaryList.add(intraDayTimeSeries);
        }

        return intradaySummaryList;
    }

    public void transferUserInfoToPerson(Person person) {
        try {
            UserInfo userInfo = apiClientService.getClient().getUserInfo(localUserDetail);
            // Transferimos la información personal de Fitbit a la ficha del usuario.
            person.setFirstName(userInfo.getFullName());
            try {
                person.setPhoto(ImageUtil.getPhotoImageAsByteArray(new URL(userInfo.getAvatar())));
            } catch (MalformedURLException ex) {
                Logger.getLogger(HermesFitbitController.class.getName()).log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la foto del usuario de Fitbit" , ex);
            } catch (IOException ex) {
                Logger.getLogger(HermesFitbitController.class.getName()).log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la foto del usuario de Fitbit", ex);
            }
            person.setComments(userInfo.getAboutMe());
        } catch (FitbitAPIException ex) {
            Logger.getLogger(HermesFitbitController.class.getName()).log(Level.SEVERE, "transferUserInfoToPerson() - Error al obtener la información del usuario de Fitbit", ex);
        }

        // Transferimos las credenciales de Fitbit.
        person.setFitbitAccessToken(resourceCredentials.getAccessToken());
        person.setFitbitAccessTokenSecret(resourceCredentials.getAccessTokenSecret());
        person.setFitbitUserId(resourceCredentials.getResourceId());
    }

    public boolean isResourceCredentialsSet() {
        return resourceCredentials != null;
    }
}
