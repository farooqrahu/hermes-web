/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.ztreamy;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.HermesException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ztreamy.Publisher;
import ztreamy.Event;
import ztreamy.JSONSerializer;

/**
 *
 * @author Jorge Yago
 */
public abstract class AbstractHermesZtreamyFacade<T> {

    private static final Logger log = Logger.getLogger(AbstractHermesZtreamyFacade.class.getName());
    private Publisher publisher;
    private Event model;
    private List<Event> events;
    private Person person;
    private String url;
    private boolean oneEvent;

    public AbstractHermesZtreamyFacade(T element, Person person, String url) throws MalformedURLException, HermesException {
        this.person = person;
        this.url = url;
        this.oneEvent = true;
        this.model = prepareEvent();
        this.publisher = new Publisher(new URL(url), new JSONSerializer());
        setBody(element);
    }

    public AbstractHermesZtreamyFacade(Collection<T> collection, Person person, String url, boolean oneEvent) throws MalformedURLException, HermesException {
        this.person = person;
        this.url = url;
        this.oneEvent = false;
        this.publisher = new Publisher(new URL(url), new JSONSerializer());
        setBody(collection);
    }

    private void setBody(T element) throws HermesException {
        prepareOneEventBody(getBodyObject(element));
    }

    private void setBody(Collection<T> collection) throws HermesException {
        if (oneEvent) {
            prepareOneEventBody(getBodyObject(collection));
        } else {
            prepareMultipleEventsBodies(getBodyObjects(collection));
        }
    }

    private void prepareOneEventBody(Map<String, Object> body) throws HermesException {
        if (body == null || body.isEmpty()) {
            log.log(Level.WARNING, "El cuerpo del evento de la persona {0} está vacío. No se enviará por Ztreamy", person.getFullName());
            throw new HermesException("Ztreamy.emptyDataset");
        }

        model.setBody(body);
    }

    private void prepareMultipleEventsBodies(Collection<Object> collection) throws HermesException {
        if (collection == null || collection.isEmpty()) {
            log.log(Level.WARNING, "El cuerpo del evento de la persona {0} está vacío. No se enviará por Ztreamy", person.getFullName());
            throw new HermesException("Ztreamy.emptyDataset");
        }

        events = new ArrayList();
        for (Object info : collection)
        {
            Event event = prepareEvent();
            Map<String, Object> map = new HashMap<>();
            map.put(getType(), info);
            event.setBody(map);

            events.add(event);
        }
    }

    public final boolean send() throws IOException {

        boolean ok = false;
        int result = -1;

        if (oneEvent) {
            result = publisher.publish(model);
        } else {
            result = publisher.publish(events.toArray(new Event[events.size()]));
        }

        if (result == 200) {
            ok = true;
            log.log(Level.INFO, "send() - Datos enviados por Ztreamy satisfactoriamente");
        } else {
            log.log(Level.SEVERE, "send() - Error al enviar los datos por Ztreamy", result);
        }

        return ok;
    }

    public Person getPerson() {
        return this.person;
    }

    public String getUrl() {
        return url;
    }

    public abstract Map<String, Object> getBodyObject(T element);

    public abstract Map<String, Object> getBodyObject(Collection<T> collection);
    
    public abstract Collection<Object> getBodyObjects(Collection<T> collection);

    public abstract Event prepareEvent();
    
    public abstract String getType();
}
