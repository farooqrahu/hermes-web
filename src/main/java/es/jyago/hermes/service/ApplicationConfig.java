/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.service;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Jorge Yago
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Declaramos las clases con los servicios web por entidad.
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(es.jyago.hermes.service.ContextLogFacadeREST.class);
        resources.add(es.jyago.hermes.service.PersonFacadeREST.class);
        resources.add(es.jyago.hermes.service.SleepLogFacadeREST.class);
    }

}
