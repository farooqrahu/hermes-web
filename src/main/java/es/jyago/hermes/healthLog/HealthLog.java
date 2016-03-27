/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.healthLog;

import es.jyago.hermes.heartLog.HeartLog;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "health_log", uniqueConstraints = @UniqueConstraint(columnNames = {"date_log", "person_id"}))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "HealthLog.findAll", query = "SELECT h FROM HealthLog h"),
    @NamedQuery(name = "HealthLog.findByHealthLogId", query = "SELECT h FROM HealthLog h WHERE h.healthLogId = :healthLogId"),
    @NamedQuery(name = "HealthLog.findByDateLog", query = "SELECT h FROM HealthLog h WHERE h.dateLog = :dateLog"),
    @NamedQuery(name = "HealthLog.findBySent", query = "SELECT h FROM HealthLog h WHERE h.sent = :sent"),
    @NamedQuery(name = "HealthLog.findByAverage", query = "SELECT h FROM HealthLog h WHERE h.average = :average")})
public class HealthLog implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "healthLog", orphanRemoval = true)
    @OrderBy("timeLog ASC")
    private List<HeartLog> heartLogList;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "health_log_id")
    private Integer healthLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_log")
    @Temporal(TemporalType.DATE)
    private Date dateLog;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "average")
    private Integer average;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;

    @Basic(optional = false)
    @Column(name = "sent")
    private boolean sent;

    @Transient
    private String aggregation;
    @Transient
    private HashMap heartLogHashMap;
    @Transient
    private int maximumHeartRate;
    @Transient
    private int minimumHeartRate;

    public HealthLog() {
    }

    public HealthLog(Integer healthLogId) {
        this.healthLogId = healthLogId;
    }

    public HealthLog(Integer healthLogId, Date dateLog) {
        this.healthLogId = healthLogId;
        this.dateLog = dateLog;
    }

    // JYFR: Método que será invocado automáticamente tras cargar los datos de la base de datos y de ser inyectados en los atributos correspondientes.
    @PostLoad
    private void init() {
        findMaxMinValues();
    }

    public Integer getHealthLogId() {
        return healthLogId;
    }

    public void setHealthLogId(Integer healthLogId) {
        this.healthLogId = healthLogId;
    }

    public Date getDateLog() {
        return dateLog;
    }

    public void setDateLog(Date dateLog) {
        this.dateLog = dateLog;
    }

    public Integer getAverage() {
        return average;
    }

    public void setAverage(Integer average) {
        this.average = average;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(dateLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HealthLog)) {
            return false;
        }
        HealthLog other = (HealthLog) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.healthLogId, other.healthLogId).
                isEquals();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(this.getDateLog())
                .append(" -> ")
                .append(this.getAverage())
                .append("]");

        return sb.toString();
    }

    @XmlTransient
    public List<HeartLog> getHeartLogList() {
        return heartLogList;
    }

    public void setHeartLogList(List<HeartLog> heartLogList) {
        this.heartLogList = heartLogList;
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

    public Collection<HeartLog> getAggregatedHeartRateCollection() {

        if (getAggregation().equals(Constants.TimeAggregations.Days.toString())) {
            // Agregación por días.
            // Se hace así para que sea homogénea la devolución de este método.
            Collection<HeartLog> heartRateCollection = new LinkedHashSet<>();
            HeartLog heartLog = new HeartLog();
            heartLog.setRate(average);
            heartRateCollection.add(heartLog);
            return heartRateCollection;
        } else if (getAggregation().equals(Constants.TimeAggregations.Hours.toString())) {
            // Agregación por horas.
            return getHeartLogCollectionByHours();
        } else {
            // Agregación por minutos.
            return getHeartLogList();
        }
    }

    private Collection<HeartLog> getHeartLogCollectionByHours() {
        Collection<HeartLog> heartLogCollectionByHour = new ArrayList<>();
        LocalTime localTime = new LocalTime(0, 0);

        // Tenemos que hacerlo así porque no tenemos garantía de que estén los 1440 minutos del día.
        Iterator it = heartLogList.iterator();
        int amount = 0;

        for (int hour = 0; hour < 24; hour++) {

            HeartLog heartLogHour = new HeartLog();
            heartLogHour.setTimeLog(localTime.toDateTimeToday().toDate());

            int count = 0;
            while (it.hasNext()) {
                HeartLog heartLog = (HeartLog) it.next();
                DateTime time = new DateTime(heartLog.getTimeLog());
                if (time.getHourOfDay() == localTime.getHourOfDay()) {
                    amount += heartLog.getRate();
                    count++;
                } else {
                    heartLogHour.setRate(amount / count);
                    count = 0;
                    amount = heartLog.getRate();
                    break;
                }
            }
            heartLogCollectionByHour.add(heartLogHour);
            localTime = localTime.plusHours(1);
        }

        return heartLogCollectionByHour;
    }

    public LineChartModel getLineModel(String title) {
        LineChartModel model = new LineChartModel();
        LineChartSeries series = new LineChartSeries();

        // Rellenamos la serie con los tiempos y el pulso.
        for (HeartLog heartLog : getAggregatedValues()) {
            series.set(heartLog.getTimeLog().getTime(), heartLog.getRate());
        }

        // Indicamos el texto de la leyenda.
        series.setLabel(ResourceBundle.getBundle("/Bundle").getString("HeartRate"));

        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(true);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(true);
        model.setDatatipFormat("%1$s -> %2$d");
        model.setSeriesColors("FF392E");
        model.setAnimate(true);
        model.setZoom(true);

        DateAxis xAxis = new DateAxis();
        xAxis.setTickAngle(-45);
        xAxis.setTickFormat("%H:%M");
        model.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setMin(0);

        if (!series.getData().isEmpty()) {
            model.addSeries(series);
        }

        return model;
    }

    public List<HeartLog> getAggregatedValues() {

        if (getAggregation().equals(Constants.TimeAggregations.Days.toString())) {
            // Agregación por días.
            return getHeartLogByDay();
        } else if (getAggregation().equals(Constants.TimeAggregations.Hours.toString())) {
            // Agregación por horas.
            return getHeartLogByHours();
        } else {
            // Agregación por minutos.
            return getHeartLogList();
        }
    }

    private List<HeartLog> getHeartLogByDay() {
        // Se hace así para que sea homogénea la devolución de este método.
        List<HeartLog> heartLogByDayList = new ArrayList<>();

        HeartLog heartLog = new HeartLog();
        heartLog.setTimeLog(dateLog);
        heartLog.setRate(average);
        heartLogByDayList.add(heartLog);

        return heartLogByDayList;
    }

    private List<HeartLog> getHeartLogByHours() {
        List<HeartLog> heartLogByHourList = new ArrayList<>();
        LocalTime localTime = new LocalTime(0, 0);

        if (heartLogList != null) {
            // Tenemos que hacerlo así porque no tenemos garantía de que estén los 1440 minutos del día.
            Iterator it = heartLogList.iterator();
            int amount = 0;

            for (int hour = 0; hour < 24; hour++) {

                HeartLog heartLogHour = new HeartLog();
                heartLogHour.setTimeLog(localTime.toDateTimeToday().toDate());

                int count = 0;
                while (it.hasNext()) {
                    HeartLog heartLog = (HeartLog) it.next();
                    DateTime time = new DateTime(heartLog.getTimeLog());
                    if (time.getHourOfDay() == localTime.getHourOfDay()) {
                        amount += heartLog.getRate();
                        count++;
                    } else {
                        heartLogHour.setRate(count > 0 ? (amount / count) : 0);
                        amount = heartLog.getRate();
                        count = 0;
                        break;
                    }
                }

                heartLogByHourList.add(heartLogHour);
                localTime = localTime.plusHours(1);
            }
        }

        return heartLogByHourList;
    }

    private void prepareHeartLogHashMap() {
        this.heartLogHashMap = new LinkedHashMap<>();

        if (heartLogList != null) {
            for (HeartLog h : heartLogList) {
                if (h.getTimeLog() != null) {
                    heartLogHashMap.put(h.getTimeLog(), h);
                }
            }
        }
    }

    public int getMaximumHeartRate() {
        return maximumHeartRate;
    }

    public void setMaximumHeartRate(int maximumHeartRate) {
        this.maximumHeartRate = maximumHeartRate;
    }

    public int getMinimumHeartRate() {
        return minimumHeartRate;
    }

    public void setMinimumHeartRate(int minimumHeartRate) {
        this.minimumHeartRate = minimumHeartRate;
    }

    private void findMaxMinValues() {

        if (heartLogList != null && !heartLogList.isEmpty()) {
            maximumHeartRate = heartLogList.get(0).getRate();
            minimumHeartRate = heartLogList.get(0).getRate();

            for (HeartLog heartLog : heartLogList) {
                if (heartLog.getRate() > maximumHeartRate) {
                    maximumHeartRate = heartLog.getRate();
                }
                if (heartLog.getRate() < minimumHeartRate) {
                    minimumHeartRate = heartLog.getRate();
                }
            }
        }
    }

//    public static class CompDate implements Comparator<HealthLog> {
//
//        private int mode = 1;
//
//        public CompDate(boolean desc) {
//            if (desc) {
//                mode = -1;
//            }
//        }
//
//        @Override
//        public int compare(HealthLog o1, HealthLog o2) {
//            return mode * o1.getDateLog().compareTo(o2.getDateLog());
//        }
//    }
    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
