/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.configuration;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "configuration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Configuration.findAll", query = "SELECT c FROM Configuration c"),
    @NamedQuery(name = "Configuration.findByOptionKey", query = "SELECT c FROM Configuration c WHERE c.optionKey = :optionKey"),
    @NamedQuery(name = "Configuration.findByOptionLabel", query = "SELECT c FROM Configuration c WHERE c.optionLabel = :optionLabel"),
    @NamedQuery(name = "Configuration.findByOptionDescription", query = "SELECT c FROM Configuration c WHERE c.optionDescription = :optionDescription"),
    @NamedQuery(name = "Configuration.findByOptionRolesAllowed", query = "SELECT c FROM Configuration c WHERE c.optionRolesAllowed = :optionRolesAllowed"),
    @NamedQuery(name = "Configuration.findByPersonId", query = "SELECT c FROM Configuration c WHERE c.optionPersonId = :optionPersonId")})
public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "option_key")
    private String optionKey;
    @Size(max = 100)
    @Column(name = "option_value")
    private String optionValue;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "option_label")
    private String optionLabel;
    @Size(max = 200)
    @Column(name = "option_description")
    private String optionDescription;
    @Size(max = 20)
    @Column(name = "option_roles_allowed")
    private String optionRolesAllowed;
    @Size(max = 5)
    @Column(name = "option_person_id")
    @Basic(optional = true)
    private String optionPersonId;


    public Configuration() {
    }

    public Configuration(String optionKey) {
        this.optionKey = optionKey;
    }

    public Configuration(String optionKey, String optionLabel) {
        this.optionKey = optionKey;
        this.optionLabel = optionLabel;
    }

    public String getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public String getOptionDescription() {
        return optionDescription;
    }

    public void setOptionDescription(String optionDescription) {
        this.optionDescription = optionDescription;
    }

    public String getOptionRolesAllowed() {
        return optionRolesAllowed;
    }

    public void setOptionRolesAllowed(String optionRolesAllowed) {
        this.optionRolesAllowed = optionRolesAllowed;
    }

    public String getOptionPersonId() {
        return optionPersonId;
    }

    public void setOptionPersonId(String optionPersonId) {
        this.optionPersonId = optionPersonId;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (optionKey != null ? optionKey.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Configuration)) {
            return false;
        }
        Configuration other = (Configuration) object;
        if ((this.optionKey == null && other.optionKey != null) || (this.optionKey != null && !this.optionKey.equals(other.optionKey))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "es.jyago.hermes.configuration.Configuration[ optionKey=" + optionKey + " ]";
    }
    
}
