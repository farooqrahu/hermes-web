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

public class DataSectionHermesZtreamyFacade extends AbstractHermesZtreamyFacade<DataSection> {

    private static final String EVENT_TYPE = "Data Section";
    private static final String BODY_ELEMENTS_TYPE = "Data Section";

    public DataSectionHermesZtreamyFacade(DataSection dataSection, Person person, String url) throws MalformedURLException, HermesException {
        super(dataSection, person, url, EVENT_TYPE);
    }

    public DataSectionHermesZtreamyFacade(Collection<DataSection> dataSectionCollection, Person person, String url) throws MalformedURLException, HermesException {
        super(dataSectionCollection, person, url, EVENT_TYPE, false);
    }

    @Override
    public Map<String, Object> getBodyObject(DataSection dataSection) {
        HashSet<DataSection> dataSectionCollection = new HashSet();

        dataSectionCollection.add(dataSection);

        return getBodyObject(dataSectionCollection);
    }

    @Override
    public Map<String, Object> getBodyObject(Collection<DataSection> dataSectionCollection) {
        Map<String, Object> bodyObject = null;

        if (dataSectionCollection != null && !dataSectionCollection.isEmpty()) {
            bodyObject = new HashMap<>();
            if (dataSectionCollection.size() == 1) {
                bodyObject.put(BODY_ELEMENTS_TYPE, dataSectionCollection.iterator().next());
            } else {
                bodyObject.put(BODY_ELEMENTS_TYPE, dataSectionCollection);
            }
        }

        return bodyObject;
    }

    @Override
    public Collection<Object> getBodyObjects(Collection<DataSection> collection) {
        List<Object> dataSectionList = new ArrayList<>();

        if (collection != null && !collection.isEmpty()) {
            dataSectionList.addAll(collection);
        }

        return dataSectionList;
    }

    @Override
    public String getType() {
        return BODY_ELEMENTS_TYPE;
    }

}
