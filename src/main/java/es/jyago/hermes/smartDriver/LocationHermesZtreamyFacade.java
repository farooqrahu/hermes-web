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

    private static final String VEHICLE_LOCATION = "Vehicle Location";

    public LocationHermesZtreamyFacade(Location location, Person person, String url) throws MalformedURLException, HermesException {
        super(location, person, url, VEHICLE_LOCATION);
    }

    public LocationHermesZtreamyFacade(Collection<Location> locationCollection, Person person, String url) throws MalformedURLException, HermesException {
        super(locationCollection, person, url, VEHICLE_LOCATION, false);
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
                bodyObject.put(VEHICLE_LOCATION, locationCollection.iterator().next());
            } else {
                bodyObject.put(VEHICLE_LOCATION, locationCollection);
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
        return VEHICLE_LOCATION;
    }

}
