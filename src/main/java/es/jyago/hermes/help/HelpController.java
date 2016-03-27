package es.jyago.hermes.help;

import es.jyago.hermes.bean.LocaleBean;
import es.jyago.hermes.email.Email;
import es.jyago.hermes.login.LoginController;
import es.jyago.hermes.util.JsfUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

@Named("helpController")
@RequestScoped
public class HelpController {

    private static final Logger LOG = Logger.getLogger(HelpController.class.getName());
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void validate() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc.isValidationFailed()) {
            // FIXME: ¿Validación del texto enviado?
        }
    }

    public void sendContactQuestion() {
        send("PREGUNTA");
        
        // La pregunta se queda registrada, aunque sea sólo en el log, con lo que al usuario le mostramos un mensaje satisfactorio, para no enfadarlo si hubiera un problema ;)
        JsfUtil.addSuccessMessage(LocaleBean.getBundle().getString("ContactQuestionRegisteredSuccessfully"));
    }

    public void sendSuggestion() {
        send("SOLICITUD DE MEJORA");
        
        // La sugerencia se queda registrada, aunque sea sólo en el log, con lo que al usuario le mostramos un mensaje satisfactorio, para no enfadarlo si hubiera un problema ;)
        JsfUtil.addSuccessMessage(LocaleBean.getBundle().getString("SuggestionRegisteredSuccessfully"));
    }

    private void send(String type) {
        try {
            // Recuperamos la información de la sesión, para indicar el remitente del mensaje.
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            LoginController loginBean = (LoginController) request.getSession().getAttribute("loginController");
            String sender = "";
            
            if (loginBean != null && loginBean.isLoggedIn()) {
                sender = loginBean.getPerson().getFullName() + loginBean.getPerson().getEmail() != null ? loginBean.getPerson().getEmail() : "";
                text += "<hr/>Enviado por: " + sender;
            }

            // Enviamos el mensaje a la cuenta del administrador.
            Email.generateAndSendEmailToAdministrator(type, text);
        } catch (MessagingException ex) {
            LOG.log(Level.SEVERE, "send() - Error al enviar el mensaje: \n###" + text + "###", ex);
        }

    }

}
