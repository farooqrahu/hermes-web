package es.jyago.hermes.google.directions;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Route {

    @SerializedName("legs")
    @Expose
    private List<Leg> legs = new ArrayList<>();
    @SerializedName("overview_polyline")
    @Expose
    private OverviewPolyline overviewPolyline;
    @SerializedName("summary")
    @Expose
    private String summary;

    /**
     *
     * @return The legs
     */
    public List<Leg> getLegs() {
        return legs;
    }

    /**
     *
     * @param legs The legs
     */
    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    /**
     *
     * @return The overviewPolyline
     */
    public OverviewPolyline getOverviewPolyline() {
        return overviewPolyline;
    }

    /**
     *
     * @param overviewPolyline The overview_polyline
     */
    public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }

    /**
     *
     * @return The summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     *
     * @param summary The summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
}
