/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.ztreamy;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private Publisher publisher;
    private Event event;

    public AbstractHermesZtreamyFacade(T element) throws MalformedURLException {
        List<T> collection = new ArrayList();
        collection.add(element);
        init(collection);
    }

    public AbstractHermesZtreamyFacade(Collection<T> collection) throws MalformedURLException {
        init(collection);
    }

    private void init(Collection<T> collection) throws MalformedURLException {
        Serializer serializer = new JSONSerializer();
        // Los parámetros de configuración de Ztreamy estarán en la tabla de configuración.
        publisher = new Publisher(new URL(Constants.getConfigurationValueByKey("ZtreamyURL")), serializer);
        event = new Event(Constants.getConfigurationValueByKey("ZtreamyStepsSourceId"),
                "application/json", Constants.getConfigurationValueByKey("ZtreamyApplicationId"));
        setBody(collection);
    }

    private void setBody(T element) {
        event.setBody(getBodyObject(element));
    }

    private void setBody(Collection<T> collection) {
        event.setBody(getBodyObject(collection));
    }

    public final boolean send() throws IOException {

        boolean ok = false;
        int result = publisher.publish(event);

        if (result == 200) {
            ok = true;
            Logger.getLogger(Person.class.getName()).log(Level.INFO, "send() - Datos enviados por Ztreamy satisfactoriamente");
        } else {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, "send() - Error al enviar los datos por Ztreamy", result);
        }

        return ok;
    }

    public abstract Map<String, Object> getBodyObject(T element);

    public abstract Map<String, Object> getBodyObject(Collection<T> collection);
}
