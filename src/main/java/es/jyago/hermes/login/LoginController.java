package es.jyago.hermes.login;

import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.bean.LocaleBean;
import es.jyago.hermes.bean.ThemeBean;
import es.jyago.hermes.contextLog.ContextLog;
import es.jyago.hermes.healthLog.HealthLog;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonController;
import es.jyago.hermes.person.configuration.PersonConfiguration;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.util.TemplateBean;
import es.jyago.hermes.util.Util;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpSession;
import org.primefaces.extensions.component.gchart.model.GChartModel;
import org.primefaces.extensions.component.gchart.model.GChartModelBuilder;
import org.primefaces.extensions.component.gchart.model.GChartType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.PieChartModel;

// JYFR: Tiene que ser @ManagedBean en lugar de @Named para que pueda gestionarlo PrimeFaces.
@ManagedBean(name = "loginController")
@SessionScoped
public class LoginController implements Serializable {

    private static final Logger log = Logger.getLogger(LoginController.class.getName());
    private String username;
    private String password;
    private Person person;
    @ManagedProperty("#{bundle}")
    private ResourceBundle bundle;
    @ManagedProperty(value = "#{personController}")
    private PersonController personController;
    @ManagedProperty(value = "#{templateBean}")
    private TemplateBean templateBean;
    private String dateSummary;

//    private HorizontalBarChartModel welcomeAndRegisterBarModel;
    private GChartModel welcomeAndRegisterBarModel;
    private Date selectedDate;
    private ActivityLog selectedActivityLog;
    private SleepLog selectedSleepLog;
    private HealthLog selectedHealthLog;
    private ContextLog selectedContextLog;

    private LineChartModel stepsChartModel;
    private BarChartModel sleepChartModel;
    private LineChartModel heartRateChartModel;
    private PieChartModel contextChartModel;
    private GChartModel geoChartModel;

    public PersonController getPersonController() {
        return personController;
    }

    public void setPersonController(PersonController personController) {
        this.personController = personController;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() {
        try {
            if (getUserFromDatabase()) {
                setSelectedDate(person.getLastSynchronization() != null ? person.getLastSynchronization() : new Date());

                // Gráfico de resumen de accesos.
                initWelcomeAndRegisterBarModel();

                // Gráfico de resumen de accesos por ubicación geográfica.
                initGeoChartModel();

                // Gráfico de pasos del día.
                initStepsChartModel();

                // Gráfico de sueño del día.
                initSleepChartModel();

                // Gráfico de ritmo cardíaco del día.
                initHeartRateChartModel();

                personController.setSelected(person);
                templateBean.setPage("/main.xhtml");
                FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("Welcome"), person.getFullName()));
            } else {
                log.log(Level.SEVERE, "login() - Credenciales de acceso incorrectas");
            }
        } catch (HermesException ex) {
            log.log(Level.SEVERE, "login() - Credenciales de acceso incorrectas");
            FacesContext.getCurrentInstance().validationFailed();
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("InvalidCredentials"), ""));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "login() - Error al acceder al sistema", ex);
            FacesContext.getCurrentInstance().validationFailed();
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("InvalidCredentials"), ""));
        }
        
        return "index?faces-redirect=true";
    }

