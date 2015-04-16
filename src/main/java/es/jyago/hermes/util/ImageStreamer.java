/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.util;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * ManagedBean para obtener las fotos de las personas como 'StreamedContent' a
 * partir de los 'blob' de la base de datos.
 *
 * @author Jorge Yago
 */
// JYFR: Tiene que ser @ManagedBean en lugar de @Named para que pueda gestionarlo PrimeFaces.
@ManagedBean
@ApplicationScoped
public class ImageStreamer {

    @EJB
    private PersonFacade facade;

    public StreamedContent getPhotoImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // Se está renderizando la vista. Devolveremos un 'StreamedContent' inicializado para generar la URL y que no de fallo.
            return new DefaultStreamedContent();
        } else {
            // Ahora el navegador es cuando está pidiendo la imagen. Devolvemos el 'StreamedContent' real con la imagen.
            String id = context.getExternalContext().getRequestParameterMap().get("personId");
            // FIXME: Función alternativa que no coja la imagen de la B.D. sino la que tenga la persona actualmente.
            return getPhotoImageAsStreamedContent(facade.find(Integer.valueOf(id)));
        }
    }

    private StreamedContent getPhotoImageAsStreamedContent(Person person) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // Se está renderizando la vista. Devolveremos un 'StreamedContent' inicializado para generar la URL y que no de fallo.
            return new DefaultStreamedContent();
        } else {
            // Ahora el navegador es cuando está pidiendo la imagen. Devolvemos el 'StreamedContent' real con la imagen.
            byte[] image = person.getPhoto();

            // Si no tenemos imagen en la base de datos, pondemos una imagen por defecto.
            if (image != null) {
                return new DefaultStreamedContent(new ByteArrayInputStream(image));
            } else {
                return new DefaultStreamedContent(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/img/no_photo.png"));
            }
        }
    }
}
