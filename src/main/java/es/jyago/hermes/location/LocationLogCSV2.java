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

    @Override
    public void init(Integer columns) {
        cellProcessors = new CellProcessor[columns];
        cellProcessors[0] = new org.supercsv.cellprocessor.constraint.NotNull(); // fecha y hora
        cellProcessors[1] = new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()); // latitud
        cellProcessors[2] = new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()); // longitud
        cellProcessors[3] = new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble()); // velocidad
        cellProcessors[4] = new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt()); // pulso
        cellProcessors[5] = new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt()); // intervalo RR

        fields = new String[columns];
        fields[0] = "dateTimeLog";
        fields[1] = "latitude";
        fields[2] = "longitude";
        fields[3] = "speed";
        fields[4] = "heartRate";
        fields[5] = "rrTime";
    }

    public int getRrTime() {
        return rrTime;
    }

    public void setRrTime(int rrTime) {
        this.rrTime = rrTime;
    }

    @Override
    public CellProcessor[] getProcessors() {
        return cellProcessors;
    }

    @Override
    public String[] getFields() {
        return fields;
    }
}
