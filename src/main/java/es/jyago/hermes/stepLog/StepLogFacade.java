package es.jyago.hermes.stepLog;

import es.jyago.hermes.AbstractFacade;
import es.jyago.hermes.stepLog.StepLog;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class StepLogFacade extends AbstractFacade<StepLog> {

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public StepLogFacade() {
        super(StepLog.class);
    }

}
