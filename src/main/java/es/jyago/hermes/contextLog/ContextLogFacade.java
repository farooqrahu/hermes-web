package es.jyago.hermes.contextLog;

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
public class ContextLogFacade extends AbstractFacade<ContextLog> {

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ContextLogFacade() {
        super(ContextLog.class);
    }
    
    public List<ContextLog> findNotSent() {
        // FIXME: No debería ser necesario.
//        em.clear();
        return em.createNamedQuery("ContextLog.findBySent").setParameter("sent", false).getResultList();
    }
}
