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

    private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static String minutesToTimeString(int minutes) {

        long hours = TimeUnit.MINUTES.toHours(minutes);
        long remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%02d:%02d", hours, remainMinutes);
    }

    public static boolean validateEmail(String email) {
        if (email == null || email.length() == 0) {
            return false;
        }

        return pattern.matcher(email).matches();
    }
}
