/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.person;

import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Jorge Yago
 */
@FacesValidator("es.jyago.hermes.person.UsernameValidator")
public class UsernameValidator implements Validator {

    private static final Logger log = Logger.getLogger(UsernameValidator.class.getName());

    @EJB
    private es.jyago.hermes.person.PersonFacade ejbFacade;

    public UsernameValidator() {
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

    }

}
