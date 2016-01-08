/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.location;

import es.jyago.hermes.util.Constants;
import java.util.Date;

/**
 *
 * @author Jorge Yago
 */
public class IntervalData {

    private int intervalId;
    private Date date;

    // Datos de velocidad.
    private double minSpeed;
    private double maxSpeed;
    private double averageSpeed;
    private double medianSpeed;
    private double standardDeviationSpeed;
    private double pke;
    private double speedAtStart;
    private double speedAtEnd;

    // Datos de posición.
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private double length;
    private double cummulativeLength;

    // Datos temporales.
    private Date startDate;
    private Date endDate;

    // Datos de ritmo cardíaco.
    private int minHeartRate;
    private int maxHeartRate;
    private double averageHeartRate;
    private double medianHeartRate;
    private double standardDeviationHeartRate;
    private int heartRateAtStart;
    private int heartRateAtEnd;
    
    // Tramo previo. Servirá para analizar la tendencia.
    private IntervalData previousIntervalData;

    public IntervalData() {
        intervalId = 0;
        date = null;

        minSpeed = 0.0d;
        maxSpeed = 0.0d;
        averageSpeed = 0.0d;
        medianSpeed = 0.0d;
        standardDeviationSpeed = 0.0d;
        pke = 0.0d;
        speedAtStart = 0.0d;
        speedAtEnd = 0.0d;

        startLatitude = 0.0d;
        startLongitude = 0.0d;
        endLatitude = 0.0d;
        endLongitude = 0.0d;
        length = 0.0d;
        cummulativeLength = 0.0d;

        minHeartRate = 0;
        maxHeartRate = 0;
        averageHeartRate = 0.0d;
        medianHeartRate = 0.0d;
        standardDeviationHeartRate = 0.0d;
        heartRateAtStart = 0;
        heartRateAtEnd = 0;
        
        previousIntervalData = null;
    }

    public int getIntervalId() {
        return intervalId;
    }

    public void setIntervalId(int intervalId) {
        this.intervalId = intervalId;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getMedianSpeed() {
        return medianSpeed;
    }

    public void setMedianSpeed(double medianSpeed) {
        this.medianSpeed = medianSpeed;
    }

    public double getStandardDeviationSpeed() {
        return standardDeviationSpeed;
    }

    public void setStandardDeviationSpeed(double standardDeviationSpeed) {
        this.standardDeviationSpeed = standardDeviationSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(double minSpeed) {
        this.minSpeed = minSpeed;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getCummulativeLength() {
        return cummulativeLength;
    }

    public void setCummulativeLength(double cummulativeLength) {
        this.cummulativeLength = cummulativeLength;
    }

    public double getPke() {
        return pke;
    }

    public void setPke(double pke) {
        this.pke = pke;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormattedDate() {
        return this.date != null ? Constants.df.format(this.date) : "";
    }

    public double getSpeedAtStart() {
        return speedAtStart;
    }

    public void setSpeedAtStart(double speedAtStart) {
        this.speedAtStart = speedAtStart;
    }

    public double getSpeedAtEnd() {
        return speedAtEnd;
    }

    public void setSpeedAtEnd(double speedAtEnd) {
        this.speedAtEnd = speedAtEnd;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getFormattedStartDate() {
        return startDate != null ? Constants.dfTime.format(startDate) : "";
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getFormattedEndDate() {
        return endDate != null ? Constants.dfTime.format(endDate) : "";
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getMinHeartRate() {
        return minHeartRate;
    }

    public void setMinHeartRate(int minHeartRate) {
        this.minHeartRate = minHeartRate;
    }

    public int getMaxHeartRate() {
        return maxHeartRate;
    }

    public void setMaxHeartRate(int maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public double getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(double averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    public double getMedianHeartRate() {
        return medianHeartRate;
    }

    public void setMedianHeartRate(double medianHeartRate) {
        this.medianHeartRate = medianHeartRate;
    }

    public double getStandardDeviationHeartRate() {
        return standardDeviationHeartRate;
    }

    public void setStandardDeviationHeartRate(double standardDeviationHeartRate) {
        this.standardDeviationHeartRate = standardDeviationHeartRate;
    }

    public int getHeartRateAtStart() {
        return heartRateAtStart;
    }

    public void setHeartRateAtStart(int heartRateAtStart) {
        this.heartRateAtStart = heartRateAtStart;
    }

    public int getHeartRateAtEnd() {
        return heartRateAtEnd;
    }

    public void setHeartRateAtEnd(int heartRateAtEnd) {
        this.heartRateAtEnd = heartRateAtEnd;
    }

    public String getTimeDifference() {
        if (this.startDate != null && this.endDate != null) {
            long diff = this.endDate.getTime() - this.startDate.getTime();
            return Constants.dfTimeGMT.format(new Date(diff));
        }

        return "";
    }

    public IntervalData getPreviousIntervalData() {
        return previousIntervalData;
    }

    public void setPreviousIntervalData(IntervalData previousIntervalData) {
        this.previousIntervalData = previousIntervalData;
    }
}
