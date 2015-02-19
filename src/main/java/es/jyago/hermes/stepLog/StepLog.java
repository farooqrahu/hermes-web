/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.stepLog;

import es.jyago.hermes.activityLog.ActivityLog;
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

/**
 *
 * @author Jorge Yago
 */
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
    @JoinColumn(name = "activity_log_id", referencedColumnName = "activity_log_id")
    @ManyToOne(optional = false)
    private ActivityLog activityLog;

    public StepLog() {
    }

    public StepLog(Integer stepLogId) {
        this.stepLogId = stepLogId;
    }

    public StepLog(Integer stepLogId, Date timeLog, int steps) {
        this.stepLogId = stepLogId;
        this.timeLog = timeLog;
        this.steps = steps;
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

    public ActivityLog getActivityLog() {
        return activityLog;
    }

    public void setActivityLog(ActivityLog activityLog) {
        this.activityLog = activityLog;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (stepLogId != null ? stepLogId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StepLog)) {
            return false;
        }
        StepLog other = (StepLog) object;
        if ((this.stepLogId == null && other.stepLogId != null) || (this.stepLogId != null && !this.stepLogId.equals(other.stepLogId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "es.jyago.amp.StepLog[ stepLogId=" + stepLogId + " ]";
    }
    
}
