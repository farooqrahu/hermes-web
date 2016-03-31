package es.jyago.hermes.activityLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivitySession {

    private Date startDate;
    private Date endDate;
    private int steps;
    private List<RestSession> restsList;

    public ActivitySession() {
        restsList = new ArrayList<>();
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public List<RestSession> getRestsList() {
        return restsList;
    }

    public void setRestsList(List<RestSession> restsList) {
        this.restsList = restsList;
    }
}
