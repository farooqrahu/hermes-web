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
import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.login.LoginBean;
import es.jyago.hermes.person.configuration.PersonConfiguration;
import es.jyago.hermes.util.HermesException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.LineChartModel;

@Named("personController")
@SessionScoped
public class PersonController implements Serializable, CSVControllerInterface<Person> {

    private static final Logger log = Logger.getLogger(PersonController.class.getName());

    @EJB
    private PersonFacade ejbFacade;
    private List<Person> items;
    private Person selected;
    private HermesFitbitController hermesFitbitController;
    private String authorizeUrl;
    private Date startDate;
    private Date endDate;
    private String aggregation;
    private ActivityLog selectedActivity;
    private List<ActivityLog> chartMonthActivityLogList;
    private int dateSelector;
    @Inject
    private ActivityLogController activityLogController;

    public PersonController() {
        log.log(Level.INFO, "PersonController() - Inicialización del controlador de personas");
        authorizeUrl = null;
        selected = null;
        items = null;
        hermesFitbitController = null;
        startDate = Calendar.getInstance().getTime();
        endDate = Calendar.getInstance().getTime();
        dateSelector = 1;
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
        List<Configuration> configList = new ArrayList();

        for (Person.PersonOptions option : Person.PersonOptions.values()) {
            configList.add(Constants.getConfigurationByKey(option.name()));
        }

        selected = new Person(configList);

        initializeEmbeddableKey();
        return selected;
    }

    public void prepareEdit() {
        if (selected != null) {

            // Analizamos las configuraciones que tiene asignadas la persona, por si faltan.
            if (selected.getConfigurationCollection() == null || selected.getConfigurationCollection().size() < Person.PersonOptions.values().length) {
                for (Person.PersonOptions option : Person.PersonOptions.values()) {
                    boolean found = false;
                    for (PersonConfiguration pc : selected.getConfigurationCollection()) {
                        if (option.name().equals(pc.getOption().getOptionKey())) {
                            found = true;
                            break;
                        }
                    }

                    // Si es una opción que no tiene la persona, la añadimos, pero le asignamos 'null' como valor para que el usuario tenga que grabar los datos.
                    if (!found) {
                        PersonConfiguration pc = new PersonConfiguration();
                        pc.setOption(Constants.getConfigurationByKey(option.name()));
                        pc.setPerson(selected);
                        pc.setValue(null);
                        selected.getConfigurationCollection().add(pc);
                    }
                }
                selected.prepareConfigurationCollectionHashMap();
            }
        }
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
            LoginBean loginBean = (LoginBean) request.getSession().getAttribute("userLogin");

            if (loginBean.getUser().isAdmin() || loginBean.getUser().isDoctor()) {
                items = getFacade().findAll();
                // Procesamos todos los elementos para añadirle las opciones de configuración.

            } else {
                items = new ArrayList<>();
                items.add(loginBean.getUser());
            }
            // Mostramos el mensaje de ayuda si es la primera vez que accede (que será si no existe la cookie)
            JsfUtil.showHelpMessage("initialMessagePersonList", ResourceBundle.getBundle("/Bundle").getString("ContextMenuInfo"));
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
                    items = null;    // Invalidate list of items to trigger re-query.
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
            try {
                initFitbitController();
                startDate = getStartSyncDate();
                endDate = getEndSyncDate();
            } catch (HermesException ex) {
                FacesContext.getCurrentInstance().validationFailed();
                log.log(Level.SEVERE, "initSynchronizationDates() - Error al obtener el rango de fechas de sincronización", ex);
                FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
            }
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
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Info"), ex.getMessage()));
        }

