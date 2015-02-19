/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jyago.hermes;

import java.io.Serializable;

/**
 *
 * @author Jorge Yago
 */
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    private String url;
    private String descripcion;

    public Report() {
    }

    public String getUrl() {
        return url;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (url != null ? url.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Report)) {
            return false;
        }
        Report other = (Report) object;
        if ((this.url == null && other.url != null) || (this.url != null && !this.url.equals(other.url))) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append(this.url);
        
        return sb.toString();
    }
    
}
