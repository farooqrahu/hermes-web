package es.jyago.hermes.stepLog;

import es.jyago.hermes.util.JsfUtil;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@Named("stepLogController")
@SessionScoped
public class StepLogController implements Serializable {

    private static final Logger log = Logger.getLogger(StepLogController.class.getName());

    @EJB
    private es.jyago.hermes.stepLog.StepLogFacade ejbFacade;
    private List<StepLog> items = null;
    private StepLog selected;

    public StepLogController() {
        log.log(Level.INFO, "StepLogController() - Inicialización del controlador de pasos");
    }

    public StepLog getSelected() {
        return selected;
    }

    public void setSelected(StepLog selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private StepLogFacade getFacade() {
        return ejbFacade;
    }

    public StepLog prepareCreate() {
        selected = new StepLog();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(JsfUtil.PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("StepLogCreated"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(JsfUtil.PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("StepLogUpdated"));
    }

    public void destroy() {
        persist(JsfUtil.PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("StepLogDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<StepLog> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(JsfUtil.PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != JsfUtil.PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                // Activamos la bandera para indicar que ha habido un error.
                FacesContext.getCurrentInstance().validationFailed();
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    // Mostramos un mensaje informativo para que revise el formulario.
                    JsfUtil.addErrorMessage(msg, ResourceBundle.getBundle("/Bundle").getString("CheckData"));
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                FacesContext.getCurrentInstance().validationFailed();
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public StepLog getStepLog(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<StepLog> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<StepLog> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = StepLog.class)
    public static class StepLogControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StepLogController controller = (StepLogController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "stepLogController");
            return controller.getStepLog(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof StepLog) {
                StepLog o = (StepLog) object;
                return getStringKey(o.getStepLogId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), StepLog.class.getName()});
                return null;
            }
        }

    }
}
