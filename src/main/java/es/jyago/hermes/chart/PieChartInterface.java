/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jyago.hermes.chart;

import java.util.Map;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author Jorge Yago
 */
public interface PieChartInterface {
    
    public PieChartModel getPieModel(Map<String, Integer> values);
}