        return endSyncDate;
    }

    private void initFitbitController() throws HermesException {
        if (selected != null) {
            if (selected.hasFitbitCredentials()) {
                hermesFitbitController = new HermesFitbitController(selected);
            } else {
                throw new HermesException("Fitbit.info.PersonWithoutCredentials");
            }
        } else {
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/Bundle").getString("Info"), ResourceBundle.getBundle("/Bundle").getString("ListPersonNotSelected")));
        }
    }

    public void authorize(String nextPage) {
        try {
            initFitbitController();
            authorizeUrl = hermesFitbitController.getAuthorizeURL((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(), nextPage);
        } catch (HermesException ex) {
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
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
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.invalidTokens")));
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
                // Comprobamos si está rellena la información necesaria y si no, la rellenamos con valores por defecto.
                fillDefaultPerson();
                create();
                //update();
                // Invocamos el formulario de edición de usuario, para que el usuario pueda corregir sus datos.
//                RequestContext.getCurrentInstance().execute("PF('PersonEditDialog').show()");
                return true;
            }
        } catch (HermesException ex) {
            log.log(Level.SEVERE, "register() - Error al completar el registro", ex);
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
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
        try {
            log.log(Level.INFO, "synchronize() - Sincronización manual con Fitbit de la persona {0}", selected.toString());
            initFitbitController();
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
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ex.getMessage()));
        } catch (ParseException ex) {
            log.log(Level.SEVERE, "synchronize() - Error al sincronizar los datos de Fitbit", ex);
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Fitbit.error.parsingDate")));
        }

        selected = null;
        items = null;
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
            prepareCreate();
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
            log.log(Level.SEVERE, "sendToZtreamy() - Error en la URL", ex);
        } catch (IOException ex) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("Ztreamy.error"));
            log.log(Level.SEVERE, "sendToZtreamy() - Error de I/O", ex);
        }

        FacesContext.getCurrentInstance().addMessage("messages", message);
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
            LocalDate start = new LocalDate(startDate);
            LocalDate end = new LocalDate(endDate);
            int days = Days.daysBetween(start, end).getDays();

            chartMonthActivityLogList = selected.getActivityLogCollection(startDate, endDate, Constants.TimeAggregations.Days.toString());

            LinkedHashMap<Date, Integer> values = new LinkedHashMap();
            if (chartMonthActivityLogList != null && chartMonthActivityLogList.size() > 0) {
                for (ActivityLog activityLog : chartMonthActivityLogList) {
                    values.put(activityLog.getDate(), activityLog.getTotal());
                }
            }

            if (values.size() < days) {
                LocalDate tempDate;

                if (values.isEmpty()) {
                    tempDate = new LocalDate(start.plusDays(-1));
                } else {
                    tempDate = new LocalDate(values.keySet().iterator().next());
                }

                int total = days - values.size();

                // No hay datos para todo el rango indicado. Para evitar que no se pueda representar, añadimos datos con 0 en los restantes.
                for (int i = 0; i <= total; i++) {
                    tempDate = tempDate.plusDays(1);
                    values.put(tempDate.toDate(), 0);
                }
            }

            return selected.getLineModel(values, Constants.df.format(startDate) + " - " + Constants.df.format(endDate));
        }

        return null;
    }

