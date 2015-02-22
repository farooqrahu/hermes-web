package es.jyago.hermes.categoryLog;

import es.jyago.hermes.util.JsfUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

@Named("categoryLogController")
@SessionScoped
public class CategoryLogController implements Serializable {

    @EJB
    private es.jyago.hermes.categoryLog.CategoryLogFacade ejbFacade;
    private List<CategoryLog> items = null;
    private CategoryLog selected;

    public CategoryLogController() {
    }

    public CategoryLog getSelected() {
        return selected;
    }

    public void setSelected(CategoryLog selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private CategoryLogFacade getFacade() {
        return ejbFacade;
    }

    public CategoryLog prepareCreate() {
        selected = new CategoryLog();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(JsfUtil.PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("CategoryLogCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(JsfUtil.PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("CategoryLogUpdated"));
    }

    public void destroy() {
        persist(JsfUtil.PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("CategoryLogDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<CategoryLog> getItems() {
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
                // JYFR: Activamos la bandera para indicar que ha habido un error.
                FacesContext.getCurrentInstance().validationFailed();
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    // JYFR: Mostramos un mensaje informativo para que revise el formulario.
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

    public CategoryLog getCategoryLog(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<CategoryLog> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<CategoryLog> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public ChartModel getPieChartModel() {
        if (selected != null) {
            Map<String, Integer> values = selected.getAggregatedValues();

            if (values == null) {
                values = new HashMap();
                values.put("Remaining", selected.getPerson().getStepsGoal());
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
            LinkedHashMap<String, Integer> values = selected.getValues();
            if (values == null) {
                values = new LinkedHashMap();
            }
            LinkedHashMap<String, Integer> localizedValues = new LinkedHashMap();

            for (String key : values.keySet()) {
                localizedValues.put(key, values.get(key));
            }

            return selected.getLineModel(localizedValues);
        }

        return null;
    }
    
    public LineChartModel getAnalyzedChartModel() {
        if (selected != null) {
            LinkedHashMap<String, Integer> values = selected.getValues();
            if (values == null) {
                values = new LinkedHashMap();
            }
            LinkedHashMap<String, Integer> localizedValues = new LinkedHashMap();

            for (String key : values.keySet()) {
                localizedValues.put(key, values.get(key));
            }

            return selected.getAreaModel(localizedValues);
        }

        return null;
    }

    @FacesConverter(forClass = CategoryLog.class)
    public static class CategoryLogControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CategoryLogController controller = (CategoryLogController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "categoryLogController");
            return controller.getCategoryLog(getKey(value));
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
            if (object instanceof CategoryLog) {
                CategoryLog o = (CategoryLog) object;
                return getStringKey(o.getCategoryLogId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), CategoryLog.class.getName()});
                return null;
            }
        }

    }
}
