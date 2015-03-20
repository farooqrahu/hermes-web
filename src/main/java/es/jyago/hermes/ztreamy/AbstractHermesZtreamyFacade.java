/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.ztreamy;

import es.jyago.hermes.person.Person;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ztreamy.Publisher;
import ztreamy.Event;
import ztreamy.JSONSerializer;
import ztreamy.Serializer;

/**
 *
 * @author Jorge Yago
 */
public abstract class AbstractHermesZtreamyFacade<T> {

    // TODO: Meter en la tabla de configuración y recuperar al logarse el usuario.
    private String url = "http://hermes1.gast.it.uc3m.es:9100/collector/publish";
    // TODO: Meter en la tabla de configuración y recuperar al logarse el usuario.
    private String sourceId = "DraftSourceIDHermes-Citizen";
    // TODO: Meter en la tabla de configuración y recuperar al logarse el usuario.
    private String applicationId = "Hermes-Citizen-Fitbit";

    private final Publisher publisher;
    private final Event event;

    public AbstractHermesZtreamyFacade(T element) throws MalformedURLException {
        Serializer serializer = new JSONSerializer();
        publisher = new Publisher(new URL(url), serializer);
        event = new Event(sourceId,
                "application/json", applicationId);
        setBody(element);
    }
    
    public AbstractHermesZtreamyFacade(Collection<T> collection) throws MalformedURLException {
        Serializer serializer = new JSONSerializer();
        publisher = new Publisher(new URL(url), serializer);
        event = new Event(sourceId,
                "application/json", applicationId);
        setBody(collection);
    }
    
    private void setBody(T element)
    {
        event.setBody(getBodyObject(element));
    }
    
    private void setBody(Collection<T> collection)
    {
        event.setBody(getBodyObject(collection));
    }

    public final void send() throws IOException {

        int result = publisher.publish(event);
        if (result == 200) {
           Logger.getLogger(Person.class.getName()).log(Level.INFO, "Datos enviados a Ztreamy satisfactoriamente");
        } else {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, "Error al enviar los datos a Ztreamy", result);
        }
    }

    public abstract Map<String, Object> getBodyObject(T element);
    
    public abstract Map<String, Object> getBodyObject(Collection<T> collection);
}
