package es.jyago.hermes.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author Jorge Yago
 */
public class InternationalizedException extends Exception {
    
    private static ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");

    public InternationalizedException() {
    }

    
    public InternationalizedException(String key) {
        super(bundle.getString(key));
    }
    
    public InternationalizedException(String key, Object... params) {
        super(MessageFormat.format(bundle.getString(key), params));
    }
}
