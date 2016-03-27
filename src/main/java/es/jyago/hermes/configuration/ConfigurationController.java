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
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;

@Singleton
@Startup
public class ConfigurationController implements Serializable {

    private static final Logger LOG = Logger.getLogger(ConfigurationController.class.getName());
    @EJB
    private es.jyago.hermes.configuration.ConfigurationFacade ejbFacade;
    private List<Configuration> items = null;
    private Configuration selected;

    public ConfigurationController() {
        LOG.log(Level.INFO, "ConfigurationController() - Inicialización del controlador de configuración");
    }

    public Configuration getSelected() {
        return selected;
    }

    public void setSelected(Configuration s) {
        selected = s;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

//    public Configuration prepareCreate() {
//        selected = new Configuration();
//        initializeEmbeddableKey();
//        return selected;
//    }
//    public void create() {
//        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("OptionCreated"));
//        if (!JsfUtil.isValidationFailed()) {
//            selected = null; // Remove selection
//            items = null;    // Invalidate list of items to trigger re-query.
//        }
//    }
//    public void update() {
//        update(true);
//    }
    public void update(boolean showMessage) {
        String message = null;
        if (showMessage) {
            message = ResourceBundle.getBundle("/Bundle").getString("OptionUpdated");
        }
        persist(PersistAction.UPDATE, message);
    }

//    public void destroy() {
//        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("OptionDeleted"));
//        if (!JsfUtil.isValidationFailed()) {
//            selected = null; // Remove selection
//            items = null;    // Invalidate list of items to trigger re-query.
//        }
//    }
    
    public List<Configuration> getItems() {
        if (items == null) {
            items = ejbFacade.findAll();
        }
        return items;
    }
    
    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    ejbFacade.edit(selected);
                } else {
                    ejbFacade.remove(selected);
                }
                if (successMessage != null) {
                    JsfUtil.addSuccessMessage(successMessage);
                    // Mostramos el mensaje 
                    JsfUtil.showHelpMessage(ResourceBundle.getBundle("/Bundle").getString("ApplyNextLoginInfo"));
                }
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
                LOG.log(Level.SEVERE, "persist() - No se ha podido grabar la configuración", ex);
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
            LOG.log(Level.INFO, "getItemByKey() - No existe la configuración con clave: {0}", key);
            FacesContext.getCurrentInstance().validationFailed();
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
            LOG.log(Level.WARNING, "getIntValueFromItemByKey() - El valor [{0}] de la clave [{1}] no es un entero. Se devolverá un '0'", new Object[]{stringValue, key});
        }

        return value;
    }

//    public Configuration getOption(java.lang.String id) {
//        return ejbFacade.find(id);
//    }
//    public List<Configuration> getItemsAvailableSelectMany() {
//        return ejbFacade.findAll();
//    }
//    public List<Configuration> getItemsAvailableSelectOne() {
//        return ejbFacade.findAll();
//    }
//    @FacesConverter(forClass = Configuration.class)
//    public class ConfigurationControllerConverter implements Converter {
//
//        @Override
//        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
//            if (value == null || value.length() == 0) {
//                return null;
//            }
//            ConfigurationController controller = (ConfigurationController) facesContext.getApplication().getELResolver().
//                    getValue(facesContext.getELContext(), null, "configurationController");
//            return controller.getOption(getKey(value));
//        }
//
//        java.lang.String getKey(String value) {
//            java.lang.String key;
//            key = value;
//            return key;
//        }
//
//        String getStringKey(java.lang.String value) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(value);
//            return sb.toString();
//        }
//
//        @Override
//        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
//            if (object == null) {
//                return null;
//            }
//            if (object instanceof Configuration) {
//                Configuration o = (Configuration) object;
//                return getStringKey(o.getOptionKey());
//            } else {
//                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Configuration.class.getName()});
//                return null;
//            }
//        }
//
//    }
}
