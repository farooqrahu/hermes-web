package es.jyago.hermes.csv;

import es.jyago.hermes.util.HermesException;
import java.util.List;

public interface ICSVController<T> {

    public List<T> getCSVItems();

    public void processReadElement(T element) throws HermesException;
}
