package es.jyago.hermes.login;

import es.jyago.hermes.person.Person;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.NoResultException;
import org.jfree.util.Log;

@ManagedBean(name = "userLogin")
@SessionScoped
public class LoginBean {

    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());
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

    public void login(ActionEvent event) {
        FacesMessage message;

        try {
            user = getUser(username, password);

            if (user != null) {
                message = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("Welcome"), user.toString());
            } else {
                message = new FacesMessage(FacesMessage.SEVERITY_WARN, bundle.getString("InvalidCredentials"), "");
            }

            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (NoResultException ex) {
            Log.debug("No existe el usuario: " + username, ex);
        } catch (Exception ex) {
            Log.error("Error al acceder al sistema", ex);
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

    public void keepSessionAlive() {
        Log.debug("Mantener activa la sesión del usuario: " + username);
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
