package es.jyago.hermes.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Excepción controlada para Hermes. Su uso será para poder mostrar mensajes informativos internacionalizados
 * al usuario, de posibles excepciones de cualquier tipo que pudieran ocurrir en el sistema.
 *
 * @author Jorge Yago
 */
public class HermesException extends Exception {

    private static ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");

    public HermesException() {
    }

    public HermesException(String key) {
        super(bundle.getString(key));
    }

    public HermesException(String key, Object... params) {
        super(MessageFormat.format(bundle.getString(key), params));
    }
}
