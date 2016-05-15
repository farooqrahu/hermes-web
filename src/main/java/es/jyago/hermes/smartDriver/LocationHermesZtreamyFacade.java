package es.jyago.hermes.smartDriver;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.ztreamy.AbstractHermesZtreamyFacade;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LocationHermesZtreamyFacade extends AbstractHermesZtreamyFacade<Location> {

    private static final String EVENT_TYPE = "Vehicle Location";
    private static final String BODY_ELEMENTS_TYPE = "Location";

    public LocationHermesZtreamyFacade(Location location, Person person, String url) throws MalformedURLException, HermesException {
        super(location, person, url, EVENT_TYPE);
    }

    public LocationHermesZtreamyFacade(Collection<Location> locationCollection, Person person, String url) throws MalformedURLException, HermesException {
        super(locationCollection, person, url, EVENT_TYPE, false);
    }

    @Override
    public Map<String, Object> getBodyObject(Location location) {
        HashSet<Location> locationCollection = new HashSet();

        locationCollection.add(location);

        return getBodyObject(locationCollection);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<Location> locationCollection) {
        Map<String, Object> bodyObject = null;

        if (locationCollection != null && !locationCollection.isEmpty()) {
            bodyObject = new HashMap<>();
            if (locationCollection.size() == 1) {
                bodyObject.put(BODY_ELEMENTS_TYPE, locationCollection.iterator().next());
            } else {
                bodyObject.put(BODY_ELEMENTS_TYPE, locationCollection);
            }
        }

        return bodyObject;
    }

    @Override
    public Collection<Object> getBodyObjects(Collection<Location> collection) {
        List<Object> locationList = new ArrayList<>();

        if (collection != null && !collection.isEmpty()) {
            locationList.addAll(collection);
        }

        return locationList;
    }

    @Override
    public String getType() {
        return BODY_ELEMENTS_TYPE;
    }

}
