/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.service;

import es.jyago.hermes.AbstractFacade;
import es.jyago.hermes.person.Person;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Jorge Yago Ejemplo de URL para probar:
 * http://localhost:8080/HermesWeb/webresources/hermes.citizen.person/existsUser/jorgeyago@gmail.com
 * Es útil el plugin de Chrome: Advanced REST Client
 */
@Stateless
@Path("hermes.citizen.person")
public class PersonFacadeREST extends AbstractFacade<Person> {

    private static final Logger log = Logger.getLogger(PersonFacadeREST.class.getName());

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    public PersonFacadeREST() {
        super(Person.class);
    }

    @GET
    @Path("existsUser/{email}")
    @Produces(MediaType.TEXT_PLAIN)
    public Boolean existsUser(@PathParam("email") String email) {
        try {
            em.createNamedQuery("Person.findByEmail")
                    .setParameter("email", email)
                    .getSingleResult();

            return true;

        } catch (Exception ex) {
            log.log(Level.SEVERE, "existsUser() - No se ha encontrado el usuario por email: {0}", email);
        }

        return false;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
