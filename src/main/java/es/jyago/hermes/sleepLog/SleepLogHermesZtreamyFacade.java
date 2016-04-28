package es.jyago.hermes.sleepLog;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.ztreamy.AbstractHermesZtreamyFacade;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Clase para transmitir los registros de sueño por Ztreamy.
 */
public class SleepLogHermesZtreamyFacade extends AbstractHermesZtreamyFacade<SleepLog> {

    private static final String SLEEP_DATA = "Sleep Data";
    private static final Logger LOG = Logger.getLogger(SleepLogHermesZtreamyFacade.class.getName());

    public SleepLogHermesZtreamyFacade(SleepLog sleepLog, Person person, String url) throws MalformedURLException, HermesException {
        super(sleepLog, person, url, SLEEP_DATA);
    }

    public SleepLogHermesZtreamyFacade(Collection<SleepLog> collectionSleepLog, Person person, String url) throws MalformedURLException, HermesException {
        super(collectionSleepLog, person, url, SLEEP_DATA, false);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<SleepLog> collectionSleepLog) {
        Map<String, Object> bodyObject = null;

        if (collectionSleepLog != null && !collectionSleepLog.isEmpty()) {
            List<ZtreamySleepLog> listZtreamySleepLog = new ArrayList<>();
            for (SleepLog sleepLog : collectionSleepLog) {
                // Enviamos los datos que no tengan la marca de enviados.
                if (!sleepLog.isSent()) {
                    listZtreamySleepLog.add(new ZtreamySleepLog(sleepLog));
                }
            }
            bodyObject = new HashMap<>();
            if (listZtreamySleepLog.size() == 1) {
                bodyObject.put(SLEEP_DATA, listZtreamySleepLog.get(0));
            } else {
                bodyObject.put(SLEEP_DATA, listZtreamySleepLog);
            }
        }

        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyObject(SleepLog sleepLog) {
        HashSet<SleepLog> collectionSleepLog = new HashSet();

        collectionSleepLog.add(sleepLog);

        return getBodyObject(collectionSleepLog);
    }

    @Override
    public String getType() {
        return SLEEP_DATA;
    }

    @Override
    public Collection<Object> getBodyObjects(Collection<SleepLog> collection) {
        List<Object> listZtreamySleepLog = new ArrayList<>();

        if (collection != null && !collection.isEmpty()) {
            for (SleepLog sleepLog : collection) {
                listZtreamySleepLog.add(new ZtreamySleepLog(sleepLog));
            }
        }

        return listZtreamySleepLog;
    }

    /**
     * Clase con los atributos mínimos en el registro de actividad, para enviar
     * por Ztreamy.
     */
    class ZtreamySleepLog implements Serializable {

        private final String dateTime;
        private final int minutesAsleep;
        private final int awakenings;
        private final int minutesInBed;
        private final String startTime;
        private final String endTime;

        public ZtreamySleepLog(SleepLog sleepLog) {
            this.dateTime = Constants.df.format(sleepLog.getDateLog());
            this.minutesAsleep = sleepLog.getMinutesAsleep();
            this.awakenings = sleepLog.getAwakenings();
            this.minutesInBed = sleepLog.getMinutesInBed();
            this.startTime = Constants.dfTime.format(sleepLog.getStartTime());
            this.endTime = Constants.dfTime.format(sleepLog.getEndTime());
        }

        public String getDateTime() {
            return dateTime;
        }

        public int getMinutesAsleep() {
            return minutesAsleep;
        }

        public int getAwakenings() {
            return awakenings;
        }

        public int getMinutesInBed() {
            return minutesInBed;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }
    }
}
