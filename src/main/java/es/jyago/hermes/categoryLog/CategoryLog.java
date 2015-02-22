/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.categoryLog;

import es.jyago.hermes.chart.ChartInterface;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.recordLog.RecordLog;
import es.jyago.hermes.csv.CSVBeanInterface;
import es.jyago.hermes.util.Constants;
import static es.jyago.hermes.util.Constants.df;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.ChartSeries;
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
    @NamedQuery(name = "CategoryLog.findAll", query = "SELECT a FROM CategoryLog a"),
    @NamedQuery(name = "CategoryLog.findByCategoryLogId", query = "SELECT a FROM CategoryLog a WHERE a.categoryLogId = :categoryLogId"),
    @NamedQuery(name = "CategoryLog.findByDate", query = "SELECT a FROM CategoryLog a WHERE a.date = :date")})
public class CategoryLog implements Serializable, CSVBeanInterface, ChartInterface {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "activity_log_id")
    private Integer categoryLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "categoryLog")
    @OrderBy("timeLog ASC")
    private Collection<RecordLog> recordLogCollection;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;

    public CategoryLog() {
    }

    public CategoryLog(Integer categoryLogId) {
        this.categoryLogId = categoryLogId;
    }

    public CategoryLog(Integer categoryLogId, Date date) {
        this.categoryLogId = categoryLogId;
        this.date = date;
    }

    public Integer getCategoryLogId() {
        return categoryLogId;
    }

    public void setCategoryLogId(Integer categoryLogId) {
        this.categoryLogId = categoryLogId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Collection<RecordLog> getRecordLogCollection() {
        return recordLogCollection;
    }

    public void setRecordLogCollection(Collection<RecordLog> recordLogCollection) {
        this.recordLogCollection = recordLogCollection;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (categoryLogId != null ? categoryLogId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CategoryLog)) {
            return false;
        }
        CategoryLog other = (CategoryLog) object;
        return !((this.categoryLogId == null && other.categoryLogId != null) || (this.categoryLogId != null && !this.categoryLogId.equals(other.categoryLogId)));
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
    public LineChartModel getLineModel(LinkedHashMap<String, Integer> values) {
        LineChartModel model = new LineChartModel();

        ChartSeries series = getSeries(values);

        model.setTitle(Constants.df.format(date));
        model.setLegendPosition("ne");
        model.setShowPointLabels(false);
        model.setDatatipFormat("%2$d");
        CategoryAxis xAxis = new CategoryAxis(ResourceBundle.getBundle("/Bundle").getString("Time"));
        xAxis.setTickAngle(-45);
        xAxis.setTickCount(800);
        model.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
        // En el eje de ordenadas, el mínimo será 0 y el máximo el máximo de pasos más un margen de 20.
        yAxis.setMin(0);
//        yAxis.setMax(maximum + 20);

        model.addSeries(series);
        model.setAnimate(true);
        model.setZoom(true);
//        model.setTitle("Zoom for Details");
//        model.getAxis(AxisType.Y).setLabel("Values");
//        DateAxis axis = new DateAxis("Dates");
//        axis.setTickAngle(-50);
//        axis.setMax("2014-02-01");
//        axis.setTickFormat("%b %#d, %y");
//        model.getAxes().put(AxisType.X, axis);

        return model;
    }

    private ChartSeries getSeries(LinkedHashMap<String, Integer> values) {
        ChartSeries series = new ChartSeries();

//        int maximum = 0;
        for (String key : values.keySet()) {
            int value = values.get(key);
//            if (value > maximum) {
//                maximum = value;
//            }
            series.set(key, value);
        }
        series.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
        
        return series;
    }

    public LineChartModel getAreaModel(LinkedHashMap<String, Integer> values) {
        LineChartModel model = new LineChartModel();
        LineChartSeries areaSeries = new LineChartSeries();
        ChartSeries lineSeries = getSeries(values);

        int restStepsThreshold = getPerson().getRestStepsThreshold();
        int restMinutes = 0;
        int endSessionStoppedMinutes = getPerson().getEndSessionStoppedMinutes();
        boolean sessionEnded = true;
        int minSessionMinutes = getPerson().getMinSessionMinutes();
        List<String> sessionTime = new ArrayList();

        for (String key : values.keySet()) {
            int value = values.get(key);

            if (value < restStepsThreshold) {
                if (sessionEnded) {
                    areaSeries.set(key, 0);
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
                    for (String time : sessionTime) {
                        areaSeries.set(time, 1);
                    }
                } else {
                    for (String time : sessionTime) {
                        areaSeries.set(time, 0);
                    }
                }
                sessionTime.clear();
            }
        }
        for (String time : sessionTime) {
            areaSeries.set(time, 0);
        }

        areaSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("Sessions"));
        areaSeries.setFill(true);

        model.setTitle(Constants.df.format(date));

        model.setLegendPosition("ne");
        model.setShowPointLabels(false);
        model.setDatatipFormat("%2$d");

        CategoryAxis xAxis = new CategoryAxis(ResourceBundle.getBundle("/Bundle").getString("Time"));
        xAxis.setTickCount(800);
        xAxis.setTickAngle(-45);

        model.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("ActiveSession"));
        yAxis.setMin(0);
        yAxis.setMax(1);
        yAxis.setTickFormat("%d");
        yAxis.setTickCount(1);

        model.addSeries(areaSeries);
//        model.addSeries(lineSeries);

        model.setAnimate(true);
        model.setZoom(true);
//        model.setTitle("Zoom for Details");
//        model.getAxis(AxisType.Y).setLabel("Values");
//        DateAxis axis = new DateAxis("Dates");
//        axis.setTickAngle(-50);
//        axis.setMax("2014-02-01");
//        axis.setTickFormat("%b %#d, %y");
//        model.getAxes().put(AxisType.X, axis);

        return model;
    }

    @Override
    public Map<String, Integer> getAggregatedValues() {
        HashMap<String, Integer> values = new HashMap();
        int remaining = getPerson().getStepsGoal();
        int achieved = 0;

        for (RecordLog recordLog : this.recordLogCollection) {
            achieved += recordLog.getSteps();
        }

        remaining -= achieved;

        if (remaining < 0) {
            remaining = 0;
        }

        values.put("Achieved", achieved);
        values.put("Remaining", remaining);

        return values;
    }

    @Override
    public LinkedHashMap<String, Integer> getValues() {
        LinkedHashMap<String, Integer> values = new LinkedHashMap();

        for (RecordLog recordLog : this.recordLogCollection) {
            values.put(Constants.dfTime.format(recordLog.getTimeLog()), recordLog.getSteps());
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
}
