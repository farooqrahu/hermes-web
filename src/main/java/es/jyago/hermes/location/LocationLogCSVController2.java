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


public class LocationLogCSVController2 implements ICSVController<LocationLogCSV2> {

    private static final Logger LOG = Logger.getLogger(LocationLogCSVController2.class.getName());

    private UploadedFile locationLogFile;
    private final LocationLog locationLog;

    public LocationLogCSVController2() {
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
                LOG.log(Level.INFO, "processFile() - Lectura usando ';' como separador");
                new CSVUtil<LocationLogCSV2>().setData(new LocationLogCSV2(), this, locationLogFile, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE, true);
            } catch (Exception ex) {
                if (ex instanceof HermesException) {
                    try {
                        // Como no hemos podido leerlo, probamos esta vez con ',' como separador.
                        LOG.log(Level.INFO, "processFile() - Lectura usando ',' como separador ");
                        new CSVUtil<LocationLogCSV2>().setData(new LocationLogCSV2(), this, locationLogFile, CsvPreference.STANDARD_PREFERENCE, true);
                    } catch (Exception ex2) {
                        if (ex2 instanceof HermesException) {
                            throw new HermesException("InvalidFile", file.getFileName());
                        } else {
                            LOG.log(Level.SEVERE, MessageFormat.format("processFile() - Error al importar el archivo: {0}", file.getFileName()), ex2.getMessage());
                            throw new HermesException("InvalidFile", file.getFileName());
                        }
                    }
                } else {
                    LOG.log(Level.SEVERE, MessageFormat.format("processFile() - Error al importar el archivo: {0}", file.getFileName()), ex.getMessage());
                    throw new HermesException("InvalidFile", file.getFileName());
                }
            }
        }
    }

    @Override
    public List<LocationLogCSV2> getCSVItems() {
        // No necesitamos devolver los elementos leídos del archivo CSV.
        return null;
    }

    @Override
    public void processReadElement(LocationLogCSV2 element) throws HermesException {
        try {
            LOG.log(Level.FINEST, "processReadElement() - Procesando elemento {0}", element.getDateTimeLog());
            LocationLogDetail locationLogDetail = new LocationLogDetail();
            if (element.getDateTimeLog().split(",").length > 1) {
                throw new HermesException();
            }

            locationLogDetail.setTimeLog(Constants.dfTimeSmartDriver.parse(element.getDateTimeLog().replaceAll(".*_", "")));
            locationLogDetail.setLatitude(element.getLatitude());
            locationLogDetail.setLongitude(element.getLongitude());
            locationLogDetail.setHeartRate(element.getHeartRate());
            locationLogDetail.setSpeed(element.getSpeed());
            locationLogDetail.setRrTime(element.getRrTime());
            locationLogDetail.setLocationLog(locationLog);

            locationLog.getLocationLogDetailList().add(locationLogDetail);

        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, "processReadElement() - Error al procesar el elemento: {0}", ex.getMessage());
        }
    }

    public LocationLog getLocationLog() {
        return locationLog;
    }
}
