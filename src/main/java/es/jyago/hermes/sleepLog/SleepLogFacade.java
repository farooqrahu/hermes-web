/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.sleepLog;

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
public class SleepLogFacade extends AbstractFacade<SleepLog> {

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public SleepLogFacade() {
        super(SleepLog.class);
    }
    
    public List<SleepLog> findNotSent() {
        // FIXME: No debería ser necesario.
//        em.clear();
        return em.createNamedQuery("SleepLog.findBySent").setParameter("sent", false).getResultList();
    }
}
