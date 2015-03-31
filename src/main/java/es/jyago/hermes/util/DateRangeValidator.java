/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.util;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.joda.time.LocalDate;

/**
 * Clase de validación de un rango de fechas. Los atributos deben ser
 * 'startDate' y 'endDate'.
 *
 * @author Jorge Yago
 */
@FacesValidator("dateRangeValidator")
public class DateRangeValidator implements Validator {

    /**
     * Validación de un rango de fechas. Si 'endDate' es nulo -> OK Si 'endDate'
     * tiene valor y 'startDate' es nulo -> OK Si 'endDate' es posterior a
     * 'startDate' -> OK
     *
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // Si la fecha de fin no tiene valor, la validación es correcta.
        if (value == null) {
            return;
        }

        // Si la fecha de fin tiene valor, pero la fecha de inicio no tiene valor, la validación es correcta.
        Object startDateValue = component.getAttributes().get("startDate");
        if (startDateValue == null) {
            return;
        }

        LocalDate startDate = new LocalDate(startDateValue);
        LocalDate endDate = new LocalDate(value);
        // Si la fecha de inicio es anterior a la fecha de fin, la validación es correcta.
        // En otro caso, se lanza la excepción.
        if (endDate.isBefore(startDate)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("EndDate.error.MustBeLaterStartDate")));
        }
    }
}
