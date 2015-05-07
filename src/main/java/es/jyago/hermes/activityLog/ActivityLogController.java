package es.jyago.hermes.activityLog;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.JsfUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
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
import org.jfree.util.Log;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.LineChartModel;

@Named("activityLogController")
@SessionScoped
public class ActivityLogController implements Serializable {

    private static final Logger log = Logger.getLogger(ActivityLogController.class.getName());

    @EJB
    private es.jyago.hermes.activityLog.ActivityLogFacade ejbFacade;
    private List<ActivityLog> items = null;
    private ActivityLog selected;

    public ActivityLogController() {
        log.log(Level.INFO, "ActivityLogController() - Inicialización del controlador de actividades");
    }

    public ActivityLog getSelected() {
        return selected;
    }

    public void setSelected(ActivityLog selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ActivityLogFacade getFacade() {
        return ejbFacade;
    }

    public ActivityLog prepareCreate() {
        selected = new ActivityLog();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(JsfUtil.PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("ActivityLogCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(JsfUtil.PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("ActivityLogUpdated"));
    }

    public void destroy() {
        persist(JsfUtil.PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("ActivityLogDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<ActivityLog> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public void initListFromPerson(int personId) {
        items = ejbFacade.getEntityManager().createNamedQuery("ActivityLog.findAllFromPerson")
                .setParameter("personId", personId).getResultList();
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

    public ActivityLog getActivityLog(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<ActivityLog> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<ActivityLog> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public ChartModel getPieChartModel() {
        if (selected != null) {
            Map<String, Integer> values = selected.getAggregatedValues();

            if (values == null) {
                values = new HashMap();
                values.put("Remaining", selected.getPerson().getConfigurationIntValue(Person.PersonOptions.StepsGoal.name()));
            }

            Map<String, Integer> localizedValues = new HashMap();
            ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");

            for (String key : values.keySet()) {
                String localizedKey = "---";
                try {
                    localizedKey = bundle.getString(key);
                } catch (MissingResourceException ex) {
                    Log.error("Error al obtener la clave '" + key + "' del archivo de recursos", ex);
                }
                localizedValues.put(localizedKey, values.get(key));
            }

            return selected.getPieModel(localizedValues);
        }

        return null;
    }

    public LineChartModel getLineChartModel() {
        if (selected != null) {
            return selected.getLineModel(selected.getValues(), Constants.df.format(selected.getDate()));
        }

        return null;
    }

    public LineChartModel getSessionsChartModel() {
        if (selected != null) {
            return selected.getAreaModel(Constants.df.format(selected.getDate()));
        }

        return null;
    }

    @FacesConverter(forClass = ActivityLog.class)
    public static class ActivityLogControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ActivityLogController controller = (ActivityLogController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "activityLogController");
            return controller.getActivityLog(getKey(value));
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
            if (object instanceof ActivityLog) {
                ActivityLog o = (ActivityLog) object;
                return getStringKey(o.getActivityLogId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ActivityLog.class.getName()});
                return null;
            }
        }

    }
}
