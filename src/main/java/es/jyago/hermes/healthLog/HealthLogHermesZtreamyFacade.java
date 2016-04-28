package es.jyago.hermes.healthLog;

import es.jyago.hermes.heartLog.HeartLog;
import es.jyago.hermes.person.Person;
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
 * Clase para transmitir los registros de ritmo cardíaco por Ztreamy. 
 */
public class HealthLogHermesZtreamyFacade extends AbstractHermesZtreamyFacade<HealthLog> {
    

    private static final String HEART_RATE_DATA = "Heart Rate Data";
    private static final Logger LOG = Logger.getLogger(HealthLogHermesZtreamyFacade.class.getName());

    public HealthLogHermesZtreamyFacade(HealthLog healthLog, Person person, String url) throws MalformedURLException, HermesException {
        super(healthLog, person, url, HEART_RATE_DATA);
    }

    public HealthLogHermesZtreamyFacade(Collection<HealthLog> collectionHealthLog, Person person, String url) throws MalformedURLException, HermesException {
        super(collectionHealthLog, person, url, HEART_RATE_DATA, false);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<HealthLog> collectionHealthLog) {
        Map<String, Object> bodyObject = null;

        if (collectionHealthLog != null && !collectionHealthLog.isEmpty()) {
            List<ZtreamyHealthLog> listZtreamyHealthLog = new ArrayList<>();

            for (HealthLog healthLog : collectionHealthLog) {
                List<ZtreamyHeartLog> listZtreamyHeartLog = new ArrayList<>();

                // Enviamos los datos que no tengan la marca de enviados.
                for (HeartLog heartLog : healthLog.getHeartLogList()) {
                    if (!heartLog.isSent()) {
                        // JYFR: 17-11-2015: Se limita el envío a únicamente los que tengan información distinta de cero.
                        if (heartLog.getRate() > 0) {
                            ZtreamyHeartLog ztreamyHeartLog = new ZtreamyHeartLog(heartLog.getTimeLog(), heartLog.getRate());
                            listZtreamyHeartLog.add(ztreamyHeartLog);
                        }
                    }
                }

                if (!listZtreamyHeartLog.isEmpty()) {
                    ZtreamyHealthLog ztreamyHealthLog = new ZtreamyHealthLog(healthLog.getDateLog(), listZtreamyHeartLog);
                    listZtreamyHealthLog.add(ztreamyHealthLog);
                }
            }

            if (!listZtreamyHealthLog.isEmpty()) {
                bodyObject = new HashMap<>();
                if (listZtreamyHealthLog.size() == 1) {
                    bodyObject.put(HEART_RATE_DATA, listZtreamyHealthLog.get(0));
                } else {
                    bodyObject.put(HEART_RATE_DATA, listZtreamyHealthLog);
                }
            }
        }

        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyObject(HealthLog healthLog) {
        HashSet<HealthLog> collectionHealthLog = new HashSet();

        collectionHealthLog.add(healthLog);

        return getBodyObject(collectionHealthLog);
    }

    @Override
    public String getType() {
        return HEART_RATE_DATA;
    }

    @Override
    public Collection<Object> getBodyObjects(Collection<HealthLog> collection) {
        List<Object> listZtreamyHealthLog = new ArrayList<>();

        if (collection != null && !collection.isEmpty()) {

            for (HealthLog healthLog : collection) {
                List<ZtreamyHeartLog> listZtreamyHeartLog = new ArrayList<>();

                for (HeartLog heartLog : healthLog.getHeartLogList()) {
                    // JYFR: 17-11-2015: Por petición de Miguel R. Luaces, se cambia el modo de envío, de un evento con una lista de datos, a una
                    //                   lista de eventos con un único dato cada uno.
                    //                   También se limita el envío a únicamente los que tengan información distinta de cero.
                    if (heartLog.getRate() > 0) {
                        ZtreamyHeartLog ztreamyHeartLog = new ZtreamyHeartLog(heartLog.getTimeLog(), heartLog.getRate());
                        listZtreamyHeartLog.add(ztreamyHeartLog);
                    }
                }

                ZtreamyHealthLog ztreamyHealthLog = new ZtreamyHealthLog(healthLog.getDateLog(), listZtreamyHeartLog);
                listZtreamyHealthLog.add(ztreamyHealthLog);
            }
        }

        return listZtreamyHealthLog;
    }

    /**
     * Clase con los atributos mínimos en el registro de salud, para enviar por
     * Ztreamy.
     */
    class ZtreamyHealthLog implements Serializable {

        private final String dateTime;
        private final List<ZtreamyHeartLog> heartRateList;

        public ZtreamyHealthLog(Date dateTime, List heartRateList) {
            this.dateTime = Constants.df.format(dateTime);
            this.heartRateList = heartRateList;
        }

        public String getDateTime() {
            return dateTime;
        }

        public List<ZtreamyHeartLog> getHeartRateList() {
            return heartRateList;
        }
    }

    /**
     * Clase con los atributos mínimos en el registro de ritmo cardíaco, para
     * enviar a Ztreamy.
     */
    class ZtreamyHeartLog implements Serializable {

        private final String timeLog;
        private final int heartRate;

        public ZtreamyHeartLog(Date timeLog, int heartRate) {
            this.timeLog = timeLog != null ? Constants.dfTime.format(timeLog) : "";
            this.heartRate = heartRate;
        }

        public String getTimeLog() {
            return timeLog;
        }

        public int getHearRate() {
            return heartRate;
        }
    }
}
