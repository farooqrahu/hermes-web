/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.location;

import es.jyago.hermes.csv.ICSVBean;
import java.io.Serializable;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author Jorge Yago
 */
public class LocationLogCSV implements Serializable, ICSVBean {

    private static final long serialVersionUID = 1L;

    private String dateTimeLog;
    private double latitude;
    private double longitude;
    private double speed;
    private int heartRate;

    public LocationLogCSV() {
    }

    public String getDateTimeLog() {
        return dateTimeLog;
    }

    public void setDateTimeLog(String dateTimeLog) {
        this.dateTimeLog = dateTimeLog;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    @Override
    public CellProcessor[] getProcessors() {
        return new CellProcessor[]{
            new org.supercsv.cellprocessor.constraint.NotNull(), // fecha y hora
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()), // latitud
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()), // longitud
            null,
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()), // velocidad
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt()), // pulso
            null,
            null,
            null
        };
    }

    @Override
    public String[] getFields() {
        return new String[]{"dateTimeLog", "latitude", "longitude", null, "speed", "heartRate", null, null, null};
    }

}
