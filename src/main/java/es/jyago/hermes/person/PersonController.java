package es.jyago.hermes.person;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.common.model.timeseries.IntradayData;
import com.fitbit.api.common.model.timeseries.IntradaySummary;
import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.activityLog.ActivityLogController;
import es.jyago.hermes.csv.CSVControllerInterface;
import es.jyago.hermes.csv.CSVUtil;
import es.jyago.hermes.fitbit.HermesFitbitController;
import es.jyago.hermes.stepLog.StepLog;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.JsfUtil;
import es.jyago.hermes.util.JsfUtil.PersistAction;
import es.jyago.hermes.activityLog.ActivityLogHermesZtreamyFacade;
import es.jyago.hermes.util.InternationalizedException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

@Named("personController")
@SessionScoped
public class PersonController implements Serializable, CSVControllerInterface<Person> {

    @EJB
    private es.jyago.hermes.person.PersonFacade ejbFacade;
    private List<Person> items;
    private Person selected;
    private HermesFitbitController hermesFitbitController;
    private String authorizeUrl;
    private Date startDate;
    private Date endDate;
    private String aggregation;

    public PersonController() {
        authorizeUrl = null;
        selected = null;
        items = null;
        hermesFitbitController = null;
        startDate = Calendar.getInstance().getTime();
        endDate = Calendar.getInstance().getTime();
    }

    public Person getSelected() {
        return selected;
    }

