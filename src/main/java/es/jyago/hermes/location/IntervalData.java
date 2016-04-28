package es.jyago.hermes.location;

import es.jyago.hermes.csv.ICSVBean;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;


public class IntervalData implements Serializable, ICSVBean {

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
    private double averageAcceleration;
    private double averageDeceleration;

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
    private double stress;
    private int minRRTime;
    private int maxRRTime;
    private double averageRRTime;

    // Tramo previo. Servirá para analizar la tendencia.
    private IntervalData previousIntervalData;

    // Datos de sueño
    private SleepLog sleepLog;

    protected CellProcessor[] cellProcessors;
    protected String[] fields;
    protected String[] headers;

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
        averageAcceleration = 0.0d;
        averageDeceleration = 0.0d;

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
        minRRTime = 0;
        maxRRTime = 0;
        averageRRTime = 0.0d;

        stress = 0.0d;

        previousIntervalData = null;

        sleepLog = null;
    }

    @Override
    public void init(Integer columns) {
        // Si el número de columnas es 'null', se establecen las que se definan internamente en este método.

        List<CellProcessor> cpl = new ArrayList();

        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt())); // número de intervalo
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull()); // hora de inicio
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull()); // hora de final
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull()); // diferencia de tiempo
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // velocidad al inicio
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // velocidad al final
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // velocidad mínima
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // velocidad máxima
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // velocidad media
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // mediana de la velocidad
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // desviación típica de la velocidad
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // aceleración
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // deceleración
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // pke
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt())); // pulso al inicio
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt())); // pulso al final
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt())); // pulso mínimo
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt())); // pulso máximo
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // pulso media
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // mediana del pulso
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // desviación típica del pulso
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseInt())); // intervalo RR mínimo
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseInt())); // intervalo RR máximo
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseDouble(new FmtNumber("0.00")))); // intervalo RR medio
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // estrés
        cpl.add(new org.supercsv.cellprocessor.Optional()); // hora de inicio del sueño
        cpl.add(new org.supercsv.cellprocessor.Optional()); // hora de fin del sueño
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseInt())); // minutos en la cama
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseInt())); // minutos durmiendo
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseInt())); // despertares
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // longitud del tramo
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseDouble(new FmtNumber("0.00")))); // longitud acumulada

        if (columns != null) {
            cellProcessors = new CellProcessor[columns];
            for (int i = 0; i < columns; i++) {
                cellProcessors[i] = cpl.get(i);
            }
        } else {
            cellProcessors = cpl.toArray(new CellProcessor[cpl.size()]);
        }

        List<String> f = new ArrayList();

        f.add("intervalId");
        f.add("formattedStartDate");
        f.add("formattedEndDate");
        f.add("timeDifference");
        f.add("speedAtStart");
        f.add("speedAtEnd");
        f.add("minSpeed");
        f.add("maxSpeed");
        f.add("averageSpeed");
        f.add("medianSpeed");
        f.add("standardDeviationSpeed");
        f.add("averageAcceleration");
        f.add("averageDeceleration");
        f.add("pke");
        f.add("heartRateAtStart");
        f.add("heartRateAtEnd");
        f.add("minHeartRate");
        f.add("maxHeartRate");
        f.add("averageHeartRate");
        f.add("medianHeartRate");
        f.add("standardDeviationHeartRate");
        f.add("minRRTime");
        f.add("maxRRTime");
        f.add("averageRRTime");
        f.add("stress");
        f.add("sleepLogFormattedStartTime");
        f.add("sleepLogFormattedEndTime");
        f.add("sleepLogMinutesInBed");
        f.add("sleepLogMinutesAsleep");
        f.add("sleepLogAwakenings");
        f.add("length");
        f.add("cummulativeLength");

        if (columns != null) {
            fields = new String[columns];
            for (int i = 0; i < columns; i++) {
                fields[i] = f.get(i);
            }
        } else {
            fields = f.toArray(new String[f.size()]);
        }

        List<String> h = new ArrayList();

        h.add("I");
        h.add("T0");
        h.add("T1");
        h.add("T1-T0");
        h.add("S0");
        h.add("S1");
        h.add("MinS");
        h.add("MaxS");
        h.add("AvS");
        h.add("MedS");
        h.add("StdDevS");
        h.add("AvACC");
        h.add("AvDEC");
        h.add("PKE");
        h.add("HR0");
        h.add("HR1");
        h.add("MinHR");
        h.add("MaxHR");
        h.add("AvHR");
        h.add("MedHR");
        h.add("StdDevHR");
        h.add("MinRR");
        h.add("MaxRR");
        h.add("AvRR");
        h.add("Stress");
        h.add("SL0");
        h.add("SL1");
        h.add("MB");
        h.add("MA");
        h.add("AW");
        h.add("L");
        h.add("SumL");

        if (columns != null) {
            headers = new String[columns];
            for (int i = 0; i < columns; i++) {
                headers[i] = h.get(i);
            }
        } else {
            headers = h.toArray(new String[h.size()]);
        }
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

    public double getStress() {
        return stress;
    }

    public void setStress(double stress) {
        this.stress = stress;
    }

    public double getAverageAcceleration() {
        return averageAcceleration;
    }

    public void setAverageAcceleration(double averageAcceleration) {
        this.averageAcceleration = averageAcceleration;
    }

    public double getAverageDeceleration() {
        return averageDeceleration;
    }

    public void setAverageDeceleration(double averageDeceleration) {
        this.averageDeceleration = averageDeceleration;
    }

    public int getMinRRTime() {
        return minRRTime;
    }

    public void setMinRRTime(int minRRTime) {
        this.minRRTime = minRRTime;
    }

    public int getMaxRRTime() {
        return maxRRTime;
    }

    public void setMaxRRTime(int maxRRTime) {
        this.maxRRTime = maxRRTime;
    }

    public double getAverageRRTime() {
        return averageRRTime;
    }

    public void setAverageRRTime(double averageRRTime) {
        this.averageRRTime = averageRRTime;
    }

    public SleepLog getSleepLog() {
        return sleepLog;
    }

    public void setSleepLog(SleepLog sleepLog) {
        this.sleepLog = sleepLog;
    }

    public String getSleepLogFormattedStartTime() {
        return sleepLog != null ? sleepLog.getFormattedStartTime() : "";
    }

    public String getSleepLogFormattedEndTime() {
        return sleepLog != null ? sleepLog.getFormattedEndTime() : "";
    }

    public Integer getSleepLogMinutesInBed() {
        return sleepLog != null ? sleepLog.getMinutesInBed() : null;
    }

    public Integer getSleepLogMinutesAsleep() {
        return sleepLog != null ? sleepLog.getMinutesAsleep() : null;
    }

    public Integer getSleepLogAwakenings() {
        return sleepLog != null ? sleepLog.getAwakenings() : null;
    }

    public CellProcessor[] getCellProcessors() {
        return cellProcessors;
    }

    public void setCellProcessors(CellProcessor[] cellProcessors) {
        this.cellProcessors = cellProcessors;
    }

    @Override
    public CellProcessor[] getProcessors() {
        return cellProcessors;
    }

    @Override
    public String[] getFields() {
        return fields;
    }

    @Override
    public String[] getHeaders() {
        return headers;
    }
}
