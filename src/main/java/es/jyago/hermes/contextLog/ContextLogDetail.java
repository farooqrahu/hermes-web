/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.contextLog;

import es.jyago.hermes.util.Constants;
import java.io.Serializable;
import java.util.Date;
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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "context_log_detail")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ContextLogDetail.findAll", query = "SELECT c FROM ContextLogDetail c"),
    @NamedQuery(name = "ContextLogDetail.findByContextLogDetailId", query = "SELECT c FROM ContextLogDetail c WHERE c.contextLogDetailId = :contextLogDetailId"),
    @NamedQuery(name = "ContextLogDetail.findByTimeLog", query = "SELECT c FROM ContextLogDetail c WHERE c.timeLog = :timeLog"),
    @NamedQuery(name = "ContextLogDetail.findByLatitude", query = "SELECT c FROM ContextLogDetail c WHERE c.latitude = :latitude"),
    @NamedQuery(name = "ContextLogDetail.findByLongitude", query = "SELECT c FROM ContextLogDetail c WHERE c.longitude = :longitude"),
    @NamedQuery(name = "ContextLogDetail.findByDetectedActivity", query = "SELECT c FROM ContextLogDetail c WHERE c.detectedActivity = :detectedActivity")})
public class ContextLogDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "context_log_detail_id")
    private Integer contextLogDetailId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "time_log")
    @Temporal(TemporalType.TIME)
    private Date timeLog;
    @Basic(optional = true)
    @Column(name = "latitude")
    private Double latitude;
    @Basic(optional = true)
    @Column(name = "longitude")
    private Double longitude;
    @Basic(optional = true)
    @Column(name = "accuracy")
    private Float accuracy;
    @Basic(optional = true)
    @Column(name = "detected_activity")
    private String detectedActivity;
    @Basic(optional = false)
    @Column(name = "sent")
    private boolean sent;
    @JoinColumn(name = "context_log_id", referencedColumnName = "context_log_id")
    @ManyToOne(optional = false)
    private ContextLog contextLog;

    public ContextLogDetail() {
    }

    public ContextLogDetail(Integer contextLogDetailId) {
        this.contextLogDetailId = contextLogDetailId;
    }

    public Integer getContextLogDetailId() {
        return contextLogDetailId;
    }

    public void setContextLogDetailId(Integer contextLogDetailId) {
        this.contextLogDetailId = contextLogDetailId;
    }

    public Date getTimeLog() {
        return timeLog;
    }

    public void setTimeLog(Date timeLog) {
        this.timeLog = timeLog;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public String getDetectedActivity() {
        return detectedActivity;
    }

    public void setDetectedActivity(String detectedActivity) {
        this.detectedActivity = detectedActivity;
    }

    public ContextLog getContextLog() {
        return contextLog;
    }

    public void setContextLog(ContextLog contextLog) {
        this.contextLog = contextLog;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(timeLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ContextLogDetail)) {
            return false;
        }
        ContextLogDetail other = (ContextLogDetail) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.contextLogDetailId, other.contextLogDetailId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(Constants.df.format(this.timeLog))
                .append(" -> ")
                .append(this.detectedActivity)
                .append("]");

        return sb.toString();
    }

}
