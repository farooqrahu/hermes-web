package es.jyago.hermes.login;

import es.jyago.hermes.person.Person;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.NoResultException;

// JYFR: Tiene que ser @ManagedBean en lugar de @Named para que pueda gestionarlo PrimeFaces.
@ManagedBean(name = "userLogin")
@SessionScoped
public class LoginBean {

    private static final Logger log = Logger.getLogger(LoginBean.class.getName());
    private String username;
    private String password;
    private Person user;
    @ManagedProperty("#{bundle}")
    private ResourceBundle bundle;
    @EJB
    private es.jyago.hermes.person.PersonFacade ejbFacade;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Person getUser(String username, String password) {
        return (Person) ejbFacade.getEntityManager().createNamedQuery("Person.findByUsernamePassword")
                .setParameter("username", username)
                .setParameter("password", password)
                .getSingleResult();
    }

    public boolean login(ActionEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("InvalidCredentials"), "");
        boolean ok = false;

        try {
            user = getUser(username, password);

            if (user != null) {
                message = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("Welcome"), user.toString());
                ok = true;
            } else {
                FacesContext.getCurrentInstance().validationFailed();
            }
        } catch (NoResultException ex) {
            FacesContext.getCurrentInstance().validationFailed();
            log.log(Level.INFO, "login() - No existe el usuario: {0}", username);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().validationFailed();
            log.log(Level.SEVERE, "login() - Error al acceder al sistema", ex);
        }

        FacesContext.getCurrentInstance().addMessage("messages", message);

        return ok;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

    public void keepSessionAlive() {
        log.log(Level.INFO, "keepSessionAlive() - Mantener activa la sesiónn del usuario: {0}", username);
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public Person getUser() {
        return user;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }
}
