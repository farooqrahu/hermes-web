package es.jyago.hermes.openStreetMap;

import com.google.gson.annotations.SerializedName;

public class GeomWaySteps {

    @SerializedName("linkId")
    private Integer linkId;
    @SerializedName("maxSpeed")
    private Integer maxSpeed;
    @SerializedName("linkName")
    private String linkName;
    @SerializedName("linkType")
    private String linkType;
    @SerializedName("length")
    private Double length;
    @SerializedName("cost")
    private Double cost;
    @SerializedName("geom_way")
    private GeomWay geomWay;

    /**
     *
     * @return The linkId
     */
    public Integer getLinkId() {
        return linkId;
    }

    /**
     *
     * @param linkId The linkId
     */
    public void setLinkId(Integer linkId) {
        this.linkId = linkId;
    }

    /**
     *
     * @return The maxSpeed
     */
    public Integer getMaxSpeed() {
        return maxSpeed;
    }

    /**
     *
     * @param maxSpeed The maxSpeed
     */
    public void setMaxSpeed(Integer maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     *
     * @return The linkName
     */
    public String getLinkName() {
        return linkName;
    }

    /**
     *
     * @param linkName The linkName
     */
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    /**
     *
     * @return The linkType
     */
    public String getLinkType() {
        return linkType;
    }

    /**
     *
     * @param linkType The linkType
     */
    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    /**
     *
     * @return The length
     */
    public Double getLength() {
        return length;
    }

    /**
     *
     * @param length The length
     */
    public void setLength(Double length) {
        this.length = length;
    }

    /**
     *
     * @return The cost
     */
    public Double getCost() {
        return cost;
    }

    /**
     *
     * @param cost The cost
     */
    public void setCost(Double cost) {
        this.cost = cost;
    }

    /**
     *
     * @return The geomWay
     */
    public GeomWay getGeomWay() {
        return geomWay;
    }

    /**
     *
     * @param geomWay The geom_way
     */
    public void setGeomWay(GeomWay geomWay) {
        this.geomWay = geomWay;
    }

}
