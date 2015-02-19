package es.jyago.hermes;

import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named("reportController")
@SessionScoped
public class ReportController implements Serializable {

    private List<Report> items = null;
    private Report selected;
    private JasperPrint jasperPrint;
    @EJB
    private es.jyago.hermes.person.PersonFacade personFacade;
//    @EJB
//    private es.jyago.amp.EventoFacade eventoFacade;

    public ReportController() {
    }

    public Report getSelected() {
        return selected;
    }

    public void setSelected(Report selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    public List<Report> getItems() {
        if (items == null) {
            items = new ArrayList<Report>();

            // TODO: Leer los report del directorio.
//            Report i1 = new Report();
//            i1.setDescripcion("Listado para firmar de alumnos");
//            i1.setUrl("reportAlumnos.jasper");
//            items.add(i1);
//
//            Report i2 = new Report();
//            i2.setDescripcion("Listado de eventos");
//            i2.setUrl("reportEventos.jasper");
//
//            items.add(i2);
        }
        return items;
    }

    public Report getReport(String url) {
//        return getFacade().find(id);
        // TODO: Obtener el informe a través de la url.
        return null;
    }

//    @FacesConverter(forClass = Informe.class)
//    public static class InformeControllerConverter implements Converter {
//
//        @Override
//        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
//            if (value == null || value.length() == 0) {
//                return null;
//            }
//            InformeController controller = (InformeController) facesContext.getApplication().getELResolver().
//                    getValue(facesContext.getELContext(), null, "informeController");
//            return controller.getInforme(value);
//        }
//
//        @Override
//        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
//            if (object == null) {
//                return null;
//            }
//            if (object instanceof Informe) {
//                Informe o = (Informe) object;
//                return o.getUrl();
//            } else {
//                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), TipoEvento.class.getName()});
//                return null;
//            }
//        }
//
//    }
    private void initJasper() throws JRException {

        // TODO: ¡Arreglar!
        JRBeanCollectionDataSource beanCollectionDataSource = null;
        if (selected.getDescripcion().contains("alumno")) {
            List<Person> listAlumnos = personFacade.getEntityManager().createNamedQuery("Person.findByIdRole")
                    .setParameter("idRol", Constants.USER_ROLE)
                    .getResultList();

            beanCollectionDataSource = new JRBeanCollectionDataSource(listAlumnos);
        }
        else
        {
//            List<Evento> listEventos = eventoFacade.findAll();
//            beanCollectionDataSource = new JRBeanCollectionDataSource(listEventos);
        }
            
        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/secured/resources/reports/" + selected.getUrl());
        jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap(), beanCollectionDataSource);
    }

    public void PDF() throws JRException, IOException {
        initJasper();
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=informe.pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();
    }
}
