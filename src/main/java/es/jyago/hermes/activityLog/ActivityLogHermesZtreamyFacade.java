package es.jyago.hermes.activityLog;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.stepLog.StepLog;
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
 * Clase para transmitir los registros de actividad por Ztreamy.
 */
public class ActivityLogHermesZtreamyFacade extends AbstractHermesZtreamyFacade<ActivityLog> {

    private static final String STEPS_DATA = "Steps Data";
    private static final Logger LOG = Logger.getLogger(ActivityLogHermesZtreamyFacade.class.getName());

    public ActivityLogHermesZtreamyFacade(ActivityLog activityLog, Person person, String url) throws MalformedURLException, HermesException {
        super(activityLog, person, url, STEPS_DATA);
    }

    public ActivityLogHermesZtreamyFacade(Collection<ActivityLog> collectionActivityLog, Person person, String url) throws MalformedURLException, HermesException {
        super(collectionActivityLog, person, url, STEPS_DATA, false);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<ActivityLog> collectionActivityLog) {
        Map<String, Object> bodyObject = null;

        if (collectionActivityLog != null && !collectionActivityLog.isEmpty()) {
            List<ZtreamyActivityLog> listZtreamyActivityLog = new ArrayList<>();

            for (ActivityLog activityLog : collectionActivityLog) {
                List<ZtreamyStepLog> listZtreamyStepLog = new ArrayList<>();

                // Enviamos los datos que no tengan la marca de enviados.
                for (StepLog stepLog : activityLog.getStepLogList()) {
                    if (!stepLog.isSent()) {
                        // JYFR: 17-11-2015: Se limita el envío a únicamente los que tengan información distinta de cero.
                        if (stepLog.getSteps() > 0) {
                            ZtreamyStepLog ztreamyStepLog = new ZtreamyStepLog(stepLog.getTimeLog(), stepLog.getSteps());
                            listZtreamyStepLog.add(ztreamyStepLog);
                        }
                    }
                }

                if (!listZtreamyStepLog.isEmpty()) {
                    ZtreamyActivityLog ztreamyActivityLog = new ZtreamyActivityLog(activityLog.getDateLog(), listZtreamyStepLog);
                    listZtreamyActivityLog.add(ztreamyActivityLog);
                }
            }

            if (!listZtreamyActivityLog.isEmpty()) {
                bodyObject = new HashMap<>();
                if (listZtreamyActivityLog.size() == 1) {
                    bodyObject.put(STEPS_DATA, listZtreamyActivityLog.get(0));
                } else {
                    bodyObject.put(STEPS_DATA, listZtreamyActivityLog);
                }
            }
        }

        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyObject(ActivityLog activityLog) {
        HashSet<ActivityLog> collectionActivityLog = new HashSet();

        collectionActivityLog.add(activityLog);

        return getBodyObject(collectionActivityLog);
    }

    @Override
    public String getType() {
        return STEPS_DATA;
    }

    @Override
    public Collection<Object> getBodyObjects(Collection<ActivityLog> collection) {
        List<Object> listZtreamyActivityLog = new ArrayList<>();

        if (collection != null && !collection.isEmpty()) {

            for (ActivityLog activityLog : collection) {
                List<ZtreamyStepLog> listZtreamyStepLog = new ArrayList<>();

                for (StepLog stepLog : activityLog.getAggregatedValues()) {
                    ZtreamyStepLog ztreamyStepLog = new ZtreamyStepLog(stepLog.getTimeLog(), stepLog.getSteps());
                    listZtreamyStepLog.add(ztreamyStepLog);
                }

                ZtreamyActivityLog ztreamyActivityLog = new ZtreamyActivityLog(activityLog.getDateLog(), listZtreamyStepLog);
                listZtreamyActivityLog.add(ztreamyActivityLog);
            }
        }

        return listZtreamyActivityLog;
    }

    /**
     * Clase con los atributos mínimos en el registro de actividad, para enviar
     * por Ztreamy.
     */
    class ZtreamyActivityLog implements Serializable {

        private final String dateTime;
        private final List<ZtreamyStepLog> stepsList;

        public ZtreamyActivityLog(Date dateTime, List stepsList) {
            this.dateTime = Constants.df.format(dateTime);
            this.stepsList = stepsList;
        }

        public String getDateTime() {
            return dateTime;
        }

        public List<ZtreamyStepLog> getStepsList() {
            return stepsList;
        }
    }

    /**
     * Clase con los atributos mínimos en el registro de pasos, para enviar a
     * Ztreamy.
     */
    class ZtreamyStepLog implements Serializable {

        private final String timeLog;
        private final int steps;

        public ZtreamyStepLog(Date timeLog, int steps) {
            this.timeLog = timeLog != null ? Constants.dfTime.format(timeLog) : "";
            this.steps = steps;
        }

        public String getTimeLog() {
            return timeLog;
        }

        public int getSteps() {
            return steps;
        }
    }
}
