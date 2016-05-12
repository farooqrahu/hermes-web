
package es.jyago.hermes.openStreetMap;

import java.util.ArrayList;
import java.util.List;

public class GeomWay {

    private String type;
    private List<List<Double>> coordinates = new ArrayList<>();

    /**
     * 
     * @return
     *     The type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The coordinates
     */
    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    /**
     * 
     * @param coordinates
     *     The coordinates
     */
    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

}
