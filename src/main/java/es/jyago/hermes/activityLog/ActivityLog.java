/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.activityLog;

import es.jyago.hermes.chart.LineChartInterface;
import es.jyago.hermes.chart.PieChartInterface;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.csv.CSVBeanInterface;
import es.jyago.hermes.util.Constants;
import static es.jyago.hermes.util.Constants.df;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.MeterGaugeChartModel;
import org.primefaces.model.chart.PieChartModel;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "activity_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ActivityLog.findAll", query = "SELECT a FROM ActivityLog a"),
    @NamedQuery(name = "ActivityLog.findByActivityLogId", query = "SELECT a FROM ActivityLog a WHERE a.activityLogId = :activityLogId"),
    @NamedQuery(name = "ActivityLog.findByDate", query = "SELECT a FROM ActivityLog a WHERE a.date = :date"),
    @NamedQuery(name = "ActivityLog.findAllFromPerson", query = "SELECT a FROM ActivityLog a WHERE a.person.personId = :personId ORDER BY a.date DESC")})
public class ActivityLog implements Serializable, CSVBeanInterface, PieChartInterface, LineChartInterface {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "activity_log_id")
    private Integer activityLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "activityLog", orphanRemoval = true)
    @OrderBy("timeLog ASC")
    private List<StepLog> stepLogCollection;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;
    @Column(name = "total")
    private int total;
    
    @Transient
    private String aggregation;
    
    public ActivityLog() {
    }
    
    public ActivityLog(Integer activityLogId) {
        this.activityLogId = activityLogId;
    }
    
    public ActivityLog(Integer activityLogId, Date date) {
        this.activityLogId = activityLogId;
        this.date = date;
    }
    
    public Integer getActivityLogId() {
        return activityLogId;
    }
    
    public void setActivityLogId(Integer activityLogId) {
        this.activityLogId = activityLogId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public List<StepLog> getStepLogCollection() {
        return stepLogCollection;
    }
    
    public void setStepLogCollection(List<StepLog> stepLogCollection) {
        this.stepLogCollection = stepLogCollection;
    }
    
    public Person getPerson() {
        return person;
    }
    
    public void setPerson(Person person) {
        this.person = person;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (date != null ? date.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ActivityLog)) {
            return false;
        }
        ActivityLog other = (ActivityLog) object;

        // Dos 'ActivityLog' serán iguales si tienen la misma fecha y son de la misma persona.
        String thisDate = Constants.df.format(this.date);
        String otherDate = Constants.df.format(other.date);
        
        return (thisDate.equals(otherDate) && this.person.equals(other.person));
    }
    
    @Override
    public String toString() {
        return df.format(date);
    }
    
    @Override
    public CellProcessor[] getProcessors() {
        return new CellProcessor[]{
            new org.supercsv.cellprocessor.constraint.NotNull() // fecha
        };
    }
    
    @Override
    public String[] getFields() {
        return new String[]{"date"};
    }
    
    @Override
    public PieChartModel getPieModel(Map<String, Integer> values) {
        PieChartModel model = new PieChartModel();
        
        for (String key : values.keySet()) {
            model.set(key, values.get(key));
        }
        
        model.setTitle(Constants.df.format(this.date));
        model.setFill(false);
        model.setShowDataLabels(true);
        model.setLegendPosition("ne");
        
        return model;
    }
    
    @Override
    public LineChartModel getLineModel(LinkedHashMap<Date, Integer> values, String title) {
        LineChartModel model = new LineChartModel();
        LineChartSeries series = new LineChartSeries();

        // Rellenamos la serie con los tiempos y los totales de pasos.
        for (Date key : values.keySet()) {
            int value = values.get(key);
            series.set(key.getTime(), value);
        }

        // Indicamos el texto de la leyenda.
        series.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
        
        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(true);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(true);
        model.setDatatipFormat("%1$s -> %2$d");
        model.setSeriesColors("779ECB");
        model.setAnimate(true);
        model.setZoom(true);
        
        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Time"));
        xAxis.setTickAngle(-45);
        xAxis.setTickFormat("%H:%M");
        model.getAxes().put(AxisType.X, xAxis);
        
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
        yAxis.setMin(0);
        
        model.addSeries(series);
        
        return model;
    }
    
    public LineChartModel getAreaModel(LinkedHashMap<Date, Integer> values, String title) {
        LineChartModel model = new LineChartModel();
        LineChartSeries areaSeries = new LineChartSeries();
        
        int restStepsThreshold = getPerson().getRestStepsThreshold();
        int restMinutes = 0;
        int endSessionStoppedMinutes = getPerson().getEndSessionStoppedMinutes();
        boolean sessionEnded = true;
        int minSessionMinutes = getPerson().getMinSessionMinutes();
        List<Date> sessionTime = new ArrayList();
        
        for (Date key : values.keySet()) {
            int value = values.get(key);
            
            if (value < restStepsThreshold) {
                if (sessionEnded) {
                    areaSeries.set(key.getTime(), 0);
                } else {
                    restMinutes++;
                    if (restMinutes > endSessionStoppedMinutes) {
                        sessionEnded = true;
                    }
                }
            } else {
                restMinutes = 0;
                sessionEnded = false;
                sessionTime.add(key);
            }
            
            if (sessionEnded == true) {
                if (sessionTime.size() > minSessionMinutes) {
                    for (Date time : sessionTime) {
                        areaSeries.set(time.getTime(), 1);
                    }
                } else {
                    for (Date time : sessionTime) {
                        areaSeries.set(time.getTime(), 0);
                    }
                }
                sessionTime.clear();
            }
        }
        for (Date time : sessionTime) {
            areaSeries.set(time.getTime(), 0);
        }
        
        areaSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("Sessions"));
        areaSeries.setFill(true);
        
        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(false);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(true);
        model.setDatatipFormat("%1$s -> %2$d");
        model.setSeriesColors("CB99C9");
        model.setAnimate(true);
        model.setZoom(true);
        
        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Time"));
        xAxis.setTickAngle(-45);
        xAxis.setTickFormat("%H:%M");
        model.getAxes().put(AxisType.X, xAxis);
        
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("ActiveSession"));
        yAxis.setMin(0);
        yAxis.setMax(1);
        yAxis.setTickFormat("%d");
        yAxis.setTickCount(1);
        
        model.addSeries(areaSeries);
        
        return model;
    }
    
    public Map<String, Integer> getAggregatedValues() {
        HashMap<String, Integer> values = new HashMap();
        int remaining = getPerson().getStepsGoal();
        
        remaining -= total;
        
        if (remaining < 0) {
            remaining = 0;
        }
        
        values.put("Achieved", total);
        values.put("Remaining", remaining);
        
        return values;
    }
    
    public LinkedHashMap<Date, Integer> getValues() {
        LinkedHashMap<Date, Integer> values = new LinkedHashMap();
        
        for (StepLog stepLog : this.stepLogCollection) {
            values.put(stepLog.getTimeLog(), stepLog.getSteps());
        }
        
        return values;
    }
    
    public ChartModel getGaugeModel() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        // Por defecto, si no tiene indicado un valor de objetivo de pasos, establecemos 10000.
        int stepsGoal = getPerson().getStepsGoal() > 0 ? getPerson().getStepsGoal() : 10000;
        List<Number> intervals = new ArrayList<Number>() {
            {
                add(stepsGoal / 3);
                add(stepsGoal / 2);
                add(stepsGoal);
            }
        };
        int achieved = getAggregatedValues().get("Achieved");
        MeterGaugeChartModel meterGaugeModel = new MeterGaugeChartModel(achieved > stepsGoal ? stepsGoal : achieved, intervals);
        meterGaugeModel.setSeriesColors("cc6666,E7E658,66cc66");
        meterGaugeModel.setIntervalOuterRadius(30);
        
        return meterGaugeModel;
    }
    
    public Collection<StepLog> getAggregatedStepCollection() {
        
        if (getAggregation().equals(Constants.TimeAggregations.Days.toString())) {
            // Agregación por días.
            // Se hace así para que sea homogénea la devolución de este método.
            Collection<StepLog> stepLogCollectionByDay = new LinkedHashSet<>();
            StepLog stepLog = new StepLog();
            stepLog.setSteps(total);
            stepLogCollectionByDay.add(stepLog);
            return stepLogCollectionByDay;
        } else if (getAggregation().equals(Constants.TimeAggregations.Hours.toString())) {
            // Agregación por horas.
            return getStepLogCollectionByHours();
        } else {
            // Agregación por minutos.
            return getStepLogCollection();
        }
    }
    
    private Collection<StepLog> getStepLogCollectionByHours() {
        Collection<StepLog> stepLogCollectionByHour = new ArrayList<>();
        LocalTime localTime = new LocalTime(0, 0);

        // Tenemos que hacerlo así porque no tenemos garantía de que estén los 1440 minutos del día.
        Iterator it = stepLogCollection.iterator();
        int amount = 0;
        
        for (int hour = 0; hour < 24; hour++) {
            
            StepLog stepLogHour = new StepLog();
            stepLogHour.setTimeLog(localTime.toDateTimeToday().toDate());
            
            while (it.hasNext()) {
                StepLog stepLog = (StepLog) it.next();
                DateTime time = new DateTime(stepLog.getTimeLog());
                if (time.getHourOfDay() == localTime.getHourOfDay()) {
                    amount += stepLog.getSteps();
                } else {
                    stepLogHour.setSteps(amount);
                    amount = stepLog.getSteps();
                    break;
                }
            }
            stepLogCollectionByHour.add(stepLogHour);
            localTime = localTime.plusHours(1);
        }
        
        return stepLogCollectionByHour;
    }
    
    public void calculateTotal() {
        total = 0;
        
        for (StepLog stepLog : getStepLogCollection()) {
            total += stepLog.getSteps();
        }
    }
    
    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }
    
    public String getAggregation() {
        if (aggregation == null) {
            // La agregación por defecto será por 'Minutos', que es como se almacena en la B.D.
            aggregation = Constants.TimeAggregations.Minutes.toString();
        }
        return aggregation;
    }
    
}
