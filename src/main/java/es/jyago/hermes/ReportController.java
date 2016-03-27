package es.jyago.hermes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Named("reportController")
@SessionScoped
public class ReportController implements Serializable {

    private static final Logger LOG = Logger.getLogger(ReportController.class.getName());

    private List<Report> items;
    private Report selected;
    private JasperPrint jasperPrint;

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
            try {
                items = new ArrayList();

                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                String reportsFolder = ec.getRealPath("/resources/report");
                LOG.log(Level.INFO, "getItems() - Listado de informes del directorio {0}", reportsFolder);

                Files.walk(Paths.get(reportsFolder)).filter(p -> p.toString().endsWith(".jasper")).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {

                        try {
                            // Obtenemos el archivo compilado de JasperReports, para coger el nombre del informe creado en iReport.
                            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new File(filePath.toString()));

                            Report report = new Report();
                            report.setFileName(filePath.getFileName().toString().replaceFirst("[.][^.]+$", ""));
                            report.setDescription(jasperReport.getName());
                            report.setUrl(filePath.toString());

                            items.add(report);
                        } catch (JRException ex) {
                            Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "getItems() - Error al obtener el listado de informes", ex);
            }
        }
        return items;
    }

    public void generatePDF(Report report) {
        selected = report;
        try {
            LOG.log(Level.INFO, "generate() - Generar el informe {0}", selected.getDescription());

            InitialContext initialContext;
            try {
                initialContext = new InitialContext();
                DataSource dataSource = (DataSource) initialContext.lookup("HermesWeb_JNDI");
                Connection connection = dataSource.getConnection();
                InputStream reportStream = new FileInputStream(selected.getUrl());
                jasperPrint = JasperFillManager.fillReport(reportStream, new HashMap(), connection);
                HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                httpServletResponse.reset();
                httpServletResponse.setContentType("application/pdf");
                httpServletResponse.setHeader("Content-disposition", "attachment; filename=" + selected.getFileName() + ".pdf");
                ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
                FacesContext.getCurrentInstance().responseComplete();

            } catch (NamingException ex) {
                Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (JRException ex) {
            Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private void initJasper() throws JRException {
//
//        // TODO: ¡Arreglar!
//        JRBeanCollectionDataSource beanCollectionDataSource = null;
//        if (selected.getDescription().contains("alumno")) {
//            List<Person> listAlumnos = personFacade.getEntityManager().createNamedQuery("Person.findByIdRole")
//                    .setParameter("idRol", Constants.USER_ROLE)
//                    .getResultList();
//
//            beanCollectionDataSource = new JRBeanCollectionDataSource(listAlumnos);
//        } else {
////            List<Evento> listEventos = eventoFacade.findAll();
////            beanCollectionDataSource = new JRBeanCollectionDataSource(listEventos);
//        }
//        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/secured/resources/reports/" + selected.getUrl());
//        jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap(), beanCollectionDataSource);
//    }
//    /**
//     * Método para generar el PDF del informe generado.
//     *
//     * @throws JRException
//     * @throws IOException
//     */
//    public void PDF() throws JRException, IOException {
//        initJasper();
//        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
//        httpServletResponse.addHeader("Content-disposition", "attachment; filename=informe.pdf");
//        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
//        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
//        FacesContext.getCurrentInstance().responseComplete();
//    }
}
