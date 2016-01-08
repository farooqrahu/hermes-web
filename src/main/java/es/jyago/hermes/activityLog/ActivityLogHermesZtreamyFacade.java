/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import ztreamy.Event;

/**
 * Clase para transmitir los registros de actividad por Ztreamy.
 *
 * @author Jorge Yago
 */
public class ActivityLogHermesZtreamyFacade extends AbstractHermesZtreamyFacade<ActivityLog> {

    private static final String STEPS_DATA = "Steps Data";
    private static final Logger log = Logger.getLogger(ActivityLogHermesZtreamyFacade.class.getName());

    public ActivityLogHermesZtreamyFacade(ActivityLog activityLog, Person person, String url) throws MalformedURLException, HermesException {
        super(activityLog, person, url);
    }

    public ActivityLogHermesZtreamyFacade(Collection<ActivityLog> collectionActivityLog, Person person, String url) throws MalformedURLException, HermesException {
        super(collectionActivityLog, person, url, false);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<ActivityLog> collectionActivityLog) {
        Map<String, Object> bodyObject = null;

        if (collectionActivityLog != null && !collectionActivityLog.isEmpty()) {
            List<ZtreamyActivityLog> listZtreamyActivityLog = new ArrayList<>();

            for (ActivityLog activityLog : collectionActivityLog) {
                List<ZtreamyStepLog> listZtreanyStepLog = new ArrayList<>();

                for (StepLog stepLog : activityLog.getAggregatedValues()) {
                    ZtreamyStepLog ztreamyStepLog = new ZtreamyStepLog(stepLog.getTimeLog(), stepLog.getSteps());
                    listZtreanyStepLog.add(ztreamyStepLog);
                }

                ZtreamyActivityLog ztreanyActivityLog = new ZtreamyActivityLog(activityLog.getDateLog(), listZtreanyStepLog);
                listZtreamyActivityLog.add(ztreanyActivityLog);
            }
            bodyObject = new HashMap<>();
            if (listZtreamyActivityLog.size() == 1) {
                bodyObject.put(STEPS_DATA, listZtreamyActivityLog.get(0));
            } else {
                bodyObject.put(STEPS_DATA, listZtreamyActivityLog);
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
    public Event prepareEvent() {
        log.log(Level.INFO, "init() - Preparando el envío de datos de pasos por Ztreamy de: {0}", getPerson().getFullName());
        String sha = getPerson().getSha();
        if (sha == null || sha.length() == 0) {
            sha = new String(Hex.encodeHex(DigestUtils.sha256(getPerson().getEmail())));
        }
        return new Event(sha, MediaType.APPLICATION_JSON, getUrl(), STEPS_DATA);
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
                List<ZtreamyStepLog> listZtreanyStepLog = new ArrayList<>();

                for (StepLog stepLog : activityLog.getAggregatedValues()) {
                    ZtreamyStepLog ztreamyStepLog = new ZtreamyStepLog(stepLog.getTimeLog(), stepLog.getSteps());
                    listZtreanyStepLog.add(ztreamyStepLog);
                }

                ZtreamyActivityLog ztreamyActivityLog = new ZtreamyActivityLog(activityLog.getDateLog(), listZtreanyStepLog);
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
