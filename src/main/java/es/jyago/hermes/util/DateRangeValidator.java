/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.util;

import java.util.Date;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
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
     * @param dateValue
     * @throws ValidatorException
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object dateValue) throws ValidatorException {
        // Si la fecha es nula, la validación es correcta porque no hay nada que validar.
        if (dateValue == null) {
            return;
        }

        // Comprobamos si el componente que solicita la validación es el de fecha de inicio o el de fecha de fin.
        Date endDateValue = (Date) component.getAttributes().get("endDate");
        Date startDateValue = (Date) component.getAttributes().get("startDate");

        if (endDateValue == null && startDateValue == null) {
            return;
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (startDateValue != null) {
            startDate = new LocalDate(startDateValue);
            endDate = new LocalDate(dateValue);
        } else if (endDateValue != null) {
            startDate = new LocalDate(dateValue);
            endDate = new LocalDate(endDateValue);
        } else {
            return;
        }

        // Si la fecha de inicio es anterior a la fecha de fin, la validación es correcta.
        // En otro caso, se lanza la excepción.
        if (endDate.isBefore(startDate)) {
            FacesContext.getCurrentInstance().validationFailed();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ResourceBundle.getBundle("/Bundle").getString("Error"), ResourceBundle.getBundle("/Bundle").getString("EndDate.error.MustBeLaterStartDate")));
        }
    }
}
