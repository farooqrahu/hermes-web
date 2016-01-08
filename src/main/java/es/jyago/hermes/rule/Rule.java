/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.rule;

import es.jyago.hermes.alert.Alert;
import java.io.Serializable;
import java.util.Random;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "rule")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Rule.findAll", query = "SELECT r FROM Rule r"),
    @NamedQuery(name = "Rule.findByRuleId", query = "SELECT r FROM Rule r WHERE r.ruleId = :ruleId"),
    @NamedQuery(name = "Rule.findByCheckWhat", query = "SELECT r FROM Rule r WHERE r.checkWhat = :checkWhat"),
    @NamedQuery(name = "Rule.findByOperator", query = "SELECT r FROM Rule r WHERE r.operator = :operator"),
    @NamedQuery(name = "Rule.findByValue", query = "SELECT r FROM Rule r WHERE r.value = :value"),
    @NamedQuery(name = "Rule.findByCheckWhen", query = "SELECT r FROM Rule r WHERE r.checkWhen = :checkWhen")})
public class Rule implements Serializable {

    public static enum RuleOperators {

        Equal("=="), Distinct("!="), LessThan("<"), LessOrEqual("<="), GreaterThan(">"), GreaterOrEqual(">=");

        private String op;

        RuleOperators(String op) {
            this.op = op;
        }

        public String getOp() {
            return op;
        }
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "rule_id")
    private Integer ruleId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "check_what")
    private String checkWhat;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "operator")
    private String operator;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "value")
    private String value;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "check_when")
    private String checkWhen;
    @JoinColumn(name = "alert_id", referencedColumnName = "alert_id")
    @ManyToOne(optional = false)
    private Alert alert;

    public Rule() {
        Random rand = new Random();
        this.ruleId = rand.nextInt(Integer.MAX_VALUE);
        this.value = "0";
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public String getCheckWhat() {
        return checkWhat;
    }

    public void setCheckWhat(String checkWhat) {
        this.checkWhat = checkWhat;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setOperator(RuleOperators operator) {
        this.operator = operator.name();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCheckWhen() {
        return checkWhen;
    }

    public void setCheckWhen(String checkWhen) {
        this.checkWhen = checkWhen;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(checkWhat).
                append(checkWhen).
                append(operator).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Rule)) {
            return false;
        }
        Rule other = (Rule) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.ruleId, other.ruleId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(this.checkWhat)
                .append(" ")
                .append(this.operator)
                .append(" ")
                .append(this.value)
                .append(" ")
                .append(this.checkWhen)
                .append("]");

        return sb.toString();
    }

}
