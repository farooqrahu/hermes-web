/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.alert;

import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.healthLog.HealthLog;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.rule.Rule;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.util.Constants;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.persistence.Table;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "alert")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Alert.findAll", query = "SELECT a FROM Alert a"),
    @NamedQuery(name = "Alert.findByAlertId", query = "SELECT a FROM Alert a WHERE a.alertId = :alertId"),
    @NamedQuery(name = "Alert.findByName", query = "SELECT a FROM Alert a WHERE a.name = :name"),
    @NamedQuery(name = "Alert.findByActive", query = "SELECT a FROM Alert a WHERE a.active = :active"),
    @NamedQuery(name = "Alert.findAllFromPerson", query = "SELECT a FROM Alert a WHERE a.person.personId = :personId ORDER BY a.name DESC")})
public class Alert implements Serializable {

    public static enum AlertServices {

        TotalSteps, SleepHours, DeepSleepHours, Awakenings, AverageHeartRate, MinimummHeartRate, MaximumHeartRate
    }

    public static enum RulesOperator {

        AND, OR;

    }
    private static final Logger log = Logger.getLogger(Alert.class.getName());

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "alert_id")
    private Integer alertId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private boolean active;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "alert", orphanRemoval = true)
    private List<Rule> ruleList;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "rules_operator")
    private String rulesOperator;

    public Alert() {
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @XmlTransient
    public List<Rule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<Rule> ruleList) {
        this.ruleList = ruleList;
    }

    public String getRulesOperator() {
        return rulesOperator;
    }

    public void setRulesOperator(String rulesOperator) {
        this.rulesOperator = rulesOperator;
    }

    public boolean checkTrigger() {

        // Comprobamos si hay reglas definidas.
        if (ruleList == null || ruleList.isEmpty()) {
            // No hay reglas que comprobar.
            return false;
        }

        // Comprobamos el tipo de operador de las reglas de la alerta.
        boolean isAND = rulesOperator.equals(RulesOperator.AND.name());
        boolean trigger;

        for (Rule rule : ruleList) {
            // Comprobamos el tipo de regla de la que se trata.
            if (rule.getCheckWhat().equals(AlertServices.TotalSteps.name())) {
                ///////////////////
                // Pasos totales //
                ///////////////////
                trigger = checkStepRule(rule);
            } else if (rule.getCheckWhat().equals(AlertServices.SleepHours.name())) {
                ////////////////////
                // Horas de sueño //
                ////////////////////
                trigger = checkSleepRule(rule);
            } else if (rule.getCheckWhat().equals(AlertServices.Awakenings.name())) {
                ///////////////////////////
                // Despertares //
                ///////////////////////////
                trigger = checkAwakeningsRule(rule);

            } else if (rule.getCheckWhat().equals(AlertServices.DeepSleepHours.name())) {
                /////////////////////////////
                // Horas de sueño profundo //
                /////////////////////////////
                trigger = checkDeepSleepRule(rule);
            } else if (rule.getCheckWhat().equals(AlertServices.AverageHeartRate.name())) {
                //////////////////////////
                // Ritmo cardíaco medio //
                //////////////////////////
                trigger = checkAverageHeartRateRule(rule);
            } else if (rule.getCheckWhat().equals(AlertServices.MinimummHeartRate.name())) {
                ///////////////////////////
                // Ritmo cardíaco mínimo //
                ///////////////////////////
                trigger = checkMinimumHeartRateRule(rule);
            } else if (rule.getCheckWhat().equals(AlertServices.MaximumHeartRate.name())) {
                ///////////////////////////
                // Ritmo cardíaco máximo //
                ///////////////////////////
                trigger = checkMaximumHeartRateRule(rule);
            } else {
                trigger = false;
            }

            // Comprobamos si se cumple la alerta con las condiciones actuales.
            if (!trigger && isAND) {
                // Si es un AND, al ser una de las reglas falsa, ya no se cumple la alerta.
                return false;
            } else if (trigger && !isAND) {
                // Si es un OR, al ser una de las reglas cierta, ya se cumple la alerta.
                return true;
            }
        }

        // Llegados a este punto, ya se han evaluado todas las reglas.
        if (isAND) {
            // Como no se ha salido antes, quiere decir que todas las reglas se cumplen, luego se cumple la alerta.
            return true;
        }

        // Si es un OR y no se ha cumplido ninguna regla, quiere decir que no se cumple la alerta.
        return false;
    }

    private boolean checkStepRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            ActivityLog activityLog = person.getActivityLog(yesterday.toDate());
            if (activityLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(activityLog.getTotal());
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int stepsAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de pasos de toda la semana.
                ActivityLog activityLog = person.getActivityLog(currentDate.toDate());
                if (activityLog != null) {
                    // Hay datos.
                    stepsAmount += activityLog.getTotal();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(stepsAmount);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int stepsAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de pasos de todo el mes.
                ActivityLog activityLog = person.getActivityLog(currentDate.toDate());
                if (activityLog != null) {
                    // Hay datos.
                    stepsAmount += activityLog.getTotal();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(stepsAmount);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean checkSleepRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            SleepLog sleepLog = person.getSleepLog(yesterday.toDate());
            if (sleepLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(sleepLog.getMinutesInBed() / 60.0f);
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int sleepAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de sueño de toda la semana.
                SleepLog sleepLog = person.getSleepLog(currentDate.toDate());
                if (sleepLog != null) {
                    // Hay datos.
                    sleepAmount += sleepLog.getMinutesInBed();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(sleepAmount / 60.0f);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int sleepAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de sueño de todo el mes.
                SleepLog sleepLog = person.getSleepLog(currentDate.toDate());
                if (sleepLog != null) {
                    // Hay datos.
                    sleepAmount += sleepLog.getMinutesInBed();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(sleepAmount / 60.0f);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean checkDeepSleepRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            SleepLog sleepLog = person.getSleepLog(yesterday.toDate());
            if (sleepLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(sleepLog.getMinutesAsleep() / 60.0f);
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int sleepAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de sueño profundo de toda la semana.
                SleepLog sleepLog = person.getSleepLog(currentDate.toDate());
                if (sleepLog != null) {
                    // Hay datos.
                    sleepAmount += sleepLog.getMinutesAsleep();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(sleepAmount / 60.0f);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int sleepAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de sueño profundo de todo el mes.
                SleepLog sleepLog = person.getSleepLog(currentDate.toDate());
                if (sleepLog != null) {
                    // Hay datos.
                    sleepAmount += sleepLog.getMinutesAsleep();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(sleepAmount / 60.0f);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean checkAwakeningsRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            SleepLog sleepLog = person.getSleepLog(yesterday.toDate());
            if (sleepLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(sleepLog.getAwakenings());
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int awakeningsAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de despertares de toda la semana.
                SleepLog sleepLog = person.getSleepLog(currentDate.toDate());
                if (sleepLog != null) {
                    // Hay datos.
                    awakeningsAmount += sleepLog.getAwakenings();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(awakeningsAmount);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int awakeningsAmount = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando los totales de despertares de todo el mes.
                SleepLog sleepLog = person.getSleepLog(currentDate.toDate());
                if (sleepLog != null) {
                    // Hay datos.
                    awakeningsAmount += sleepLog.getAwakenings();
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(awakeningsAmount);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean checkAverageHeartRateRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            HealthLog healthLog = person.getHealthLog(yesterday.toDate());
            if (healthLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(healthLog.getAverage());
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int averageHeartRate = 0;
            int daysWithData = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando las pulsaciones medias de toda la semana.
                HealthLog healthLog = person.getHealthLog(currentDate.toDate());
                if (healthLog != null) {
                    // Hay datos.
                    averageHeartRate += healthLog.getAverage();
                    daysWithData++;
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(daysWithData > 0 ? averageHeartRate / daysWithData : 0);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int averageHeartRate = 0;
            int daysWithData = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando las pulsaciones medias de todo el mes.
                HealthLog healthLog = person.getHealthLog(currentDate.toDate());
                if (healthLog != null) {
                    // Hay datos.
                    averageHeartRate += healthLog.getAverage();
                    daysWithData++;
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(daysWithData > 0 ? averageHeartRate / daysWithData : 0);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean checkMinimumHeartRateRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            HealthLog healthLog = person.getHealthLog(yesterday.toDate());
            if (healthLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(healthLog.getMinimumHeartRate());
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int averageHeartRate = 0;
            int daysWithData = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando las pulsaciones mínimas de toda la semana.
                HealthLog healthLog = person.getHealthLog(currentDate.toDate());
                if (healthLog != null) {
                    // Hay datos.
                    averageHeartRate += healthLog.getMinimumHeartRate();
                    daysWithData++;
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(daysWithData > 0 ? averageHeartRate / daysWithData : 0);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int averageHeartRate = 0;
            int daysWithData = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando las pulsaciones mínimas de todo el mes.
                HealthLog healthLog = person.getHealthLog(currentDate.toDate());
                if (healthLog != null) {
                    // Hay datos.
                    averageHeartRate += healthLog.getMinimumHeartRate();
                    daysWithData++;
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(daysWithData > 0 ? averageHeartRate / daysWithData : 0);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean checkMaximumHeartRateRule(Rule rule) {
        boolean result = false;
        LocalDate today = new LocalDate();

        // Comprobamos la periodicidad de la comprobación de la regla.
        if (rule.getCheckWhen().equals(Constants.TimeChecks.Daily.name())) {
            //////////////                        
            // A diario //
            //////////////                        

            // Comprobaremos el día anterior, ya que el día actual no estará completo aún.
            LocalDate yesterday = today.minusDays(1);

            HealthLog healthLog = person.getHealthLog(yesterday.toDate());
            if (healthLog != null) {
                // Hay datos de ayer.
                StringBuilder expression = new StringBuilder();
                expression.append(healthLog.getMaximumHeartRate());
                expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
                expression.append(rule.getValue());
                result = evaluateExpression(expression.toString());
            }
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Weekly.name())) {
            //////////////////
            // Semanalmente //
            //////////////////

            // Comprobaremos la semana anterior a la que estamos.
            LocalDate weekAgo = today.minusWeeks(1);
            LocalDate mondayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.MONDAY);
            LocalDate sundayWeekAgo = weekAgo.withDayOfWeek(DateTimeConstants.SUNDAY);
            int days = Days.daysBetween(mondayWeekAgo, sundayWeekAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(mondayWeekAgo);
            int averageHeartRate = 0;
            int daysWithData = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando las pulsaciones máximas de toda la semana.
                HealthLog healthLog = person.getHealthLog(currentDate.toDate());
                if (healthLog != null) {
                    // Hay datos.
                    averageHeartRate += healthLog.getMaximumHeartRate();
                    daysWithData++;
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(daysWithData > 0 ? averageHeartRate / daysWithData : 0);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        } else if (rule.getCheckWhen().equals(Constants.TimeChecks.Monthly.name())) {
            //////////////////
            // Mensualmente //
            //////////////////

            // Comprobaremos el mes anterior al que estamos.
            LocalDate monthAgo = today.minusMonths(1);
            LocalDate firstDayMonthAgo = monthAgo.dayOfMonth().withMinimumValue();
            LocalDate lastDayMonthAgo = monthAgo.dayOfMonth().withMaximumValue();
            int days = Days.daysBetween(firstDayMonthAgo, lastDayMonthAgo).getDays() + 1;
            LocalDate currentDate = new LocalDate(firstDayMonthAgo);
            int averageHeartRate = 0;
            int daysWithData = 0;

            for (int i = 0; i < days; i++) {
                // Vamos acumulando las pulsaciones máximas de todo el mes.
                HealthLog healthLog = person.getHealthLog(currentDate.toDate());
                if (healthLog != null) {
                    // Hay datos.
                    averageHeartRate += healthLog.getMaximumHeartRate();
                    daysWithData++;
                }
            }

            StringBuilder expression = new StringBuilder();
            expression.append(daysWithData > 0 ? averageHeartRate / daysWithData : 0);
            expression.append(Rule.RuleOperators.valueOf(rule.getOperator()).getOp());
            expression.append(rule.getValue());
            result = evaluateExpression(expression.toString());
        }

        return result;
    }

    private boolean evaluateExpression(String expression) {
        try {
            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine se = sem.getEngineByName("JavaScript");
            boolean result = (Boolean) se.eval(expression);
            log.log(Level.INFO, "evaluateExpression() - Expresión: {0} -> {1}", new Object[]{expression, result});
            return result;
        } catch (ScriptException ex) {
            log.log(Level.SEVERE, "evaluateExpression() - Error al evaluar la expresión", ex);
        }

        return false;
    }

    public AlertServices[] getAlertServices() {
        return AlertServices.values();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(name).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Alert)) {
            return false;
        }
        Alert other = (Alert) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.alertId, other.alertId).
                isEquals();

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(this.name)
                .append(" -> ")
                .append(this.ruleList.size())
                .append("(").append(this.rulesOperator).append(")")
                .append("]");

        return sb.toString();
    }
}
