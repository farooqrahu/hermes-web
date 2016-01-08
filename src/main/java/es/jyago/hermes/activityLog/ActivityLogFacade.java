package es.jyago.hermes.activityLog;

import es.jyago.hermes.AbstractFacade;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jorge Yago
 */
@Stateless
public class ActivityLogFacade extends AbstractFacade<ActivityLog> {

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ActivityLogFacade() {
        super(ActivityLog.class);
    }
}
