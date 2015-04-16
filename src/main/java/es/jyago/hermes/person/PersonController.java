package es.jyago.hermes.person;

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
import es.jyago.hermes.util.HermesException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.joda.time.LocalDate;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.LineChartModel;

@Named("personController")
@SessionScoped
public class PersonController implements Serializable, CSVControllerInterface<Person> {

    private static final Logger log = Logger.getLogger(PersonController.class.getName());

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
        update(true);
    }

    public void update(boolean showMessage) {
        String message = null;
        if (showMessage) {
            message = ResourceBundle.getBundle("/Bundle").getString("PersonUpdated");
        }
        persist(PersistAction.UPDATE, message);
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
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();

        String tempTokenReceived = request.getParameter(HermesFitbitController.OAUTH_TOKEN);
        String tempTokenVerifier = request.getParameter(HermesFitbitController.OAUTH_VERIFIER);

        if (tempTokenReceived != null && tempTokenVerifier != null) {
            if (completeAuthorization(tempTokenReceived, tempTokenVerifier)) {
                // Invocamos el formulario de edición de usuario, para que el usuario pueda corregir sus datos.
                RequestContext.getCurrentInstance().execute("PF('PersonEditDialog').show()");
            }
        }

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
                if (successMessage != null) {
                    JsfUtil.addSuccessMessage(successMessage);
                }
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
                log.log(Level.SEVERE, "persist() - Error al registrar los cambios", ex);
                // TODO: Usar esta forma para mostrar los mensajes!
                // TODO: Incluso poner el 'bundle' en JsfUtil y enviar sólo la key.
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

    public void initSynchronizationDates() {
        if (selected != null) {
            initFitbitController();
            startDate = getStartSyncDate();
            endDate = getEndSyncDate();
        }
    }

    public Date getStartSyncDate() {
        // La fecha de inicio de sincronización será la más antigua entre la última registrada para la persona
        // y la última sincronización de la pulsera de Fitbit.
        // Si la persona no tuviera ninguna sincronización, tomamos como fecha de partida el primer día del año actual.
        Date personLastSynchronization = selected.getLastSynchronization();
        Date lastFitbitSynchronization = getEndSyncDate();
        if (personLastSynchronization == null) {
            Calendar firstDayOfYear = Calendar.getInstance();
            firstDayOfYear.set(Calendar.MONTH, 0);
            firstDayOfYear.set(Calendar.DAY_OF_MONTH, 1);
            personLastSynchronization = firstDayOfYear.getTime();
        }
        return personLastSynchronization.before(lastFitbitSynchronization) ? personLastSynchronization : lastFitbitSynchronization;
    }

    public Date getEndSyncDate() {
        Date endSyncDate = new Date();

        try {
            if (hermesFitbitController != null) {
                // La sincronización se hará hasta el último día de sincronización de la pulsera de Fitbit.
                endSyncDate = hermesFitbitController.getLastSyncDate();
            }
        } catch (HermesException ex) {
            log.log(Level.SEVERE, "getEndSyncDate() - Error al obtener la fecha de fin de sincronización", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Info"), ex.getMessage()));
        }

        return endSyncDate;
    }

    private void initFitbitController() {
        if (selected != null) {
            if (selected.hasFitbitCredentials()) {
                hermesFitbitController = new HermesFitbitController(selected);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/Bundle").getString("Info"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.info.PersonWithoutCredentials")));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/Bundle").getString("Info"), ResourceBundle.getBundle("/Bundle").getString("ListPersonNotSelected")));
        }
    }

    public void authorize(String nextPage) {
        initFitbitController();
        try {
            authorizeUrl = hermesFitbitController.getAuthorizeURL((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(), nextPage);
        } catch (HermesException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
        }
    }

    public void prepareRegister() {
        hermesFitbitController = new HermesFitbitController(prepareCreate());
        authorize("/faces/register.xhtml");
    }

    public String register() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map<String, String> requestMap = ctx.getExternalContext().getRequestParameterMap();

        String tempTokenReceived = requestMap.get(HermesFitbitController.OAUTH_TOKEN);
        String tempTokenVerifier = requestMap.get(HermesFitbitController.OAUTH_VERIFIER);

        if (tempTokenReceived != null && tempTokenVerifier != null) {
            if (completeAuthorization(tempTokenReceived, tempTokenVerifier)) {
                return "/faces/index.xhtml";
            }
        } else {
            log.log(Level.SEVERE, "register() - Error al completar el registro porque los tokens son nulos.\noauth_token = {0}\noauth_verifier = {1}", new Object[]{tempTokenReceived, tempTokenVerifier});
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.invalidTokens")));
        }
        /*
         ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
         ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
         */
        // Para mantener los mensajes en una redirección.
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.getFlash().setKeepMessages(true);
        return "/faces/register?faces-redirect=true";
    }

    private boolean completeAuthorization(String tempTokenReceived, String tempTokenVerifier) {
        try {
            hermesFitbitController.completeAuthorization(tempTokenReceived, tempTokenVerifier);
            if (hermesFitbitController.isResourceCredentialsSet() && selected != null) {
                hermesFitbitController.transferUserInfoToPerson(selected);
                // FIXME
                // Comprobamos si estÃ¡ rellena la informaciÃ³n necesaria y si no, la rellenamos con valores por defecto.
                fillDefaultPerson();
                create();
                //update();
                // Invocamos el formulario de ediciÃ³n de usuario, para que el usuario pueda corregir sus datos.
//                RequestContext.getCurrentInstance().execute("PF('PersonEditDialog').show()");
                return true;
            }
        } catch (HermesException ex) {
            log.log(Level.SEVERE, "register() - Error al completar el registro", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
        }

        return false;
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
        log.log(Level.INFO, "synchronize() - Sincronización manual con Fitbit de la persona {0}", selected.toString());
        initFitbitController();

        try {
            log.log(Level.INFO, "synchronize() - Obteniendo datos desde {0} hasta {1}", new Object[]{Constants.dfTime.format(startDate), Constants.dfTime.format(endDate)});

            List<IntradaySummary> listIntradaySummary = hermesFitbitController.getIntradayData(startDate, endDate);

            HashSet<ActivityLog> hashSetActivityLog = new HashSet<>(selected.getActivityLogCollection());
            for (IntradaySummary intradaySummary : listIntradaySummary) {
                ActivityLog activityLog = new ActivityLog();

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
                activityLog.calculateTotal();
                hashSetActivityLog.remove(activityLog);
                hashSetActivityLog.add(activityLog);
            }
            selected.getActivityLogCollection().clear();
            update(false);
            selected.getActivityLogCollection().addAll(hashSetActivityLog);
            update();
        } catch (HermesException ex) {
            log.log(Level.SEVERE, "synchronize() - Error al sincronizar los datos de Fitbit", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
        } catch (ParseException ex) {
            log.log(Level.SEVERE, "synchronize() - Error al sincronizar los datos de Fitbit", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.parsingDate")));
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

    public void initChartDates() {
        if (selected != null) {
            LocalDate start = new LocalDate();
            start = start.dayOfMonth().withMinimumValue();
            startDate = start.toDate();
            LocalDate end = new LocalDate(start);
            end = end.dayOfMonth().withMaximumValue();
            endDate = end.toDate();
        }
    }

    public LineChartModel getLineChartModel() {
        if (selected != null) {
            // Para el gráfico de actividad de la persona daremos los siguientes parámetros:
            // - Fecha de inicio...: Primer día del mes actual
            // - Fecha de fin......: Último día del mes actual
            // - Agregación........: Por días
            if (startDate == null && endDate == null) {
                Calendar cal = Calendar.getInstance();

                cal.set(Calendar.DATE, cal.getActualMinimum(Calendar.DATE));
                startDate = cal.getTime();

                cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
                endDate = cal.getTime();
            }

            List<ActivityLog> monthActivityLogList = selected.getActivityLogCollection(startDate, endDate, Constants.TimeAggregations.Days.toString());

            LinkedHashMap<Date, Integer> values = new LinkedHashMap();
            if (monthActivityLogList != null && monthActivityLogList.size() > 0) {
                for (ActivityLog activityLog : monthActivityLogList) {
                    values.put(activityLog.getDate(), activityLog.getTotal());
                }
            }

            return selected.getLineModel(values, Constants.dfMonthYear.format(startDate).toUpperCase());
        }

        return null;
    }

    public boolean hasPreviousMonth() {
        LocalDate end = new LocalDate(startDate);
        end = end.plusMonths(-1);
        end = end.dayOfMonth().withMaximumValue();
        return selected.getActivityLogCollection(end.plusDays(-1).toDate(), end.toDate(), null).isEmpty();
    }

    public boolean hasNextMonth() {
        LocalDate start = new LocalDate(startDate);
        start = start.plusMonths(1);
        start = start.dayOfMonth().withMinimumValue();
        return selected.getActivityLogCollection(start.toDate(), start.plusDays(1).toDate(), null).isEmpty();
    }

    public void previousMonthChart() {
        changeLineChartMonth(-1);
    }

    public void nextMonthChart() {
        changeLineChartMonth(1);
    }

    private void changeLineChartMonth(int months) {
        LocalDate start = new LocalDate(startDate);
        start = start.plusMonths(months);
        start = start.dayOfMonth().withMinimumValue();
        startDate = start.toDate();
        LocalDate end = new LocalDate(start);
        end = end.dayOfMonth().withMaximumValue();
        endDate = end.toDate();
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
