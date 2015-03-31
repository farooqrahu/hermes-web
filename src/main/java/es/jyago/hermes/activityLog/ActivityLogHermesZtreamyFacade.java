/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.activityLog;

import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.util.Constants;
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

/**
 * Clase para transmitir los registros de actividad por Ztreamy.
 *
 * @author Jorge Yago
 */
public class ActivityLogHermesZtreamyFacade extends AbstractHermesZtreamyFacade<ActivityLog> {

    public ActivityLogHermesZtreamyFacade(ActivityLog activityLog) throws MalformedURLException {
        super(activityLog);
    }

    public ActivityLogHermesZtreamyFacade(Collection<ActivityLog> collectionActivityLog) throws MalformedURLException {
        super(collectionActivityLog);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<ActivityLog> collectionActivityLog) {
        List<ZtreamyActivityLog> listZtreamyActivityLog = new ArrayList<>();

        for (ActivityLog activityLog : collectionActivityLog) {
            List<ZtreamyStepLog> listZtreanyStepLog = new ArrayList<>();

            for (StepLog stepLog : activityLog.getAggregatedStepCollection()) {
                ZtreamyStepLog ztreamyStepLog = new ZtreamyStepLog(stepLog.getTimeLog(), stepLog.getSteps());
                listZtreanyStepLog.add(ztreamyStepLog);
            }

            ZtreamyActivityLog ztreanyActivityLog = new ZtreamyActivityLog(activityLog.getDate(), listZtreanyStepLog);
            listZtreamyActivityLog.add(ztreanyActivityLog);
        }

        Map<String, Object> bodyObject = new HashMap<>();
        bodyObject.put("dataset", listZtreamyActivityLog);
        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyObject(ActivityLog activityLog) {
        HashSet<ActivityLog> collectionActivityLog = new HashSet();

        collectionActivityLog.add(activityLog);

        return getBodyObject(collectionActivityLog);
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
            this.timeLog = Constants.dfTime.format(timeLog);
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
