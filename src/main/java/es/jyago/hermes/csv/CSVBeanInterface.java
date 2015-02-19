/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jyago.hermes.csv;

import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author Jorge Yago
 */
public interface CSVBeanInterface {
    
   public CellProcessor[] getProcessors();
   
   public String[] getFields();
    
}
