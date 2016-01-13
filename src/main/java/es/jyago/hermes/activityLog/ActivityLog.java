/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.activityLog;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.csv.ICSVBean;
import es.jyago.hermes.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.primefaces.model.chart.ChartModel;
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
@Table(name = "activity_log", uniqueConstraints = @UniqueConstraint(columnNames = {"date_log", "person_id"}))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ActivityLog.findAll", query = "SELECT a FROM ActivityLog a"),
    @NamedQuery(name = "ActivityLog.findByActivityLogId", query = "SELECT a FROM ActivityLog a WHERE a.activityLogId = :activityLogId"),
    @NamedQuery(name = "ActivityLog.findByDate", query = "SELECT a FROM ActivityLog a WHERE a.dateLog = :date"),
    @NamedQuery(name = "ActivityLog.findAllFromPerson", query = "SELECT a FROM ActivityLog a WHERE a.person.personId = :personId ORDER BY a.dateLog DESC")})
public class ActivityLog implements Serializable, ICSVBean {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "activity_log_id")
    private Integer activityLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_log")
    @Temporal(TemporalType.DATE)
    private Date dateLog;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "activityLog", orphanRemoval = true)
    @OrderBy("timeLog ASC")
    private List<StepLog> stepLogList;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;
    @Column(name = "total")
    private Integer total;
    @Column(name = "send_date")
    @Temporal(TemporalType.DATE)
    private Date sendDate;

    @Transient
    private String aggregation;
    @Transient
    private HashMap stepLogHashMap;
    @Transient
    private LinkedHashMap<Date, Integer> sessions;

    public ActivityLog() {
    }

    public Integer getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(Integer activityLogId) {
        this.activityLogId = activityLogId;
    }

    public Date getDateLog() {
        return dateLog;
    }

    public void setDateLog(Date dateLog) {
        this.dateLog = dateLog;
    }

    @XmlTransient
    public List<StepLog> getStepLogList() {
        return stepLogList;
    }

    public void setStepLogList(List<StepLog> stepLogList) {
        this.stepLogList = stepLogList;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public LinkedHashMap<Date, Integer> getSessions() {
        if (sessions == null) {
            calculateSessions(false);
        }
        return sessions;
    }

    public LinkedHashMap<Date, Integer> getActiveSessions() {
        if (sessions == null) {
            calculateSessions(true);
        }

        LinkedHashMap<Date, Integer> activeSessions = new LinkedHashMap<>();
        for (Map.Entry<Date, Integer> entry : sessions.entrySet()) {
            // El contenido será 0 si no está en una sesión y distinto de 0 si está en una sesión.
            if (entry.getValue() > -1) {
                activeSessions.put(entry.getKey(), entry.getValue());
            }
        }

        return activeSessions;
    }

    public int getSessionsTotal() {
        int result = 0;

        int previousValue = -1;
        for (int currentValue : getSessions().values()) {
            // El contenido será -1 si no está en una sesión y 1 si está en una sesión.
            // Contaremos los cambios de -1 a 1.
            if (previousValue == -1 && currentValue > 0) {
                result += 1;
            }

            previousValue = currentValue;
        }

        return result;
    }

    public int getSessionsContinuousStepsTotal() {
        int result = 0;

        if (getSessions() != null) {
            for (int value : sessions.values()) {
                result += value;
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(dateLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ActivityLog)) {
            return false;
        }
        ActivityLog other = (ActivityLog) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.activityLogId, other.activityLogId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(Constants.df.format(this.dateLog))
                .append(" -> ")
                .append(this.total)
                .append("]");

        return sb.toString();
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
    public String[] getHeaders() {
        return null;
    }

    public PieChartModel getPieModel(Map<String, Integer> values) {
        PieChartModel model = new PieChartModel();

        for (String key : values.keySet()) {
            model.set(key, values.get(key));
        }

        model.setTitle(Constants.df.format(this.dateLog));
        model.setFill(false);
        model.setShowDataLabels(true);
        model.setLegendPosition("ne");

        return model;
    }

    public LineChartModel getLineModel(String title) {
        LineChartModel model = new LineChartModel();
        LineChartSeries series = new LineChartSeries();

        // Rellenamos la serie con los tiempos y los totales de pasos.
        for (StepLog stepLog : getAggregatedValues()) {
            series.set(stepLog.getTimeLog().getTime(), stepLog.getSteps());
        }

        // Indicamos el texto de la leyenda.
        series.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));

        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(true);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(true);
        model.setDatatipFormat("%1$s -> %2$d");
        model.setSeriesColors("4C9141");
        model.setAnimate(true);
        model.setZoom(true);

//        DateAxis xAxis = new DateAxis();
//        xAxis.setTickAngle(-45);
//        xAxis.setTickFormat("%H:%M");
//        model.getAxes().put(AxisType.X, xAxis);
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setMin(0);

        if (!series.getData().isEmpty()) {
            model.addSeries(series);
        }

        // JYFR: Extensión para gráficos. Así podemos cambiar más características. Ver las opciones en la web de 'jqPlot'.
        model.setExtender("userStepsLineChartExtender");

        return model;
    }

    public LineChartModel getAreaModel(String title) {
        LineChartModel model = new LineChartModel();
        LineChartSeries areaSeries = new LineChartSeries();

        for (Map.Entry<Date, Integer> entry : getSessions().entrySet()) {
            areaSeries.set(entry.getKey().getTime(), entry.getValue());
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
        yAxis.setMin(0f);
        yAxis.setMax(1.1f);

        if (!areaSeries.getData().isEmpty()) {
            model.addSeries(areaSeries);
        }

        // JYFR: Extensión para gráficos. Así podemos cambiar más características. Ver las opciones en la web de 'jqPlot'.
        model.setExtender("customExtender");

        return model;
    }

    public LinkedHashMap<Date, Integer> calculateSessions(boolean withValues) {
        int restStepsThreshold = person.getConfigurationIntValue(Person.PersonOptions.RestStepsThreshold.name());
        int restMinutes = 0;
        int endSessionStoppedMinutes = person.getConfigurationIntValue(Person.PersonOptions.EndSessionStoppedMinutes.name());
        boolean sessionEnded = true;
        int minSessionMinutes = person.getConfigurationIntValue(Person.PersonOptions.MinimumSessionMinutes.name());

        List<Date> sessionTime = new ArrayList();
        prepareStepLogHashMap();
        sessions = new LinkedHashMap<>();

        if (stepLogList != null) {
            // Vamos procesando los tiempos.
            for (StepLog stepLog : stepLogList) {

                // Si el valor de los pasos es menor que el umbral establecido para la persona...
                if (stepLog.getSteps() < restStepsThreshold) {
                    // ... vemos si hay una sesión en curso.
                    if (sessionEnded) {
                        // Si no hay sesión en curso, añadimos 'inactividad' al contenedor de sesiones.
                        sessions.put(stepLog.getTimeLog(), -1);
                    } else {
                        // Si había una sesión en curso, añadimos un minuto más de inactividad.
                        restMinutes++;
                        // Vemos si el número de minutos de inactividad supera el número de minutos para considerar una sesión terminada.
                        if (restMinutes > endSessionStoppedMinutes) {
                            sessionEnded = true;
                            // Indicamos los minutos previos hasta la conclusión de fin de sesión, como sesión inactiva.
                            DateTime min = new DateTime(stepLog.getTimeLog());
                            min = min.minusMinutes(restMinutes);
                            for (int i = 0; i <= restMinutes; i++) {
                                sessions.put(min.toDate(), -1);
                                min = min.plusMinutes(1);
                            }
                        }
                    }
                } else {
                    // Si el valor de los pasos es mayor que el umbral, reiniciamos los minutos de inactividad y añadimos el tiempo a la lista de tiempos de sesión.
                    restMinutes = 0;
                    sessionEnded = false;
                    sessionTime.add(stepLog.getTimeLog());
                }

                // Si nos encontramos en el momento de fin de una sesión...
                if (sessionEnded == true && !sessionTime.isEmpty()) {
                    DateTime min = new DateTime(sessionTime.get(0));
                    DateTime max = new DateTime(sessionTime.get(sessionTime.size() - 1));
                    int minutes = org.joda.time.Minutes.minutesBetween(min, max).getMinutes();

                    // ... vemos si el conjunto de tiempos de sesión supera lo que se considera una sesión para la persona.
                    if (sessionTime.size() > minSessionMinutes) {
                        // Indicamos el rango como sesión activa.
                        for (int i = 0; i < minutes; i++) {
                            sessions.put(min.toDate(), withValues ? ((StepLog) stepLogHashMap.get(min.toDate())).getSteps() : 1);
                            min = min.plusMinutes(1);
                        }
                    } else {
                        // Indicamos el rango como sesión inactiva.
                        for (int i = 0; i < minutes; i++) {
                            sessions.put(min.toDate(), -1);
                            min = min.plusMinutes(1);
                        }
                    }
                    // Reiniciamos el contenedor de tiempos de sesión.
                    sessionTime.clear();
                }
            }
        }

        // Procesamos los tiempos que hayan quedado en el contenedor de tiempos de sesión.
        if (sessionTime.size() > minSessionMinutes) {
            // Indicamos el rango como sesión activa.
            for (Date time : sessionTime) {
                sessions.put(time, withValues ? ((StepLog) stepLogHashMap.get(time)).getSteps() : 1);
            }
        } else {
            // Indicamos el rango como sesión inactiva.
            for (Date time : sessionTime) {
                sessions.put(time, -1);
            }
        }

        return sessions;
    }

    public Map<String, Integer> getSummary() {
        HashMap<String, Integer> values = new HashMap();
        int remaining = getPerson().getConfigurationIntValue(Person.PersonOptions.StepsGoal.name());

        remaining -= total;

        if (remaining < 0) {
            remaining = 0;
        }

        values.put("Achieved", total);
        values.put("Remaining", remaining);

        return values;
    }

    public ChartModel getGaugeModel() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        // Por defecto, si no tiene indicado un valor de objetivo de pasos, establecemos 10000.
        int stepsGoal = getPerson().getConfigurationIntValue(Person.PersonOptions.StepsGoal.name()) > 0 ? getPerson().getConfigurationIntValue(Person.PersonOptions.StepsGoal.name()) : 10000;
        List<Number> intervals = new ArrayList<Number>() {
            {
                add(stepsGoal / 3);
                add(stepsGoal / 2);
                add(stepsGoal);
            }
        };
        int achieved = getSummary().get("Achieved");
        MeterGaugeChartModel meterGaugeModel = new MeterGaugeChartModel(achieved > stepsGoal ? stepsGoal : achieved, intervals);
        meterGaugeModel.setSeriesColors("cc6666,E7E658,66cc66");
        meterGaugeModel.setIntervalOuterRadius(30);

        return meterGaugeModel;
    }

    public List<StepLog> getAggregatedValues() {

        if (getAggregation().equals(Constants.TimeAggregations.Days.toString())) {
            // Agregación por días.
            return getStepLogByDay();
        } else if (getAggregation().equals(Constants.TimeAggregations.Hours.toString())) {
            // Agregación por horas.
            return getStepLogByHours();
        } else {
            // Agregación por minutos.
            return getStepLogList();
        }
    }

    private List<StepLog> getStepLogByDay() {
        // Se hace así para que sea homogénea la devolución de este método.
        List<StepLog> stepLogByDayList = new ArrayList<>();

        StepLog stepLog = new StepLog();
        stepLog.setTimeLog(dateLog);
        stepLog.setSteps(total);
        stepLogByDayList.add(stepLog);

        return stepLogByDayList;
    }

    private List<StepLog> getStepLogByHours() {
        List<StepLog> stepLogByHourList = new ArrayList<>();
        LocalTime localTime = new LocalTime(0, 0);

        if (stepLogList != null) {
            // Tenemos que hacerlo así porque no tenemos garantía de que estén los 1440 minutos del día.
            Iterator it = stepLogList.iterator();
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
                stepLogByHourList.add(stepLogHour);
                localTime = localTime.plusHours(1);
            }
        }

        return stepLogByHourList;
    }

    public void calculateTotal() {
        total = 0;

        for (StepLog stepLog : getStepLogList()) {
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    private void prepareStepLogHashMap() {
        this.stepLogHashMap = new LinkedHashMap<>();

        if (stepLogList != null) {
            for (StepLog s : stepLogList) {
                if (s.getTimeLog() != null) {
                    stepLogHashMap.put(s.getTimeLog(), s);
                }
            }
        }
    }

//    public static class CompDate implements Comparator<ActivityLog> {
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
//        public int compare(ActivityLog o1, ActivityLog o2) {
//            return mode * o1.getDateLog().compareTo(o2.getDateLog());
//        }
//    }
}
