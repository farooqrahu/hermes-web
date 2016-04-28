package es.jyago.hermes.person;

import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;


@FacesValidator("es.jyago.hermes.person.UsernameValidator")
public class UsernameValidator implements Validator {

    private static final Logger LOG = Logger.getLogger(UsernameValidator.class.getName());

    @EJB
    private PersonFacade personFacade;

    public UsernameValidator() {
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // TODO: Implementar resto de validaciones, como usuario existente, nombre con caracteres extraños, etc.
        if (value == null) {
            return;
        }

    }

}
