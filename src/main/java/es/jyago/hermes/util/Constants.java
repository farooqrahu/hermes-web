package es.jyago.hermes.util;

import java.text.SimpleDateFormat;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class Constants {

    public static final String VERSION = "v0.2.1";
    
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

    public String getVersion() {
        return VERSION;
    }
}
