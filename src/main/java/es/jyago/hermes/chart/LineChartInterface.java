/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jyago.hermes.chart;

import java.util.Date;
import java.util.LinkedHashMap;
import org.primefaces.model.chart.LineChartModel;

/**
 *
 * @author Jorge Yago
 */
public interface LineChartInterface {
    
    public LineChartModel getLineModel(LinkedHashMap<Date, Integer> values, String title);
}
