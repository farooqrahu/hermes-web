/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.ztreamy;

import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.util.Constants;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
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
        Collection<ZtreamyActivityLog> setZtreamyActivityLog = new HashSet<>();

        for (ActivityLog activityLog : collectionActivityLog) {
            
            List<ZtreamyStepLog> listZtreanyStepLog = new ArrayList<>();
            
            for (StepLog stepLog : activityLog.getStepLogCollection()) {
                ZtreamyStepLog ztreanyStepLog = new ZtreamyStepLog(stepLog.getTimeLog(), stepLog.getSteps());
                listZtreanyStepLog.add(ztreanyStepLog);
            }

            ZtreamyActivityLog ztreanyActivityLog = new ZtreamyActivityLog(activityLog.getDate(), listZtreanyStepLog);
            setZtreamyActivityLog.add(ztreanyActivityLog);
        }

        Map<String, Object> bodyObject = new HashMap<>();
        bodyObject.put("dataset", setZtreamyActivityLog);
        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyObject(ActivityLog activityLog) {
        HashSet<ActivityLog> collectionActivityLog = new HashSet();

        collectionActivityLog.add(activityLog);

        return getBodyObject(collectionActivityLog);
    }

//    private static JsonObject getJSONElement(StepLog stepLog) {
//
//        JsonObject jsonObject = new JsonObject();
//
//        jsonObject.addProperty("time", dfTime.format(stepLog.getTimeLog().getTime()));
//        jsonObject.addProperty("value", Integer.toString(stepLog.getSteps()));
//
//        return jsonObject;
//    }
//
//    @Override
//    public Map<String, Object> getBodyObject(List<StepLog> list) {
//        Map<String, Object> bodyObject = new LinkedHashMap<String, Object>();
//        JsonObject body = new JsonObject();
//        JsonArray p = new JsonArray();
//
//        for (StepLog stepLog : list) {
//            p.add(getElement(stepLog));
//        }
//        body.add("dataset", p);
//
//        return bodyObject;
//    }
//
//    private JsonObject getElement(StepLog stepLog) {
//
//        JsonObject jsonObject = new JsonObject();
//
//        jsonObject.addProperty("time", dfTime.format(stepLog.getTimeLog().getTime()));
//        jsonObject.addProperty("value", Integer.toString(stepLog.getSteps()));
//
//        return jsonObject;
//    }
    class ZtreamyActivityLog {

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

    class ZtreamyStepLog {

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
