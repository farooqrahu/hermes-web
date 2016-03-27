/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.service;

import es.jyago.hermes.AbstractFacade;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.Util;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

    private static final Logger LOG = Logger.getLogger(PersonFacadeREST.class.getName());

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
            LOG.log(Level.SEVERE, "existsUser() - No se ha encontrado el usuario por email: {0}", email);
        }

        return false;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @POST
    @Path("/registerUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public int create(AndroidPerson androidPerson) {

        Person person = new Person();

        // Validamos el email.
        if (Util.isValidEmail(androidPerson.getEmail()) && androidPerson.getEmail().length() <= 100) {
            person.setEmail(androidPerson.getEmail());
        } else {
            return Constants.REST_ERROR_INVALID_EMAIL;
        }

        // Validamos la clave.
        if (Util.isAlphaNumeric(androidPerson.getPassword()) && androidPerson.getPassword().length() <= 20) {
            person.setPassword(androidPerson.getPassword());
        } else {
            return Constants.REST_ERROR_INVALID_PASSWORD;
        }

        try {
            // Comprobamos si ya existe el usuario.
            if (existsUser(androidPerson.getEmail())) {
                return Constants.REST_ERROR_USER_EXISTS;
            } else {

                person.setUsername(Long.toString(System.currentTimeMillis()));
                person.setFullName(androidPerson.getEmail());
                // Lo registramos en la B.D.
                super.create(person);
                em.flush();
                // Actualizamos el nombre de usuario.
                person.setUsername("User" + person.getPersonId());
                super.edit(person);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "create() - Error al crear el usuario", ex);
            return Constants.REST_ERROR_USER_NOT_REGISTERED;
        }

        return Constants.REST_OK;
    }

    public static class AndroidPerson {

        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
