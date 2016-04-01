/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.csv;

import es.jyago.hermes.util.HermesException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Jorge Yago
 */
public class CSVUtil<T> {

    private static final Logger LOG = Logger.getLogger(CSVUtil.class.getName());

    public StreamedContent getData(ICSVBean bean, ICSVController<T> ci, CsvPreference csvPreference, boolean ignoreHeaders) {
        ICsvBeanWriter beanWriter = null;
        StreamedContent file = null;
        InputStream is;

        bean.init(null);

        try {
            // Creamos un archivo temporal para procesar los elementos.
            File temporal = File.createTempFile("temporal", ".csv");
            temporal.deleteOnExit();

            beanWriter = new CsvBeanWriter(new FileWriter(temporal), csvPreference);

            // Seleccionamos los atributos que vamos a exportar.
            final String[] fields = bean.getFields();

            // Aplicamos las características de los campos.
            final CellProcessor[] processors = bean.getProcessors();

            if (!ignoreHeaders) {
                // Ponemos la cabecera con los nombres de los atributos.
                if (bean.getHeaders() != null) {
                    beanWriter.writeHeader(bean.getHeaders());
                } else {
                    beanWriter.writeHeader(fields);
                }
            }

            // Procesamos los elementos.
            for (final T elemento : ci.getCSVItems()) {
                beanWriter.write(elemento, fields, processors);
            }

            // Creamos el archivo con los datos que vamos a devolver.
            is = new FileInputStream(temporal);
            file = new DefaultStreamedContent(is, "text/plain", "data.csv");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "getData() - Error al exportar a CSV", ex);
        } finally {
            // Cerramos.
            if (beanWriter != null) {
                try {
                    beanWriter.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "getData() - Error al cerrar el 'writer'", ex);
                }
            }
        }

        return file;
    }

    public void getFileData(ICSVBean bean, ICSVController<T> ci, CsvPreference csvPreference, boolean ignoreHeaders, File file) {
        ICsvBeanWriter beanWriter = null;
        InputStream is;

        bean.init(null);

        try {
            beanWriter = new CsvBeanWriter(new FileWriter(file), csvPreference);

            // Seleccionamos los atributos que vamos a exportar.
            final String[] fields = bean.getFields();

            // Aplicamos las características de los campos.
            final CellProcessor[] processors = bean.getProcessors();

            if (!ignoreHeaders) {
                // Ponemos la cabecera con los nombres de los atributos.
                if (bean.getHeaders() != null) {
                    beanWriter.writeHeader(bean.getHeaders());
                } else {
                    beanWriter.writeHeader(fields);
                }
            }

            // Procesamos los elementos.
            for (final T elemento : ci.getCSVItems()) {
                beanWriter.write(elemento, fields, processors);
            }

            // Creamos el archivo con los datos que vamos a devolver.
            is = new FileInputStream(file);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "getFileData() - Error al exportar a CSV", ex);
        } finally {
            // Cerramos.
            if (beanWriter != null) {
                try {
                    beanWriter.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "getFileData() - Error al cerrar el 'writer'", ex);
                }
            }
        }
    }

    public void setData(ICSVBean bean, ICSVController<T> ci, UploadedFile file, CsvPreference csvPreference, boolean hasHeader) throws HermesException {

        ICsvBeanReader beanReader = null;

        try {
            LOG.log(Level.INFO, "setData() - Procesar el archivo: {0}", file.getFileName());

            CellProcessor[] processors = null;
            String[] fields = null;

            ICsvListReader listReader = null;

            try {
                listReader = new CsvListReader(new InputStreamReader(file.getInputstream()), csvPreference);
                if (listReader.read() != null) {
                    // Inicializamos los atributos del bean y establecemos el mismo número de columnas que el archivo, para hacer más flexible el proceso.
                    bean.init(listReader.length());

//                    // Aplicamos las características de los campos y seleccionamos los atributos que vamos a importar.
//                    if (listReader.length() == bean.getProcessors().length) {
                    LOG.log(Level.FINEST, "setData() - Número de columnas correcto");
                    processors = bean.getProcessors();
                    fields = bean.getFields();
//                    } else {
//                        LOG.log(Level.WARNING, "setData() - Número de columnas distintas a las esperadas");
//                        // Lanzamos una excepción básica para intentar procesar el archivo de otra forma.
//                        throw new HermesException();
//
//                        List<CellProcessor> customCellProcessor = new ArrayList();
//                        List<String> customFields = new ArrayList();
//
//                        for (int i = 0; i < listReader.length(); i++) {
//                            customCellProcessor.add(bean.getProcessors()[i]);
//                            customFields.add(bean.getFields()[i]);
//                        }
//
//                        processors = customCellProcessor.toArray(new CellProcessor[customCellProcessor.size()]);
//                        fields = customFields.toArray(new String[customFields.size()]);
//                    }
                }
            } finally {
                if (listReader != null) {
                    listReader.close();
                }
            }

            beanReader = new CsvBeanReader(new InputStreamReader(file.getInputstream()), csvPreference);

            // Leer las cabeceras, si así se indica.
            String[] header = null;
            if (hasHeader) {
                header = beanReader.getHeader(hasHeader);
            }
            LOG.log(Level.INFO, "setData() - ¿Cabecera? {0}", (header != null));

            // Procesamos los elementos.
            T element;
            boolean linesLeft = true;
            int goodRows = 0;
            while (linesLeft) {
                try {
                    linesLeft = (element = (T) beanReader.read(bean.getClass(), fields, processors)) != null;
                    LOG.log(Level.FINEST, String.format("setData() - Procesando línea=%s", beanReader.getLineNumber()));
                    if (element != null) {
                        //  Procesamos el elemento sólo si no es nulo.
                        ci.processReadElement(element);
                        goodRows++;
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, String.format("setData() - La línea=%s no es válida. No se incorporarán sus datos", beanReader.getLineNumber()), ex.getMessage());
                }
            }
            if (goodRows == 0) {
                LOG.log(Level.SEVERE, "setData() - Parece que no hay ninguna línea correcta. ¿Puede ser porque tenga un formato incorrecto?");
                // Lanzamos una excepción básica para intentar procesar el archivo de otra forma.
                throw new HermesException();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "setData() - Error al importar de CSV", ex);
            throw new HermesException("LocationLogFileUploadError");
        } finally {
            if (beanReader != null) {
                try {
                    beanReader.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "setData() - Error al cerrar el 'reader'", ex);
                }
            }
        }
    }
}
