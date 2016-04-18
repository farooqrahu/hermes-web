package es.jyago.hermes.location.google;

import java.util.List;

public class TrackInfo {

    private SimpleStep summary;
    private List<SimpleStep> simpleStepList;

    public TrackInfo() {
    }

    public SimpleStep getSummary() {
        return summary;
    }

    public void setSummary(SimpleStep summary) {
        this.summary = summary;
    }

    public List<SimpleStep> getSimpleStepList() {
        return simpleStepList;
    }

    public void setSimpleStepList(List<SimpleStep> simpleStepList) {
        this.simpleStepList = simpleStepList;
    }
}