    public void setSelected(Person selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private PersonFacade getFacade() {
        return ejbFacade;
    }

    public Person prepareCreate() {
        selected = new Person();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("PersonCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("PersonUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("PersonDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    @Override
    public List<Person> getItems() {
        // TODO: Nuevo registro autorizando por Fitbit
        // TODO: Reubicar.
//        FacesContext ctx = FacesContext.getCurrentInstance();
//        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
//
//        String tempTokenReceived = request.getParameter(HermesFitbitController.OAUTH_TOKEN);
//        String tempTokenVerifier = request.getParameter(HermesFitbitController.OAUTH_VERIFIER);
//
//        if (tempTokenReceived != null && tempTokenVerifier != null) {
//            hermesFitbitController.completeAuthorization(tempTokenReceived, tempTokenVerifier);
//            if (hermesFitbitController.isResourceCredentialsSet() && selected != null) {
//                hermesFitbitController.transferUserInfoToPerson(selected);
//                // FIXME
//                // Comprobamos si está rellena la información necesaria y si no, la rellenamos con valores por defecto.
//                fillDefaultPerson();
//                create();
//                //update();
//                // Invocamos el formulario de edición de usuario, para que el usuario pueda corregir sus datos.
//                RequestContext.getCurrentInstance().execute("PF('PersonEditDialog').show()");
//            }
//        }

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

    public Person getPerson(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<Person> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Person> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    private void initFitbitController() {
        if (selected != null) {
            hermesFitbitController = new HermesFitbitController(selected);
        }
    }

    public void authorize(String nextPage) {
        initFitbitController();
        try {
            authorizeUrl = hermesFitbitController.getAuthorizeURL((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(), nextPage);
        } catch (FitbitAPIException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.authorization")));
        }
    }

    public void register() {
        hermesFitbitController = new HermesFitbitController(prepareCreate());
        authorize("/faces/secured/person/List.xhtml");
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public String getAuthorizeUrlAndReset() {
        // La URL de autorización sólo puede ser usada una vez.
        String temp = authorizeUrl;
        authorizeUrl = null;
        return temp;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public void synchronize() {
        initFitbitController();

        try {
            hermesFitbitController.populate();
            List<IntradaySummary> listIntradaySummary = hermesFitbitController.getIntradayData(startDate, endDate);

            HashSet<ActivityLog> hashSetActivityLog = new HashSet<>(selected.getActivityLogCollection());
            for (IntradaySummary intradaySummary : listIntradaySummary) {
                ActivityLog activityLog = new ActivityLog();

                try {
                    activityLog.setDate(Constants.dfFitbit.parse(intradaySummary.getSummary().getDateTime()));
                    activityLog.setStepLogCollection(new ArrayList());
                    activityLog.setPerson(selected);

                    for (IntradayData intradayData : intradaySummary.getIntradayDataset().getDataset()) {
                        StepLog stepLog = new StepLog();
                        stepLog.setActivityLog(activityLog);
                        stepLog.setTimeLog(Constants.dfTime.parse(intradayData.getTime()));
                        stepLog.setSteps((int) intradayData.getValue());
                        activityLog.getStepLogCollection().add(stepLog);
                    }
                    hashSetActivityLog.remove(activityLog);
                    hashSetActivityLog.add(activityLog);
                } catch (ParseException ex) {
                    Logger.getLogger(PersonController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            selected.getActivityLogCollection().clear();
            update();
            selected.getActivityLogCollection().addAll(hashSetActivityLog);
            update();
        } catch (FitbitAPIException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.Synchronize")));
            Logger.getLogger(PersonController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InternationalizedException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
            Logger.getLogger(PersonController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void validate() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc.isValidationFailed()) {
            // FIXME: Poner de una forma más elegante.
            // FIXME: El componente de TabView no enfoca la pestaña en la que ha fallado la validación
            //       ¿Combinación con p:focus? 
            //    this.tabView.setActiveIndex(0);
        }
    }

    private void fillDefaultPerson() {
        if (this.selected == null) {
            this.selected = new Person();
        }
        // Rellenamos los campos obligatorios con valores por defecto, en caso de que no vengan rellenos.
        if (this.selected.getFirstName() == null) {
            this.selected.setFirstName(ResourceBundle.getBundle("/Bundle").getString("Default"));
        }

        if (this.selected.getSurname1() == null) {
            this.selected.setSurname1(ResourceBundle.getBundle("/Bundle").getString("Default"));
        }

        if (this.selected.getSurname2() == null) {
            this.selected.setSurname2(ResourceBundle.getBundle("/Bundle").getString("Default"));
        }
    }

    public void sendToZtreamy() {
        FacesMessage message;

        try {
            ActivityLogHermesZtreamyFacade ztreamy = new ActivityLogHermesZtreamyFacade(this.getSelected().getActivityLogCollection(startDate, endDate, aggregation));

            if (ztreamy.send()) {
                message = new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/Bundle").getString("Ztreamy"), ResourceBundle.getBundle("/Bundle").getString("ZtreamySendOK"));
            } else {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Ztreamy.error"));
            }
        } catch (MalformedURLException ex) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Ztreamy.error.Url"));
            Logger.getLogger(ActivityLogController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Ztreamy.error"));
            Logger.getLogger(ActivityLogController.class.getName()).log(Level.SEVERE, null, ex);
        }

        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public String getAggregation() {
        // La agregación por defecto, será 'Horas'.
        if (aggregation == null) {
            aggregation = Constants.TimeAggregations.Hours.toString();
        }
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }

    @FacesConverter(forClass = Person.class)
    public static class PersonControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PersonController controller = (PersonController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "personController");
            return controller.getPerson(getKey(value));
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
            if (object instanceof Person) {
                Person o = (Person) object;
                return getStringKey(o.getPersonId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Person.class.getName()});
                return null;
            }
        }

    }

    public void sendDateRangeValidator(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        System.out.println(value);
        if (value == null) {
            return;
        }

        Object startDateValue = component.getAttributes().get("startDate");
        if (startDateValue == null) {
            return;
        }

        Date startDate = (Date) startDateValue;
        Date endDate = (Date) value;
        if (endDate.before(startDate)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.Synchronize")));
        }
    }

    public HermesFitbitController getHermesFitbitController() {
        return hermesFitbitController;
    }

    public StreamedContent getFile() {
        return new CSVUtil<Person>().getData(new Person(), this);
    }

    public void handleFileUpload(FileUploadEvent event) {
        // TODO: INTERNACIONALIZAR!!!
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        new CSVUtil<Person>().setData(new Person(), this, event.getFile());

        selected = null;
        items = null;
    }

    // TODO: PRUEBA!!!!
    @Override
    public void processReadElement(Person person) {

        selected = getPerson(person.getPersonId());

        if (selected != null) {
            person.setPhoto(selected.getPhoto());
//            person.setRole(selected.getRole());
            selected = person;
            update();
        } else {
            selected = person;
            create();
        }
    }

}
