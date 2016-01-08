/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.fitbit;

import es.jyago.hermes.person.Person;

/**
 *
 * @author Jorge Yago
 */
public interface IFitbitFacade {
    
    public Person getPerson();

    public void updatePerson();
}
