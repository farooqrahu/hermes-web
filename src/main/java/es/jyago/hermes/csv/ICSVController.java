/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.csv;

import es.jyago.hermes.util.HermesException;
import java.util.List;

/**
 *
 * @author Jorge Yago
 * @param <T>
 */
public interface ICSVController<T> {

    public List<T> getCSVItems();

    public void processReadElement(T element) throws HermesException;
}
