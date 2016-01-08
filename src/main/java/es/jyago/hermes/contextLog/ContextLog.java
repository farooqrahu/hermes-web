/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.contextLog;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.Util;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "context_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ContextLog.findAll", query = "SELECT c FROM ContextLog c"),
    @NamedQuery(name = "ContextLog.findByContextLogId", query = "SELECT c FROM ContextLog c WHERE c.contextLogId = :contextLogId"),
    @NamedQuery(name = "ContextLog.findByDateLog", query = "SELECT c FROM ContextLog c WHERE c.dateLog = :dateLog"),
    @NamedQuery(name = "ContextLog.findByPersonAndDateLog", query = "SELECT c FROM ContextLog c WHERE c.person.personId = :personId AND c.dateLog = :dateLog")})
public class ContextLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "context_log_id")
    private Integer contextLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_log")
    @Temporal(TemporalType.DATE)
    private Date dateLog;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contextLog", orphanRemoval = true)
    @OrderBy("timeLog ASC")
    private List<ContextLogDetail> contextLogDetailList;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;
    @Basic(optional = false)
    @NotNull
    @Size(max = 20)
    @Column(name = "device_id")
    private String deviceId;

    public ContextLog() {
    }

    public ContextLog(Integer contextLogId) {
        this.contextLogId = contextLogId;
    }

    public ContextLog(Integer contextLogId, Date dateLog) {
        this.contextLogId = contextLogId;
        this.dateLog = dateLog;
    }

    public Integer getContextLogId() {
        return contextLogId;
    }

    public void setContextLogId(Integer contextLogId) {
        this.contextLogId = contextLogId;
    }

    public Date getDateLog() {
        return dateLog;
    }

    public void setDateLog(Date dateLog) {
        this.dateLog = dateLog;
    }

    @XmlTransient
    public List<ContextLogDetail> getContextLogDetailList() {
        return contextLogDetailList;
    }

    public void setContextLogDetailList(List<ContextLogDetail> contextLogDetailList) {
        this.contextLogDetailList = contextLogDetailList;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(dateLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ContextLog)) {
            return false;
        }
        ContextLog other = (ContextLog) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.contextLogId, other.contextLogId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(Constants.df.format(this.dateLog))
                .append(" -> ")
                .append(this.deviceId)
                .append("]");

        return sb.toString();
    }

    public PieChartModel getActivitiesPieModel() {
        PieChartModel model = new PieChartModel();

        // Obtenemos los totales por actividad.
        Map<String, Number> values = getActivitiesMap();

        model.setData(values);
        model.setShowDataLabels(true);
        model.setLegendPosition("nw");
        model.setSliceMargin(3);
        model.setShadow(false);
        model.setExtender("pieExtender");

        return model;
    }

    public Map<String, Number> getActivitiesMap() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        Map<String, Integer> tempValues = new HashMap<>();

        int total = 0;
        for (ContextLogDetail cld : contextLogDetailList) {
            String key = bundle.getString("Context_" + cld.getDetectedActivity());
            Integer value = tempValues.get(key);
            value = value != null ? value + 1 : 1;
            tempValues.put(key, value);
            total++;
        }

        Map<String, Number> values = new HashMap<>();
        // Calculamos los porcentajes y ponemos los tiempos de cada actividad.
        for (String key : tempValues.keySet()) {
            int minutes = (int) tempValues.get(key);
            values.put(key + " (" + Util.minutesToTimeString(minutes) + ")", (int) Math.round(minutes * 100.0 / total));
        }

        return values;
    }
}
