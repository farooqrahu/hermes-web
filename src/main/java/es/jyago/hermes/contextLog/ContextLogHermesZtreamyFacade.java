package es.jyago.hermes.contextLog;

import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.ztreamy.AbstractHermesZtreamyFacade;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Clase para transmitir los registros de contexto por Ztreamy.
 */
public class ContextLogHermesZtreamyFacade extends AbstractHermesZtreamyFacade<ContextLog> {

    private static final String CONTEXT_DATA = "Context Data";
    private static final Logger LOG = Logger.getLogger(ContextLogHermesZtreamyFacade.class.getName());
    
    public ContextLogHermesZtreamyFacade(String applicationId, ContextLog contextLog, String sha, String url) throws MalformedURLException, HermesException {
        super(applicationId, contextLog, sha, url, CONTEXT_DATA);
    }

    public ContextLogHermesZtreamyFacade(String applicationId, Collection<ContextLog> collectionContextLog, String sha, String url) throws MalformedURLException, HermesException {
        super(applicationId, collectionContextLog, sha, url, CONTEXT_DATA, false);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<ContextLog> collectionContextLog) {
        Map<String, Object> bodyObject = null;

        if (collectionContextLog != null && !collectionContextLog.isEmpty()) {
            List<ZtreamyContextLog> listZtreamyContextLog = new ArrayList<>();

            for (ContextLog contextLog : collectionContextLog) {
                List<ZtreamyContextLogDetail> listZtreamyContextLogDetail = new ArrayList<>();

                // Enviamos los datos que no tengan la marca de enviados.
                for (ContextLogDetail contextLogDetail : contextLog.getContextLogDetailList()) {
                    if (!contextLogDetail.isSent()) {
                        // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
                        //                   lista de eventos con un único dato cada uno.
                        ZtreamyContextLogDetail ztreamyContextLogDetail = new ZtreamyContextLogDetail(
                                contextLogDetail.getTimeLog(),
                                contextLogDetail.getLatitude(),
                                contextLogDetail.getLongitude(),
                                contextLogDetail.getAccuracy(),
                                contextLogDetail.getDetectedActivity());
                        listZtreamyContextLogDetail.add(ztreamyContextLogDetail);
                    }
                }

                if (!listZtreamyContextLogDetail.isEmpty()) {
                    ZtreamyContextLog ztreamyContextLog = new ZtreamyContextLog(contextLog.getDateLog(), listZtreamyContextLogDetail);
                    listZtreamyContextLog.add(ztreamyContextLog);
                }
            }

            if (!listZtreamyContextLog.isEmpty()) {
                bodyObject = new HashMap<>();
                if (listZtreamyContextLog.size() == 1) {
                    bodyObject.put(CONTEXT_DATA, listZtreamyContextLog.get(0));
                } else {
                    bodyObject.put(CONTEXT_DATA, listZtreamyContextLog);
                }
            }
        }

        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyObject(ContextLog contextLog) {
        HashSet<ContextLog> collectionContextLog = new HashSet();

        collectionContextLog.add(contextLog);

        return getBodyObject(collectionContextLog);
    }

    @Override
    public String getType() {
        return CONTEXT_DATA;
    }

    @Override
    public Collection<Object> getBodyObjects(Collection<ContextLog> collection) {
        List<Object> listZtreamyHealthLog = new ArrayList<>();

        if (collection != null && !collection.isEmpty()) {

            for (ContextLog contextLog : collection) {
                List<ZtreamyContextLogDetail> listZtreamyHeartLog = new ArrayList<>();

                for (ContextLogDetail contextLogDetail : contextLog.getContextLogDetailList()) {
                    // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
                    //                   lista de eventos con un único dato cada uno.
                    ZtreamyContextLogDetail ztreamyContextLogDetail = new ZtreamyContextLogDetail(
                            contextLogDetail.getTimeLog(),
                            contextLogDetail.getLatitude(),
                            contextLogDetail.getLongitude(),
                            contextLogDetail.getAccuracy(),
                            contextLogDetail.getDetectedActivity());
                    listZtreamyHeartLog.add(ztreamyContextLogDetail);
                }

                ZtreamyContextLog ztreamyHealthLog = new ZtreamyContextLog(contextLog.getDateLog(), listZtreamyHeartLog);
                listZtreamyHealthLog.add(ztreamyHealthLog);
            }
        }

        return listZtreamyHealthLog;
    }

    /**
     * Clase con los atributos mínimos en el registro de contexto, para enviar
     * por Ztreamy.
     */
    class ZtreamyContextLog implements Serializable {

        private final String dateTime;
        private final List<ZtreamyContextLogDetail> contextLogDetailList;

        public ZtreamyContextLog(Date dateTime, List contextLogDetailList) {
            this.dateTime = Constants.df.format(dateTime);
            this.contextLogDetailList = contextLogDetailList;
        }

        public String getDateTime() {
            return dateTime;
        }

        public List<ZtreamyContextLogDetail> getContextLogDetailList() {
            return contextLogDetailList;
        }
    }

    /**
     * Clase con los atributos mínimos en el registro detalle del contexto, para
     * enviar a Ztreamy.
     */
    class ZtreamyContextLogDetail implements Serializable {

        private final String timeLog;
        private final Double latitude;
        private final Double longitude;
        private final String detectedActivity;
        private final Float accuracy;

        public ZtreamyContextLogDetail(Date timeLog, Double latitude, Double longitude, Float accuracy, String detectedActivity) {
            this.timeLog = timeLog != null ? Constants.dfTime.format(timeLog) : "";
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.detectedActivity = detectedActivity;
        }

        public String getTimeLog() {
            return timeLog;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public String getDetectedActivity() {
            return detectedActivity;
        }

        public Float getAccuracy() {
            return accuracy;
        }
    }
}
