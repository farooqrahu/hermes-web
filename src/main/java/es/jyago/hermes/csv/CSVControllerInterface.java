/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jyago.hermes.csv;

import java.util.List;

/**
 *
 * @author Jorge Yago
 */
public interface CSVControllerInterface<T> {
    public List<T> getItems();
    public void processReadElement(T element);
}
