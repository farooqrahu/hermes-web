package es.jyago.hermes.bean;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

// JYFR: Tiene que ser @ManagedBean en lugar de @Named para que pueda gestionarlo PrimeFaces.
@ManagedBean
// JYFR: Uso la sesión para que se conserve la selección de idioma durante toda la sesión del usuario.
@SessionScoped
public class LocaleBean implements Serializable {

    // Idioma.
    public static Locale locale;

    @ManagedProperty("#{bundle}")
    public static ResourceBundle bundle;

    // JYFR: Usamos PostConstruct para garantizar que ya se ha realizado la inyección de dependencias.
    // Se invocará después de la construcción del bean.
    // Se ejecutará sólo una vez, cuando se cree el bean y se ponga en memoria (ciclo de vida de JSF).
    @PostConstruct
    public void init() {
        // Obtenemos el idioma del navegador del cliente.
        locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    /**
     * Método para cambiar el idioma.
     *
     * @param localeCode Código del idioma.
     */
    public void changeLocale(String localeCode) {
        locale = new Locale(localeCode);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        bundle = ResourceBundle.getBundle("/Bundle", locale);
    }

    public void setBundle(ResourceBundle b) {
        bundle = b;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }
}
