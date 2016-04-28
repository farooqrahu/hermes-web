package es.jyago.hermes.healthLog;

import es.jyago.hermes.AbstractFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class HealthLogFacade extends AbstractFacade<HealthLog> {

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public HealthLogFacade() {
        super(HealthLog.class);
    }
    
    public List<HealthLog> findNotSent() {
        // FIXME: No debería ser necesario.
//        em.clear();
        return em.createNamedQuery("HealthLog.findBySent").setParameter("sent", false).getResultList();
    }
}
