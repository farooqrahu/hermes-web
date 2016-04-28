package es.jyago.hermes.google.distanceMatrix;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Row {

    @SerializedName("elements")
    @Expose
    private List<Element> elements = new ArrayList<>();

    /**
     *
     * @return The elements
     */
    public List<Element> getElements() {
        return elements;
    }

    /**
     *
     * @param elements The elements
     */
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

}
