package es.jyago.hermes.heartLog;

import es.jyago.hermes.healthLog.HealthLog;
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


@Entity
@Table(name = "heart_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "HeartLog.findAll", query = "SELECT h FROM HeartLog h"),
    @NamedQuery(name = "HeartLog.findByHeartLogId", query = "SELECT h FROM HeartLog h WHERE h.heartLogId = :heartLogId"),
    @NamedQuery(name = "HeartLog.findByTimeLog", query = "SELECT h FROM HeartLog h WHERE h.timeLog = :timeLog"),
    @NamedQuery(name = "HeartLog.findByRate", query = "SELECT h FROM HeartLog h WHERE h.rate = :rate")})
public class HeartLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "heart_log_id")
    private Integer heartLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "time_log")
    @Temporal(TemporalType.TIME)
    private Date timeLog;
    @Basic(optional = false)
    @NotNull
    @Column(name = "rate")
    private int rate;
    @Basic(optional = false)
    @Column(name = "sent")
    private boolean sent;
    @JoinColumn(name = "health_log_id", referencedColumnName = "health_log_id")
    @ManyToOne(optional = false)
    private HealthLog healthLog;

    public HeartLog() {
    }

    public HeartLog(Integer heartLogId) {
        this.heartLogId = heartLogId;
    }

    public HeartLog(Integer heartLogId, Date timeLog, int rate) {
        this.heartLogId = heartLogId;
        this.timeLog = timeLog;
        this.rate = rate;
    }

    public Integer getHeartLogId() {
        return heartLogId;
    }

    public void setHeartLogId(Integer heartLogId) {
        this.heartLogId = heartLogId;
    }

    public Date getTimeLog() {
        return timeLog;
    }

    public void setTimeLog(Date timeLog) {
        this.timeLog = timeLog;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public HealthLog getHealthLog() {
        return healthLog;
    }

    public void setHealthLog(HealthLog healthLog) {
        this.healthLog = healthLog;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(timeLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HeartLog)) {
            return false;
        }
        HeartLog other = (HeartLog) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.heartLogId, other.heartLogId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(this.getTimeLog())
                .append(" -> ")
                .append(this.getRate())
                .append("]");

        return sb.toString();
    }
}
