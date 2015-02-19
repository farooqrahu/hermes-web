/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jyago.hermes.chart;

import java.util.LinkedHashMap;
import java.util.Map;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author Jorge Yago
 */
public interface ChartInterface {
    
    public PieChartModel getPieModel(Map<String, Integer> values);
    public LineChartModel getLineModel(LinkedHashMap<String, Integer> values);
    public Map<String, Integer> getAggregatedValues();
    public LinkedHashMap<String, Integer> getValues();
}
