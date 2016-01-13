package es.jyago.hermes.util;

import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.configuration.ConfigurationController;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

// JYFR: Tiene que ser @ManagedBean en lugar de @Named para que pueda gestionarlo PrimeFaces.
@ManagedBean
@ApplicationScoped
public class Constants {

    @ManagedProperty(value = "#{configurationController}")
    private static ConfigurationController configurationController;

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    public static final int ADMINISTRATOR_ROLE = 1;
    public static final int USER_ROLE = 2;
    public static final int DOCTOR_ROLE = 3;

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
    public static final SimpleDateFormat dfFitbitFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSS");
    public static final SimpleDateFormat dfMonthYear = new SimpleDateFormat("MMMM yyyy");
    public static final SimpleDateFormat dfTimeGMT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat dfMonth = new SimpleDateFormat("yyyy-MM");
    
    public static final Date FITBIT_RELEASE_DATE = new Date(1367366400000l); // 01/05/2013 Fitbit estrenó su pulsera en mayo de 2013. No puede haber datos anteriores a esa fecha.

    public Constants() {
        dfTimeGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    public String getVersion() {
        return getConfigurationValueByKey("Version");
    }

    public boolean isDebug() {
        return configurationController != null ? Boolean.valueOf(configurationController.getItemByKey("Debugging").getOptionValue()) : false;
    }

    public void setConfigurationController(ConfigurationController configurationController) {
        Constants.configurationController = configurationController;
    }

    public static String getConfigurationValueByKey(String key) {
        return configurationController != null ? configurationController.getValueFromItemByKey(key) : null;
    }

    public static Configuration getConfigurationByKey(String key) {
        return configurationController != null ? configurationController.getItemByKey(key) : null;
    }

    public static void setConfigurationValueByKey(String key, String value) {
        Configuration c = getConfigurationByKey(key);
        c.setOptionValue(value);
        configurationController.setSelected(c);
        configurationController.update(false);
    }
}
