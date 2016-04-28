package es.jyago.hermes.csv;

import org.supercsv.cellprocessor.ift.CellProcessor;


public interface ICSVBean {
    
    public void init(Integer columns);

    public CellProcessor[] getProcessors();

    public String[] getFields();

    public String[] getHeaders();

}
