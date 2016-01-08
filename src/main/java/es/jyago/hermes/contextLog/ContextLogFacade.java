package es.jyago.hermes.contextLog;

import es.jyago.hermes.AbstractFacade;
import es.jyago.hermes.person.Person;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jorge Yago
 */
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

    public List<ContextLog> findByPersonAndDate(Person person, Date dateLog) {
        return em.createNamedQuery("ContextLog.findByPersonAndDateLog").setParameter("personId", person.getPersonId()).setParameter("dateLog", dateLog).getResultList();
    }
}
