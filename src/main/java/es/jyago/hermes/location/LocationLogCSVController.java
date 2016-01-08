/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.location;

import es.jyago.hermes.csv.ICSVController;
import es.jyago.hermes.csv.CSVUtil;
import es.jyago.hermes.location.detail.LocationLogDetail;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.model.UploadedFile;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Jorge Yago
 */
public class LocationLogCSVController implements ICSVController<LocationLogCSV> {

    private static final Logger log = Logger.getLogger(LocationLogCSVController.class.getName());

    private UploadedFile locationLogFile;
    private final LocationLog locationLog;

    public LocationLogCSVController() {
        locationLog = new LocationLog();
    }

    public void processFile(UploadedFile file) throws HermesException {
        this.locationLogFile = file;

        // Procesamos el archivo CSV.
        if (locationLogFile != null) {
            try {
                // Obtenemos la fecha.
                locationLog.setDateLog(Constants.dfSmartDriver.parse(locationLogFile.getFileName().replaceAll("_.*", "")));
                // Intentamos leer el archivo usando ';' como separador.
                log.log(Level.INFO, "processFile() - Lectura usando ';' como separador");
                new CSVUtil<LocationLogCSV>().setData(new LocationLogCSV(), this, locationLogFile, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE, false);
            } catch (Exception ex) {
                if (ex instanceof HermesException) {
                    try {
                        // Como no hemos podido leerlo, probamos esta vez con ',' como separador.
                        log.log(Level.INFO, "processFile() - Lectura usando ',' como separador ");
                        new CSVUtil<LocationLogCSV>().setData(new LocationLogCSV(), this, locationLogFile, CsvPreference.STANDARD_PREFERENCE, false);
                    } catch (Exception ex2) {
                        if (ex2.getCause() == null) {
                            throw new HermesException("InvalidFile", file.getFileName());
                        } else {
                            log.log(Level.SEVERE, MessageFormat.format("processFile() - Error al importar el archivo: {0}", file.getFileName()), ex.getMessage());
                            throw new HermesException("InvalidFile", file.getFileName());
                        }
                    }
                } else {
                    log.log(Level.SEVERE, MessageFormat.format("processFile() - Error al importar el archivo: {0}", file.getFileName()), ex.getMessage());
                    throw new HermesException("InvalidFile", file.getFileName());
                }
            }
        }
    }

    @Override
    public List<LocationLogCSV> getItems() {
        // No necesitamos devolver los elementos leídos del archivo CSV.
        return null;
    }

    @Override
    public void processReadElement(LocationLogCSV element) throws HermesException {
        try {
            log.log(Level.FINEST, "processReadElement() - Procesando elemento {0}", element.getDateTimeLog());
            LocationLogDetail locationLogDetail = new LocationLogDetail();
            if (element.getDateTimeLog().split(",").length > 1) {
                throw new HermesException();
            }

            locationLogDetail.setTimeLog(Constants.dfTimeSmartDriver.parse(element.getDateTimeLog().replaceAll(".*_", "")));
            locationLogDetail.setLatitude(element.getLatitude());
            locationLogDetail.setLongitude(element.getLongitude());
            locationLogDetail.setHeartRate(element.getHeartRate());
            locationLogDetail.setSpeed(element.getSpeed());
            locationLogDetail.setLocationLog(locationLog);

            locationLog.getLocationLogDetailList().add(locationLogDetail);
        } catch (ParseException ex) {
            log.log(Level.SEVERE, "processReadElement() - Error al procesar el elemento: {0}", ex.getMessage());
        }
    }

    public LocationLog getLocationLog() {
        return locationLog;
    }
}
