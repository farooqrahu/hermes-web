package es.jyago.hermes.util;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 *
 * @author Jorge Yago
 */
public class Util {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String ALPHANUMERIC = "^[a-zA-Z0-9]*$";

    public static String minutesToTimeString(int minutes) {

        long hours = TimeUnit.MINUTES.toHours(minutes);
        long remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%02d:%02d", hours, remainMinutes);
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.length() == 0) {
            return false;
        }

        return email.matches(EMAIL_PATTERN);
    }

    public static boolean isAlphaNumeric(String s) {
        if (s.matches(ALPHANUMERIC)) {
            return true;
        }
        
        return false;
    }
}
