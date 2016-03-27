/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.sleepLog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import java.io.Serializable;
import java.util.Date;
import java.util.ResourceBundle;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "sleep_log", uniqueConstraints = @UniqueConstraint(columnNames = {"date_log", "person_id"}))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SleepLog.findAll", query = "SELECT s FROM SleepLog s"),
    @NamedQuery(name = "SleepLog.findBySleepLogId", query = "SELECT s FROM SleepLog s WHERE s.sleepLogId = :sleepLogId"),
    @NamedQuery(name = "SleepLog.findByDate", query = "SELECT s FROM SleepLog s WHERE s.dateLog = :date"),
    @NamedQuery(name = "SleepLog.findByMinutesAsleep", query = "SELECT s FROM SleepLog s WHERE s.minutesAsleep = :minutesAsleep"),
    @NamedQuery(name = "SleepLog.findByAwakenings", query = "SELECT s FROM SleepLog s WHERE s.awakenings = :awakenings"),
    @NamedQuery(name = "SleepLog.findByMinutesInBed", query = "SELECT s FROM SleepLog s WHERE s.minutesInBed = :minutesInBed"),
    @NamedQuery(name = "SleepLog.findByStartTime", query = "SELECT s FROM SleepLog s WHERE s.startTime = :startTime"),
    @NamedQuery(name = "SleepLog.findByEndTime", query = "SELECT s FROM SleepLog s WHERE s.endTime = :endTime"),
    @NamedQuery(name = "SleepLog.findBySent", query = "SELECT s FROM SleepLog s WHERE s.sent = :sent"),
    @NamedQuery(name = "SleepLog.findByEmailSHAAndDate", query = "SELECT s FROM SleepLog s WHERE s.person.sha = :emailSHA and s.dateLog = :date")})
public class SleepLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "sleep_log_id")
    @JsonIgnore
    private Integer sleepLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_log")
    @Temporal(TemporalType.DATE)
    private Date dateLog;
    @Basic(optional = false)
    @NotNull
    @Column(name = "minutes_asleep")
    private int minutesAsleep;
    @Basic(optional = false)
    @NotNull
    @Column(name = "awakenings")
    private int awakenings;
    @Basic(optional = false)
    @NotNull
    @Column(name = "minutes_in_bed")
    private int minutesInBed;
    @Basic(optional = false)
    @NotNull
    @Column(name = "start_time")
    @Temporal(TemporalType.TIME)
    private Date startTime;
    @Basic(optional = false)
    @NotNull
    @Column(name = "end_time")
    @Temporal(TemporalType.TIME)
    private Date endTime;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    @JsonIgnore
    private Person person;
    @Basic(optional = false)
    @Column(name = "sent")
    @JsonIgnore
    private boolean sent;

    public SleepLog() {
        this.sleepLogId = null;
        this.dateLog = null;
        this.minutesAsleep = 0;
        this.awakenings = 0;
        this.minutesInBed = 0;
        this.startTime = null;
        this.endTime = null;
        this.person = null;
        this.sent = false;
    }

    public SleepLog(Integer sleepLogId) {
        this.sleepLogId = sleepLogId;
    }

    public SleepLog(Integer sleepLogId, Date dateLog, int minutesAsleep, int awakenings, int minutesInBed, Date startTime, Date endTime) {
        this.sleepLogId = sleepLogId;
        this.dateLog = dateLog;
        this.minutesAsleep = minutesAsleep;
        this.awakenings = awakenings;
        this.minutesInBed = minutesInBed;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getSleepLogId() {
        return sleepLogId;
    }

    public void setSleepLogId(Integer sleepLogId) {
        this.sleepLogId = sleepLogId;
    }

    public Date getDateLog() {
        return dateLog;
    }

    public void setDateLog(Date dateLog) {
        this.dateLog = dateLog;
    }

    public int getMinutesAsleep() {
        return minutesAsleep;
    }

    public void setMinutesAsleep(int minutesAsleep) {
        this.minutesAsleep = minutesAsleep;
    }

    public int getAwakenings() {
        return awakenings;
    }

    public void setAwakenings(int awakenings) {
        this.awakenings = awakenings;
    }

    public int getMinutesInBed() {
        return minutesInBed;
    }

    public void setMinutesInBed(int minutesInBed) {
        this.minutesInBed = minutesInBed;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getFormattedStartTime() {
        return startTime != null ? Constants.dfTime.format(startTime) : "";
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getFormattedEndTime() {
        return endTime != null ? Constants.dfTime.format(endTime) : "";
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public BarChartModel getBarModel(String title) {
        BarChartModel model = new BarChartModel();

        ChartSeries hoursAsleep = new ChartSeries();
        hoursAsleep.set(ResourceBundle.getBundle("/Bundle").getString("HoursAsleep"), this.minutesAsleep / 60.0f);

        ChartSeries hoursInBed = new ChartSeries();
        hoursInBed.set(ResourceBundle.getBundle("/Bundle").getString("HoursInBed"), this.minutesInBed / 60.0f);

        ChartSeries totalAwakenings = new ChartSeries();
        totalAwakenings.set(ResourceBundle.getBundle("/Bundle").getString("Awakenings"), this.awakenings / 1.0f);

        // Indicamos el texto de la leyenda.
        hoursAsleep.setLabel(ResourceBundle.getBundle("/Bundle").getString("HoursAsleep"));
        hoursInBed.setLabel(ResourceBundle.getBundle("/Bundle").getString("HoursInBed"));
        totalAwakenings.setLabel(ResourceBundle.getBundle("/Bundle").getString("Awakenings"));

        model.addSeries(hoursAsleep);
        model.addSeries(hoursInBed);
        model.addSeries(totalAwakenings);

        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(true);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(true);
        model.setDatatipFormat("%2$#.1f");
        model.setSeriesColors("DDF9D9,FFE5BC,F6CDE6");
        model.setAnimate(true);
        model.setZoom(true);

        // JYFR: Extensión para gráficos. Así podemos cambiar más características. Ver las opciones en la web de 'jqPlot'.
        model.setExtender("userSleepBarChartExtender");

        return model;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(dateLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SleepLog)) {
            return false;
        }
        SleepLog other = (SleepLog) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.sleepLogId, other.sleepLogId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(Constants.dfTime.format(this.startTime))
                .append(" - ")
                .append(Constants.dfTime.format(this.endTime))
                .append(" -> ")
                .append(this.minutesAsleep)
                .append("]");

        return sb.toString();
    }

//    public static class CompDate implements Comparator<SleepLog> {
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
//        public int compare(SleepLog o1, SleepLog o2) {
//            return mode * o1.getDateLog().compareTo(o2.getDateLog());
//        }
//    }
    public boolean equalsByAttributes(SleepLog sleepLog) {
        return new EqualsBuilder().
                append(this.awakenings, sleepLog.awakenings).
                append(this.minutesAsleep, sleepLog.minutesAsleep).
                append(this.minutesInBed, sleepLog.minutesInBed).
                append(Constants.dfTime.format(this.startTime), Constants.dfTime.format(sleepLog.startTime)).
                append(Constants.dfTime.format(this.endTime), Constants.dfTime.format(sleepLog.endTime)).
                isEquals();
    }
}
