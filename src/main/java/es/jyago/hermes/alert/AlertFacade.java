package es.jyago.hermes.alert;

import es.jyago.hermes.AbstractFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


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
