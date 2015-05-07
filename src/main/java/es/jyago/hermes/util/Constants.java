package es.jyago.hermes.util;

import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.configuration.ConfigurationController;
import java.text.SimpleDateFormat;
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

    public TimeAggregations[] getTimeAggregations() {
        return TimeAggregations.values();
    }

    public static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat dfe = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat dfFitbit = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat dfFitbitFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSS");
    public static final SimpleDateFormat dfMonthYear = new SimpleDateFormat("MMMM yyyy");

    public String getVersion() {
        return getConfigurationValueByKey("Version");
    }

    public boolean isDebug() {
        return Boolean.valueOf(configurationController.getItemByKey("Debugging").getOptionValue());
    }

    public void setConfigurationController(ConfigurationController configurationController) {
        Constants.configurationController = configurationController;
    }

    public static String getConfigurationValueByKey(String key) {
        return configurationController.getValueFromItemByKey(key);
    }

    public static Configuration getConfigurationByKey(String key) {
        return configurationController.getItemByKey(key);
    }
}