//    private void initWelcomeAndRegisterBarModel() {
//        welcomeAndRegisterBarModel = new HorizontalBarChartModel();
//        Integer welcomeHitsValue = Integer.parseInt(Constants.getConfigurationValueByKey("WelcomeHits"));
//        Integer registerHitsValue = Integer.parseInt(Constants.getConfigurationValueByKey("RegisterHits"));
//
//        ChartSeries welcomeHits = new ChartSeries();
//        welcomeHits.setLabel(bundle.getString("WelcomeHits"));
//        welcomeHits.set(bundle.getString("WelcomeHits"), welcomeHitsValue);
//
//        ChartSeries registerHits = new ChartSeries();
//        registerHits.setLabel(bundle.getString("RegisterHits"));
//        registerHits.set(bundle.getString("RegisterHits"), registerHitsValue);
//
//        welcomeAndRegisterBarModel.addSeries(welcomeHits);
//        welcomeAndRegisterBarModel.addSeries(registerHits);
//        welcomeAndRegisterBarModel.setTitle(bundle.getString("WelcomeAndRegisterHitsChart"));
//        welcomeAndRegisterBarModel.setLegendPosition("se");
//
//        Axis xAxis = welcomeAndRegisterBarModel.getAxis(AxisType.X);
//        xAxis.setMin(0);
//        xAxis.setTickFormat("%d");
//        xAxis.setMax(welcomeHitsValue > registerHitsValue ? welcomeHitsValue + 20 : registerHitsValue + 20);
//
//        // JYFR: Extensión para gráficos. Así podemos cambiar más características. Ver las opciones en la web de 'jqPlot'.
//        // En este caso, ocultamos el eje de ordenadas.
//        welcomeAndRegisterBarModel.setExtender("customExtender");
//    }
    private void initWelcomeAndRegisterBarModel() {
        Map<String, Object> legend = new HashMap<>();
        legend.put("position", "top");

        GChartModelBuilder chartBuilder = new GChartModelBuilder();
        chartBuilder.setChartType(GChartType.COLUMN);

        chartBuilder.addColumns(bundle.getString("AccessToPages"), bundle.getString("WelcomePage"), bundle.getString("RegisterPage"));

        Integer welcomeHitsValue = Integer.parseInt(Constants.getConfigurationValueByKey("WelcomeHits"));
        Integer registerHitsValue = Integer.parseInt(Constants.getConfigurationValueByKey("RegisterHits"));

        chartBuilder.addRow(bundle.getString("Access"), welcomeHitsValue, registerHitsValue);
        chartBuilder.addOption("legend", legend);

        welcomeAndRegisterBarModel = chartBuilder.build();
    }

    private void initStepsChartModel() {
        if (selectedActivityLog != null) {
            selectedActivityLog.setAggregation(Constants.TimeAggregations.Hours.toString());
            stepsChartModel = selectedActivityLog.getLineModel(null);
        } else {
            stepsChartModel = null;
        }
    }

    private void initSleepChartModel() {
        if (selectedSleepLog != null) {
            sleepChartModel = selectedSleepLog.getBarModel(null);
        } else {
            sleepChartModel = null;
        }
    }

    private void initHeartRateChartModel() {
        if (selectedHealthLog != null) {
            selectedHealthLog.setAggregation(Constants.TimeAggregations.Hours.toString());
            heartRateChartModel = selectedHealthLog.getLineModel(null);
        } else {
            heartRateChartModel = null;
        }
    }

    private void initContextChartModel() {
        if (selectedContextLog != null) {
            contextChartModel = selectedContextLog.getActivitiesPieModel();
        } else {
            contextChartModel = null;
        }
    }

    private void initGeoChartModel() {

        Map<String, Object> colorAxis = new HashMap();
        colorAxis.put("colors", new String[]{"white", "orange"});

        GChartModelBuilder gcb = new GChartModelBuilder();
        gcb.setChartType(GChartType.GEO);
        gcb.addColumns(LocaleBean.getBundle().getString("Country"), LocaleBean.getBundle().getString("Registered"));

        Map<String, Integer> values = new HashMap();

        for (Person person : personController.getItems()) {
            String country = person.getCountry();
            if (country == null || country.isEmpty()) {
                country = "ES";
            }

            Integer value = values.get(country);
            value = (value == null) ? value = 1 : value + 1;
            values.put(country, value);
        }

        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            Locale l = new Locale(entry.getKey().toLowerCase(), entry.getKey());
            gcb.addRow(l.getDisplayCountry(LocaleBean.locale), entry.getValue());
        }

        geoChartModel = gcb.addOption("colorAxis", colorAxis).build();
    }

    public void onPageLoad() {
        // JYFR: Controlamos que sólo se ejecute la primera vez que se carga la página de registro y no por el cambio de pestaña del formulario.
        if (!FacesContext.getCurrentInstance().isPostback()) {

            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            username = (String) session.getAttribute("username");
            session.removeAttribute("username");
            password = (String) session.getAttribute("password");
            session.removeAttribute("password");

            try {
                if (getUserFromDatabase()) {
                    FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("Welcome"), person.getFullName()));
                }
            } catch (HermesException ex) {
                FacesContext.getCurrentInstance().validationFailed();
            }
        }
    }

    private boolean getUserFromDatabase() throws HermesException {
        boolean ok = false;

        if (username == null || username.length() <= 0) {
            throw new HermesException("RegisterPersonRequiredMessage_username");
        } else if (password == null || password.length() <= 0) {
            throw new HermesException("RegisterPersonRequiredMessage_password");
        } else {
            try {
                person = personController.getPerson(username, password);

                if (person != null) {
                    ok = true;
                } else {
                    log.log(Level.SEVERE, "getUserFromDatabase() - Usuario nulo");
                    throw new HermesException("InvalidCredentials");
                }
            } catch (NoResultException ex) {
                log.log(Level.INFO, "getUserFromDatabase() - No existe el usuario: {0}", username);
                throw new HermesException("InvalidCredentials");
            } catch (Exception ex) {
                log.log(Level.SEVERE, "getUserFromDatabase() - Error al acceder al sistema", ex);
                throw new HermesException("InvalidCredentials");
            }
        }

        return ok;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

    public void keepSessionAlive() {
        log.log(Level.INFO, "keepSessionAlive() - Mantener activa la sesiónn del usuario: {0}", username);
    }

    public boolean isLoggedIn() {
        return person != null;
    }

    public Person getPerson() {
        return person;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        this.selectedActivityLog = this.person.getActivityLog(selectedDate);
        this.selectedSleepLog = this.person.getSleepLog(selectedDate);
        this.selectedHealthLog = this.person.getHealthLog(selectedDate);;
        this.selectedContextLog = this.person.getContextLog(selectedDate);;
        initDateSummary(selectedDate);
        initStepsChartModel();
        initSleepChartModel();
        initHeartRateChartModel();
        initContextChartModel();
    }

//    public HorizontalBarChartModel getWelcomeAndRegisterBarModel() {
//        return welcomeAndRegisterBarModel;
//    }
    public GChartModel getWelcomeAndRegisterBarModel() {
        return welcomeAndRegisterBarModel;
    }

    public GChartModel getGeoChartModel() {
        return geoChartModel;
    }

    private void initDateSummary(Date date) {
        StringBuilder sb = new StringBuilder();

        sb.append(bundle.getString("Date")).append(": ").append("<b>").append(Constants.df.format(date)).append("</b>").append("<br/>");
        sb.append("<hr/>");

        ActivityLog activityLog = person.getActivityLog(date);
        sb.append(bundle.getString("Steps")).append(": ");
        if (activityLog != null) {
            sb.append("<b>").append(activityLog.getTotal() != null ? activityLog.getTotal() : bundle.getString("NoData")).append("</b>");
            if (person.isAdmin()) {
                if (activityLog.getSendDate() != null) {
                    sb.append(" - ").append(bundle.getString("Sent")).append(": ").append("<b>").append(Constants.df.format(activityLog.getSendDate())).append("</b>");
                }
            }
            sb.append("<br/>");
        } else {
            sb.append("<b>").append(bundle.getString("NoData")).append("</b>").append("<br/>");
        }

        SleepLog sleepLog = person.getSleepLog(date);
        sb.append(bundle.getString("Sleep")).append(": ");
        if (sleepLog != null) {
            sb.append("<b>").append(Util.minutesToTimeString(sleepLog.getMinutesAsleep())).append("</b>");
            if (person.isAdmin()) {
                if (sleepLog.getSendDate() != null) {
                    sb.append(" - ").append(bundle.getString("Sent")).append(": ").append("<b>").append(Constants.df.format(sleepLog.getSendDate())).append("</b>");
                }
            }
            sb.append("<br/>");
        } else {
            sb.append("<b>").append(bundle.getString("NoData")).append("</b>").append("<br/>");
        }

        HealthLog healthLog = person.getHealthLog(date);
        sb.append(bundle.getString("AverageHeartRate")).append(": ");
        if (healthLog != null) {
            sb.append("<b>").append(healthLog.getAverage() != null ? healthLog.getAverage() : bundle.getString("NoData")).append("</b>");
            if (person.isAdmin()) {
                if (healthLog.getSendDate() != null) {
                    sb.append(" - ").append(bundle.getString("Sent")).append(": ").append("<b>").append(Constants.df.format(healthLog.getSendDate())).append("</b>");
                }
            }
            sb.append("<br/>");
        } else {
            sb.append("<b>").append(bundle.getString("NoData")).append("</b>").append("<br/>");
        }

        this.dateSummary = sb.toString();
    }

    public String getDateSummary() {
        return this.dateSummary;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public LineChartModel getStepsChartModel() {
        return stepsChartModel;
    }

    public BarChartModel getSleepChartModel() {
        return sleepChartModel;
    }

    public LineChartModel getHeartRateChartModel() {
        return heartRateChartModel;
    }

    public PieChartModel getContextChartModel() {
        return contextChartModel;
    }

    public String getCurrentTheme() {
        return this.person != null ? this.person.getTheme().getThemeName() : "pepper-grinder";
    }

    public void setTheme(String themeName) {
        this.person.setTheme(new ThemeBean(themeName));
        PersonConfiguration pc = this.person.getConfigurationHashMap().get(Person.PersonOptions.Theme.name());
        pc.setValue(themeName);
        personController.update(true);
    }

    public TemplateBean getTemplateBean() {
        return templateBean;
    }

    public void setTemplateBean(TemplateBean templateBean) {
        this.templateBean = templateBean;
    }
}
