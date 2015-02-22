/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.recordLog;

import es.jyago.hermes.categoryLog.CategoryLog;
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
    @NamedQuery(name = "RecordLog.findAll", query = "SELECT s FROM RecordLog s"),
    @NamedQuery(name = "RecordLog.findByRecordLogId", query = "SELECT s FROM RecordLog s WHERE s.recordLogId = :recordLogId"),
    @NamedQuery(name = "RecordLog.findByTimeLog", query = "SELECT s FROM RecordLog s WHERE s.timeLog = :timeLog"),
    @NamedQuery(name = "RecordLog.findBySteps", query = "SELECT s FROM RecordLog s WHERE s.steps = :steps")})
public class RecordLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "step_log_id")
    private Integer recordLogId;
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
    private CategoryLog categoryLog;

    public RecordLog() {
    }

    public RecordLog(Integer recordLogId) {
        this.recordLogId = recordLogId;
    }

    public RecordLog(Integer recordLogId, Date timeLog, int steps) {
        this.recordLogId = recordLogId;
        this.timeLog = timeLog;
        this.steps = steps;
    }

    public Integer getRecordLogId() {
        return recordLogId;
    }

    public void setRecordLogId(Integer recordLogId) {
        this.recordLogId = recordLogId;
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

    public CategoryLog getCategoryLog() {
        return categoryLog;
    }

    public void setCategoryLog(CategoryLog categoryLog) {
        this.categoryLog = categoryLog;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (recordLogId != null ? recordLogId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordLog)) {
            return false;
        }
        RecordLog other = (RecordLog) object;
        if ((this.recordLogId == null && other.recordLogId != null) || (this.recordLogId != null && !this.recordLogId.equals(other.recordLogId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "es.jyago.amp.RecordLog[ recordLogId=" + recordLogId + " ]";
    }
    
}
