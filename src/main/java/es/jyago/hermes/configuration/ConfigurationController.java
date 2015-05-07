package es.jyago.hermes.configuration;

import es.jyago.hermes.util.JsfUtil;
import es.jyago.hermes.util.JsfUtil.PersistAction;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import javax.persistence.NoResultException;

@Named("configurationController")
@SessionScoped
public class ConfigurationController implements Serializable {

    private static final Logger log = Logger.getLogger(ConfigurationController.class.getName());
    @EJB
    private es.jyago.hermes.configuration.ConfigurationFacade ejbFacade;
    private List<Configuration> items = null;
    private Configuration selected;

    public ConfigurationController() {
        log.log(Level.INFO, "ConfigurationController() - Inicialización del controlador de configuración");
    }

    public Configuration getSelected() {
        return selected;
    }

    public void setSelected(Configuration selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ConfigurationFacade getFacade() {
        return ejbFacade;
    }

    public Configuration prepareCreate() {
        selected = new Configuration();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("OptionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("OptionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("OptionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Configuration> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
                
                // Mostramos el mensaje 
                JsfUtil.showHelpMessage(ResourceBundle.getBundle("/Bundle").getString("ApplyNextLoginInfo"));
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Configuration getItemByKey(String key) {
        Configuration result = null;

        try {
            result = (Configuration) ejbFacade.getEntityManager().createNamedQuery("Configuration.findByOptionKey")
                    .setParameter("optionKey", key)
                    .getSingleResult();
        } catch (NoResultException ex) {
            FacesContext.getCurrentInstance().validationFailed();
            log.log(Level.INFO, "getItemByKey() - No existe la configuración con clave: {0}", key);
        }

        return result;
    }

    public String getValueFromItemByKey(String key) {
        Configuration option = getItemByKey(key);
        String value = null;

        if (option != null) {
            value = option.getOptionValue();
        }

        return value;
    }

    public int getIntValueFromItemByKey(String key) {
        int value = 0;
        String stringValue = "";

        try {
            stringValue = getValueFromItemByKey(key);
            value = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "getIntValueFromItemByKey() - El valor [{0}] de la clave [{1}] no es un entero. Se devolverá un '0'", new Object[]{stringValue, key});
        }

        return value;
    }

    public Configuration getOption(java.lang.String id) {
        return getFacade().find(id);
    }

    public List<Configuration> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Configuration> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Configuration.class)
    public static class ConfigurationControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ConfigurationController controller = (ConfigurationController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "configurationController");
            return controller.getOption(getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Configuration) {
                Configuration o = (Configuration) object;
                return getStringKey(o.getOptionKey());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Configuration.class.getName()});
                return null;
            }
        }

    }

}
