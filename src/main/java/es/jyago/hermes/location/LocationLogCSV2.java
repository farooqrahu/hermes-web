/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.location;

import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author Jorge Yago
 */
public class LocationLogCSV2 extends LocationLogCSV {

    private static final long serialVersionUID = 1L;

    private int rrTime;

    public LocationLogCSV2() {
    }

    public int getRrTime() {
        return rrTime;
    }

    public void setRrTime(int rrTime) {
        this.rrTime = rrTime;
    }

    @Override
    public CellProcessor[] getProcessors() {
        return new CellProcessor[]{
            new org.supercsv.cellprocessor.constraint.NotNull(), // fecha y hora
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()), // latitud
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()), // longitud
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()), // velocidad
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt()), // pulso
            new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt()), // intervalo RR
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        };
    }

    @Override
    public String[] getFields() {
        return new String[]{
            "dateTimeLog",
            "latitude",
            "longitude",
            "speed",
            "heartRate",
            "rrTime",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null};
    }
}
