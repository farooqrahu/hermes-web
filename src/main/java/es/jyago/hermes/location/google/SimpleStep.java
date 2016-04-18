package es.jyago.hermes.location.google;

import es.jyago.hermes.util.Constants;

public class SimpleStep {

    public SimpleStep() {
    }

    private Integer distance;
    private Integer duration;
    private Location startLocation;
    private Location endLocation;
    private String travelMode;
    private String startAddress;
    private String endAddress;

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getFormattedTime() {
        return Constants.dfTime.format(duration * 1000);
    }

    public String getFormattedDistance() {
        return String.format( "%.2f", (distance / 1000.0f)) + " Km.";
    }
}
