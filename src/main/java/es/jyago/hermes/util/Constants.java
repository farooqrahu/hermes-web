package es.jyago.hermes.util;

import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.configuration.ConfigurationController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;

@Named(value = "constants")
@Singleton
@Startup
public class Constants {

    private static final Logger LOG = Logger.getLogger(Constants.class.getName());

    @Inject
    private ConfigurationController configurationController;

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    public static final int ADMINISTRATOR_ROLE = 1;
    public static final int USER_ROLE = 2;
    public static final int DOCTOR_ROLE = 3;

    public static final int REST_ERROR = 0;
    public static final int REST_OK = 1;
    public static final int REST_ERROR_USER_NOT_FOUND = 2;
    public static final int REST_ERROR_IN_DATA = 3;
    public static final int REST_ERROR_NO_CONTEXT_DATA = 4;
    public static final int REST_ERROR_USER_EXISTS = 5;
    public static final int REST_ERROR_USER_NOT_REGISTERED = 6;
    public static final int REST_ERROR_INVALID_EMAIL = 7;
    public static final int REST_ERROR_INVALID_PASSWORD = 8;

    public static final int MONTHS_SELECTOR = 1;
    public static final int WEEKS_SELECTOR = 2;
    public static final int RANGE_SELECTOR = 3;

    private static Constants instance;

    @PostConstruct
    public void init() {
        if (instance == null) {
            LOG.log(Level.INFO, "init() - Inicialización de la instancia de las constantes del sistema");
            dfTimeGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
            instance = this;
        }
    }

    public static enum TimeAggregations {

        Minutes, Hours, Days
    }

    public static enum FitbitServices {

        Steps, Sleep, HeartRate
    }

    public static enum HermesServices {

        Steps, Sleep, HeartRate, Location, Context
    }

    public static enum TimeChecks {

        Daily, Weekly, Monthly
    }

    public TimeAggregations[] getTimeAggregations() {
        return TimeAggregations.values();
    }

    public FitbitServices[] getFitbitServices() {
        return FitbitServices.values();
    }

    public HermesServices[] getHermesServices() {
        return HermesServices.values();
    }

    public TimeChecks[] getTimeChecks() {
        return TimeChecks.values();
    }

    public static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat dfSimpleTime = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat dfSmartDriver = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat dfTimeSmartDriver = new SimpleDateFormat("HHmmss");
    public static final SimpleDateFormat dfFitbit = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat dfus = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat dfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final SimpleDateFormat dfMonthYear = new SimpleDateFormat("MMMM yyyy");
    public static final SimpleDateFormat dfTimeGMT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat dfMonth = new SimpleDateFormat("yyyy-MM");

    public static final Date FITBIT_RELEASE_DATE = new Date(1367366400000l); // 01/05/2013 Fitbit estrenó su pulsera en mayo de 2013. No puede haber datos anteriores a esa fecha.

    public String getVersion() {
//        return configurationController.getValueFromItemByKey("Version");
        // La devolveremos del código en lugar de la B.D.
        return "0.6.9";
    }

    public boolean isDebug() {
        return Boolean.valueOf(configurationController.getItemByKey("Debugging").getOptionValue());
    }

    public String getConfigurationValueByKey(String key) {
        return configurationController.getValueFromItemByKey(key);
    }

    public Configuration getConfigurationByKey(String key) {
        return configurationController.getItemByKey(key);
    }

    public void setConfigurationValueByKey(String key, String value) {
        Configuration c = getConfigurationByKey(key);
        c.setOptionValue(value);
        configurationController.setSelected(c);
        configurationController.update(false);
    }

    public static Constants getInstance() {
        return instance;
    }
}
