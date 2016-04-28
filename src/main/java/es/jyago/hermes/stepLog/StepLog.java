package es.jyago.hermes.stepLog;

import es.jyago.hermes.activityLog.ActivityLog;
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


@Entity
@Table(name = "step_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "StepLog.findAll", query = "SELECT s FROM StepLog s"),
    @NamedQuery(name = "StepLog.findByStepLogId", query = "SELECT s FROM StepLog s WHERE s.stepLogId = :stepLogId"),
    @NamedQuery(name = "StepLog.findByTimeLog", query = "SELECT s FROM StepLog s WHERE s.timeLog = :timeLog"),
    @NamedQuery(name = "StepLog.findBySteps", query = "SELECT s FROM StepLog s WHERE s.steps = :steps")})
public class StepLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "step_log_id")
    private Integer stepLogId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "time_log")
    @Temporal(TemporalType.TIME)
    private Date timeLog;
    @Basic(optional = false)
    @NotNull
    @Column(name = "steps")
    private int steps;
    @Basic(optional = false)
    @Column(name = "sent")
    private boolean sent;
    @JoinColumn(name = "activity_log_id", referencedColumnName = "activity_log_id")
    @ManyToOne(optional = false)
    private ActivityLog activityLog;

    public StepLog() {
    }

    public StepLog(Integer stepLogId) {
        this.stepLogId = stepLogId;
    }

    public Integer getStepLogId() {
        return stepLogId;
    }

    public void setStepLogId(Integer stepLogId) {
        this.stepLogId = stepLogId;
    }

    public Date getTimeLog() {
        return timeLog;
    }

    public void setTimeLog(Date timeLog) {
        this.timeLog = timeLog;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public ActivityLog getActivityLog() {
        return activityLog;
    }

    public void setActivityLog(ActivityLog activityLog) {
        this.activityLog = activityLog;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(timeLog).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StepLog)) {
            return false;
        }
        StepLog other = (StepLog) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.stepLogId, other.stepLogId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(Constants.dfTime.format(this.timeLog))
                .append(" -> ")
                .append(this.steps)
                .append("]");

        return sb.toString();
    }
}
