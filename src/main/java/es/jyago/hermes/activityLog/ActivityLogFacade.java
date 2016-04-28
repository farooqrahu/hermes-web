package es.jyago.hermes.activityLog;

import es.jyago.hermes.AbstractFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


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
    
     public List<ActivityLog> findNotSent() {
        // FIXME: No debería ser necesario.
//        em.clear();
        return em.createNamedQuery("ActivityLog.findBySent").setParameter("sent", false).getResultList();
    }
}