//    public BarChartModel getSessionsBarChartModel() {
//        if (selected != null) {
//            // Para el gráfico de sesiones de la persona daremos los siguientes parámetros:
//            // - Fecha de inicio...: Primer día de la semana
//            // - Fecha de fin......: Último día de la semana
//            // - Agregación........: Por días
//            if (startDate == null && endDate == null) {
//                LocalDate localDate = new LocalDate();
//                startDate = localDate.withDayOfWeek(DateTimeConstants.MONDAY).toDate();
//                endDate = localDate.withDayOfWeek(DateTimeConstants.SUNDAY).toDate();
//            }
//            
//            LocalDate start = new LocalDate(startDate);
//            LocalDate end = new LocalDate(endDate);
//            int days = Days.daysBetween(start, end).getDays();
//
//            List<ActivityLog> weekActivityList = selected.getActivityLogCollection(startDate, endDate, Constants.TimeAggregations.Days.toString());
//
//            LinkedHashMap<Date, Integer> values = new LinkedHashMap();
//            LinkedHashMap<Date, Integer> sessions = new LinkedHashMap();
//            LinkedHashMap<Date, Integer> continuousSteps = new LinkedHashMap();
//            
//            if (weekActivityList != null && weekActivityList.size() > 0) {
//                for (ActivityLog activityLog : weekActivityList) {
//                    values.put(activityLog.getDate(), activityLog.getTotal());
//                    continuousSteps.put(activityLog.getDate(), activityLog.getSessionsContinuousStepsTotal());
//                    sessions.put(activityLog.getDate(), activityLog.getSessionsTotal());
//                }
//            }
//            
//            if (values.size() < days) {
//                LocalDate tempDate;
//
//                if (values.isEmpty()) {
//                    tempDate = new LocalDate(start.plusDays(-1));
//                } else {
//                    tempDate = new LocalDate(values.keySet().iterator().next());
//                }
//
//                int total = days - values.size();
//                
//                // No hay datos para todo el rango indicado. Para evitar que no se pueda representar, añadimos datos con 0 en los restantes.
//                for (int i = 0; i <= total; i++) {
//                    tempDate = tempDate.plusDays(1);
//                    values.put(tempDate.toDate(), 0);
//                    continuousSteps.put(tempDate.toDate(), 0);
//                    sessions.put(tempDate.toDate(), 0);
//                }
//            }
//
//            return selected.getSessionsBarChartModel(values, continuousSteps, sessions, Constants.df.format(startDate) + " - " + Constants.df.format(endDate));
//        }
//
//        return null;
//    }
    public BarChartModel getSessionsBarChartModel() {
        if (selected != null) {
            // Para el gráfico de sesiones de la persona daremos los siguientes parámetros:
            // - Fecha de inicio...: Primer día de la semana
            // - Fecha de fin......: Último día de la semana
            // - Agregación........: Por días
            if (startDate == null && endDate == null) {
                LocalDate localDate = new LocalDate();
                startDate = localDate.withDayOfWeek(DateTimeConstants.MONDAY).toDate();
                endDate = localDate.withDayOfWeek(DateTimeConstants.SUNDAY).toDate();
            }

            LocalDate start = new LocalDate(startDate);
            LocalDate end = new LocalDate(endDate);

            List<ActivityLog> weekActivityList = selected.getActivityLogCollection(startDate, endDate, Constants.TimeAggregations.Days.toString());

            LinkedHashMap<Date, Integer> activeSessions = new LinkedHashMap();
            LinkedHashMap<String, Integer> activeSessionsSteps = new LinkedHashMap();
            LinkedHashMap<String, Integer> continuousSteps = new LinkedHashMap();

            if (weekActivityList != null && weekActivityList.size() > 0) {
                for (ActivityLog activityLog : weekActivityList) {
                    activeSessions.putAll(activityLog.getActiveSessions());
                }
                if (activeSessions.size() > 0) {
                    Iterator it = activeSessions.keySet().iterator();
                    Date currentDate = (Date) it.next();
                    DateTime previousDate = new DateTime(currentDate);
                    StringBuilder sb = new StringBuilder(Constants.df.format(currentDate));
                    sb.append(" (");
                    sb.append(Constants.dfTime.format(currentDate));

                    int activeSessionTotalSteps = activeSessions.get(currentDate);
                    int activeSessionNonStopSteps = activeSessions.get(currentDate);
                    int currentNonStopStepsAmount = activeSessions.get(currentDate);
                    boolean inSession = true;
                    while (it.hasNext()) {
                        currentDate = (Date) it.next();
                        DateTime tempCurrentDate = new DateTime(currentDate);

                        if (previousDate.plusMinutes(1).equals(tempCurrentDate)) {
                            if (!inSession) {
                                inSession = true;
                                sb.append(Constants.df.format(previousDate.toDate()));
                                sb.append(" (");
                                sb.append(Constants.dfTime.format(previousDate.toDate()));
                            }
                            int currentSteps = activeSessions.get(currentDate);
                            activeSessionTotalSteps += currentSteps;
                            currentNonStopStepsAmount += currentSteps;
                            if (currentSteps == 0) {
                                if (currentNonStopStepsAmount > activeSessionNonStopSteps) {
                                    activeSessionNonStopSteps = currentNonStopStepsAmount;
                                }
                                currentNonStopStepsAmount = 0;
                            }
                        } else if (inSession) {
                            // Fin de la sesión
                            sb.append(" - ");
                            sb.append(Constants.dfTime.format(previousDate.toDate()));
                            sb.append(")");
                            activeSessionsSteps.put(sb.toString(), activeSessionTotalSteps);
                            continuousSteps.put(sb.toString(), activeSessionNonStopSteps);
                            activeSessionTotalSteps = 0;
                            activeSessionNonStopSteps = 0;
                            currentNonStopStepsAmount = 0;
                            sb.setLength(0);
                            inSession = false;
                        }
                        previousDate = tempCurrentDate;
                    }
                }
            } else {
                LocalDate tempDate = new LocalDate(start.plusDays(-1));
                for (int i = 0; i < 7; i++) {
                    tempDate = tempDate.plusDays(1);
                    continuousSteps.put(Constants.df.format(tempDate.toDate()), 0);
                    activeSessionsSteps.put(Constants.df.format(tempDate.toDate()), 0);
                }
            }

            return selected.getSessionsBarChartModel(activeSessionsSteps, continuousSteps, Constants.df.format(startDate) + " - " + Constants.df.format(endDate));
        }

        return null;
    }

    public boolean hasPreviousMonth() {
        LocalDate localDate = new LocalDate(startDate);
        localDate = localDate.plusMonths(-1);
        return selected.getActivityLogCollection(localDate.dayOfMonth().withMinimumValue().toDate(), localDate.dayOfMonth().withMaximumValue().toDate(), null).isEmpty();
    }

    public boolean hasNextMonth() {
        LocalDate localDate = new LocalDate(startDate);
        localDate = localDate.plusMonths(1);
        return selected.getActivityLogCollection(localDate.dayOfMonth().withMinimumValue().toDate(), localDate.dayOfMonth().withMaximumValue().toDate(), null).isEmpty();
    }

    public boolean hasPreviousWeek() {
        LocalDate localDate = new LocalDate(startDate);
        localDate = localDate.plusWeeks(-1);
        return selected.getActivityLogCollection(localDate.dayOfWeek().withMinimumValue().toDate(), localDate.dayOfWeek().withMaximumValue().toDate(), null).isEmpty();
    }

    public boolean hasNextWeek() {
        LocalDate localDate = new LocalDate(startDate);
        localDate = localDate.plusWeeks(1);
        return selected.getActivityLogCollection(localDate.dayOfMonth().withMinimumValue().toDate(), localDate.dayOfMonth().withMaximumValue().toDate(), null).isEmpty();
    }

    public void previousMonthChart() {
        addMonthMonth(-1);
    }

    public void nextMonthChart() {
        addMonthMonth(1);
    }

    public void previousWeekChart() {
        addWeek(-1);
    }

    public void nextWeekChart() {
        addWeek(1);
    }

    public void updateStartDate(SelectEvent selectEvent) {
        Date selectedDate = (Date) selectEvent.getObject();
        if (!selectedDate.after(endDate)) {
            startDate = selectedDate;
        }
    }

    public void updateEndDate(SelectEvent selectEvent) {
        Date selectedDate = (Date) selectEvent.getObject();
        if (!selectedDate.before(startDate)) {
            endDate = selectedDate;
        }
    }

    public int getDateSelector() {
        return dateSelector;
    }

    public void setDateSelector(int dateSelector) {
        this.dateSelector = dateSelector;
    }

    private void addMonthMonth(int months) {
        LocalDate start = new LocalDate(startDate);
        start = start.plusMonths(months);
        start = start.dayOfMonth().withMinimumValue();
        startDate = start.toDate();
        LocalDate end = new LocalDate(start);
        end = end.dayOfMonth().withMaximumValue();
        endDate = end.toDate();
    }

    private void addWeek(int weeks) {
        LocalDate start = new LocalDate(startDate);
        start = start.plusWeeks(weeks);
        start = start.dayOfWeek().withMinimumValue();
        startDate = start.toDate();
        LocalDate end = new LocalDate(start);
        end = end.dayOfWeek().withMaximumValue();
        endDate = end.toDate();
    }

    public void itemSelect(ItemSelectEvent event) {
        try {
            selectedActivity = chartMonthActivityLogList.get(event.getItemIndex());
            log.log(Level.INFO, "itemSelect() - Se ha seleccionado el día: {0}", Constants.df.format(selectedActivity.getDate()));
            RequestContext.getCurrentInstance().execute("PF('ActivityLogSessionsChartDialog').show()");
        } catch (IndexOutOfBoundsException ex) {
            selectedActivity = null;
        }
    }

    public ActivityLog getSelectedActivity() {
        return selectedActivity;
    }

    public LineChartModel getActivityLogSessionsChartModel() {
        if (selectedActivity != null) {
            return selectedActivity.getAreaModel(Constants.df.format(selectedActivity.getDate()));
        }

        return null;
    }

    public LineChartModel getActivityLogLineChartModel() {
        if (selectedActivity != null) {
            return selectedActivity.getLineModel(selectedActivity.getValues(), Constants.df.format(selectedActivity.getDate()));
        }

        return null;
    }

    public void onRowSelect(SelectEvent event) throws IOException {
        activityLogController.initListFromPerson(selected.getPersonId());
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(ec.getRequestContextPath() + "/faces/secured/activityLog/List.xhtml");

        // JYFR - Otra alternativa, pero mejor usar la de arriba.
//        FacesContext fc = FacesContext.getCurrentInstance();
//        NavigationHandler nh = fc.getApplication().getNavigationHandler();
//        nh.handleNavigation(fc, null, "/faces/secured/activityLog/List.xhtml?faces-redirect=true");
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
                log.log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Person.class.getName()});
                return null;
            }
        }

    }

    public HermesFitbitController getHermesFitbitController() {
        return hermesFitbitController;
    }

    public StreamedContent getFile() {
        return new CSVUtil<Person>().getData(prepareCreate(), this);
    }

    public void handleFileUpload(FileUploadEvent event) {
        // TODO: INTERNACIONALIZAR!!!
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage("messages", msg);

        new CSVUtil<Person>().setData(prepareCreate(), this, event.getFile());

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
