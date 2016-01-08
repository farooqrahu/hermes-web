/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.person.configuration;

import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.person.Person;
import java.io.Serializable;
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
@Table(name = "person_configuration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PersonConfiguration.findAll", query = "SELECT p FROM PersonConfiguration p"),
    @NamedQuery(name = "PersonConfiguration.findByPersonConfigurationId", query = "SELECT p FROM PersonConfiguration p WHERE p.personConfigurationId = :personConfigurationId"),
    @NamedQuery(name = "PersonConfiguration.findByPersonId", query = "SELECT p FROM PersonConfiguration p WHERE p.person.personId = :personId")})
public class PersonConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "person_configuration_id")
    private Integer personConfigurationId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "value")
    private String value;
    @JoinColumn(name = "option_key", referencedColumnName = "option_key")
    @ManyToOne(optional = false)
    private Configuration option;
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    @ManyToOne(optional = false)
    private Person person;

    public PersonConfiguration() {
    }

    public Integer getPersonConfigurationId() {
        return personConfigurationId;
    }

    public void setPersonConfigurationId(Integer personConfigurationId) {
        this.personConfigurationId = personConfigurationId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Configuration getOption() {
        return option;
    }

    public void setOption(Configuration option) {
        this.option = option;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(option).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PersonConfiguration)) {
            return false;
        }
        PersonConfiguration other = (PersonConfiguration) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.personConfigurationId, other.personConfigurationId).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(this.getOption())
                .append(" -> ")
                .append(this.getValue())
                .append("]");

        return sb.toString();
    }

}
