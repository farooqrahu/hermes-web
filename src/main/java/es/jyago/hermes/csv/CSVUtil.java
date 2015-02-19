/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.csv;

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
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Jorge Yago
 */
public class CSVUtil<T> {

    public StreamedContent getData(CSVBeanInterface bean, CSVControllerInterface<T> ci) {
        ICsvBeanWriter beanWriter = null;
        InputStream is = null;
        StreamedContent file = null;

        try {
            // Creamos un archivo temporal para procesar los elementos.
            File temporal = File.createTempFile("temporal", ".csv");
            temporal.deleteOnExit();

            beanWriter = new CsvBeanWriter(new FileWriter(temporal), CsvPreference.STANDARD_PREFERENCE);

            // Seleccionamos los atributos que vamos a exportar.
            final String[] fields = bean.getFields();

            // Aplicamos las características de los campos.
            final CellProcessor[] processors = bean.getProcessors();

            // Ponemos la cabecerra con los nombres de los atributos.
            beanWriter.writeHeader(fields);

            // Procesamos los elementos.
            for (final T elemento : ci.getItems()) {
                beanWriter.write(elemento, fields, processors);
            }

            // Creamos el archivo con los datos que vamos a devolver.
            is = new FileInputStream(temporal);
            file = new DefaultStreamedContent(is, "text/plain", "lista.csv");
        } catch (Exception ex) {
            Logger.getLogger(CSVUtil.class.getName()).log(Level.SEVERE, "Error al exportar a CSV", ex);
        } finally {
            // Cerramos.
            if (beanWriter != null) {
                try {
                    beanWriter.close();
                } catch (IOException ex) {
                    Logger.getLogger(CSVUtil.class.getName()).log(Level.SEVERE, "Error al cerrar el 'writer'", ex);
                }
            }
        }

        return file;
    }

    public void setData(CSVBeanInterface bean, CSVControllerInterface<T> ci, UploadedFile file) {

        ICsvBeanReader beanReader = null;

        try {
            beanReader = new CsvBeanReader(new InputStreamReader(file.getInputstream()), CsvPreference.STANDARD_PREFERENCE);

            // Seleccionamos los atributos que vamos a importar.
            final String[] fields = bean.getFields();

            // Aplicamos las características de los campos.
            final CellProcessor[] processors = bean.getProcessors();

            // TODO: Ignorar las cabeceras?
            beanReader.getHeader(true);

            // Procesamos los elementos.
            T elemento = null;
            while ((elemento = (T) beanReader.read(bean.getClass(), fields, processors)) != null) {
                Logger.getLogger(CSVUtil.class.getName()).log(Level.INFO, String.format("Línea=%s", beanReader.getLineNumber()));
                ci.processReadElement(elemento);
            }
        } catch (Exception ex) {
            Logger.getLogger(CSVUtil.class.getName()).log(Level.SEVERE, "Error al importar de CSV", ex);
        } finally {
            if (beanReader != null) {
                try {
                    beanReader.close();
                } catch (IOException ex) {
                    Logger.getLogger(CSVUtil.class.getName()).log(Level.SEVERE, "Error al cerrar el 'reader'", ex);
                }
            }
        }
    }
}
