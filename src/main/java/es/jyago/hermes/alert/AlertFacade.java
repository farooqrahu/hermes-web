/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.alert;

import es.jyago.hermes.AbstractFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jorge Yago
 */
@Stateless
public class AlertFacade extends AbstractFacade<Alert> {

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public AlertFacade() {
        super(Alert.class);
    }

    public List<Alert> findAllFromPerson(int personId) {
        return em.createNamedQuery("Alert.findAllFromPerson").setParameter("personId", personId).getResultList();
    }
}
