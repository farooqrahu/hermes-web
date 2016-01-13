/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.location;

import es.jyago.hermes.bean.LocaleBean;
import es.jyago.hermes.csv.CSVUtil;
import es.jyago.hermes.csv.ICSVController;
import es.jyago.hermes.location.detail.LocationLogDetail;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.util.JsfUtil;
import es.jyago.hermes.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Jorge Yago
 */
@Named("locationLogController")
@SessionScoped
public class LocationLogController implements Serializable, ICSVController<IntervalData> {

    private static final Logger log = Logger.getLogger(LocationLogController.class.getName());

    private boolean showMaximumSpeedLocation;
    private boolean showMinimumHeartRateLocation;
    private boolean showMaximumHeartRateLocation;

    private Marker maximumSpeedMarker;
    private Marker minimumHeartRateMarker;
    private Marker maximumHeartRateMarker;
    private Marker marker;
    private Marker whitePushpin;

    private Date mapDate;
    private LocationLog selectedLocationLog;
    private List<LocationLog> locationLogList;
    private TreeMap<Date, List<LocationLog>> locationLogMap;

    private boolean filterByHeartRate;
    private boolean filterBySpeed;

    private MapModel mapModel;
    private LineChartModel speedLineChartModel;
    private LineChartModel heartRateLineChartModel;

    private int intervalLength;
    private List<IntervalData> intervalDataList;
    private IntervalData selectedInterval;
    private List<IntervalData> selectedIntervalDataList;
    @EJB
    private LocationLogFacade ejbFacade;
    private Person person;

    private int stressPercentThreshold;

    public LocationLogController() {
        showMaximumSpeedLocation = false;
        showMinimumHeartRateLocation = false;
        showMaximumHeartRateLocation = false;
        maximumSpeedMarker = null;
        minimumHeartRateMarker = null;
        maximumHeartRateMarker = null;
        marker = null;
        locationLogList = null;
        selectedLocationLog = null;
        locationLogList = null;
        locationLogMap = null;
        filterByHeartRate = false;
        filterBySpeed = false;
        mapModel = null;
        speedLineChartModel = null;
        heartRateLineChartModel = null;
        intervalLength = 100;
        intervalDataList = null;
        selectedInterval = null;
        selectedIntervalDataList = null;
        stressPercentThreshold = 20;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void initLocationLogMapModel(List<LocationLog> locationLogList) {
        this.locationLogList = locationLogList;

        if (locationLogList != null && !locationLogList.isEmpty()) {
            {
                // Filtramos las localizaciones no válidas.
                for (int i = locationLogList.size() - 1; i >= 0; i--) {
                    LocationLog ll = locationLogList.get(i);
                    if (ll.getLocationLogDetailList() == null || ll.getLocationLogDetailList().isEmpty()) {
                        locationLogList.remove(ll);
                    }
                }

                Collections.sort(locationLogList, (LocationLog pos1, LocationLog pos2) -> pos2.getDateLog().compareTo(pos1.getDateLog()));

                // Cogeremos la última fecha de las localizaciones registradas.
                if (locationLogList != null && !locationLogList.isEmpty()) {
                    mapDate = locationLogList.get(locationLogList.size() - 1).getDateLog();
                }

                // Preparamos un 'map' para acceder a las localizaciones por fecha, ya que el usuario selecciona la fecha en un calendario.
                prepareLocationLogMap();
                // Si no fuera posible establecer ninguna fecha, establecemos la fecha de hoy.
                setMapDate(mapDate != null ? mapDate : new Date());
            }
        }
    }

    public void initChartModel() {
        createMapModel();
        updateMapModel();
    }

    public boolean isShowMaximumSpeedLocation() {
        return showMaximumSpeedLocation;
    }

    public void setShowMaximumSpeedLocation(boolean showMaximumSpeedLocation) {
        this.showMaximumSpeedLocation = showMaximumSpeedLocation;
        if (showMaximumSpeedLocation) {
            marker = maximumSpeedMarker;
        }
    }

    public boolean isShowMinimumHeartRateLocation() {
        return showMinimumHeartRateLocation;
    }

    public void setShowMinimumHeartRateLocation(boolean showMinimumHeartRateLocation) {
        this.showMinimumHeartRateLocation = showMinimumHeartRateLocation;
        if (showMinimumHeartRateLocation) {
            marker = minimumHeartRateMarker;
        }
    }

    public boolean isShowMaximumHeartRateLocation() {
        return showMaximumHeartRateLocation;
    }

    public void setShowMaximumHeartRateLocation(boolean showMaximumHeartRateLocation) {
        this.showMaximumHeartRateLocation = showMaximumHeartRateLocation;
        if (showMaximumHeartRateLocation) {
            marker = maximumHeartRateMarker;
        }
    }

    public Marker getMaximumSpeedMarker() {
        return maximumSpeedMarker;
    }

    public void setMaximumSpeedMarker(Marker maximumSpeedMarker) {
        this.maximumSpeedMarker = maximumSpeedMarker;
    }

    public Marker getMinimumHeartRateMarker() {
        return minimumHeartRateMarker;
    }

    public void setMinimumHeartRateMarker(Marker minimumHeartRateMarker) {
        this.minimumHeartRateMarker = minimumHeartRateMarker;
    }

    public Marker getMaximumHeartRateMarker() {
        return maximumHeartRateMarker;
    }

    public void setMaximumHeartRateMarker(Marker maximumHeartRateMarker) {
        this.maximumHeartRateMarker = maximumHeartRateMarker;
    }

    public Date getMapDate() {
        return mapDate;
    }

    public void setMapDate(Date mapDate) {
        this.mapDate = mapDate;
        setSelectedLocationLog(mapDate);
    }

    public LocationLog getSelectedLocationLog() {
        return selectedLocationLog;
    }

    public void setSelectedLocationLog(LocationLog selectedLocationLog) {
        this.selectedLocationLog = selectedLocationLog;
    }

    public void setSelectedLocationLog(Date selectedDate) {

        if (locationLogMap != null) {
            List<LocationLog> lll = locationLogMap.get(selectedDate);
            if (lll != null && !lll.isEmpty()) {
                this.selectedLocationLog = lll.get(0);
                if (this.selectedLocationLog.getLocationLogDetailList() != null && this.selectedLocationLog.getLocationLogDetailList().size() > 0) {
                    initChartModel();
                }
            }
        }
    }

    public boolean isFilterByHeartRate() {
        return filterByHeartRate;
    }

    public void setFilterByHeartRate(boolean filterByHeartRate) {
        this.filterByHeartRate = filterByHeartRate;
        updateMapModel();
    }

    public boolean isFilterBySpeed() {
        return filterBySpeed;
    }

    public void setFilterBySpeed(boolean filterBySpeed) {
        this.filterBySpeed = filterBySpeed;
        updateMapModel();
    }

    public List<LocationLog> getLocationLogList() {
        return locationLogList;
    }

    public void onHeartRateSlideEnd(SlideEndEvent event) {
        if (filterByHeartRate) {
            updateMapModel();
        }
    }

    public void onSpeedSlideEnd(SlideEndEvent event) {
        if (filterBySpeed) {
            updateMapModel();
        }
    }

    public void onIntervalSlideEnd(SlideEndEvent event) {
        intervalLength = event.getValue();
        generateIntervalData();
    }

    public int getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(int intervalLength) {
        this.intervalLength = intervalLength;
    }

    public void generateIntervalData() {
        intervalDataList = new ArrayList<>();

        if (selectedLocationLog.getLocationLogDetailList() != null && !selectedLocationLog.getLocationLogDetailList().isEmpty()) {
            int intervalId = 0;
            double length = 0.0d;
            double cummulativeLength = 0.0d;
            double cummulativePositiveSpeeds = 0.0d;
            DescriptiveStatistics globalStats = new DescriptiveStatistics();
            DescriptiveStatistics speedStats = new DescriptiveStatistics();
            DescriptiveStatistics heartRateStats = new DescriptiveStatistics();
            LocationLogDetail lldPrev = selectedLocationLog.getLocationLogDetailList().get(0);

            // Los datos del primer intervalo los definimos antes de entrar al bucle.
            // Punto de unión entre tramos.
            double stitchLatitude = lldPrev.getLatitude();
            double stitchLongitude = lldPrev.getLongitude();
            double speedAtStart = lldPrev.getSpeed();
            int heartRateAtStart = lldPrev.getHeartRate();
            Date timeAtStart = lldPrev.getTimeLog();
            boolean newInterval = false;

            for (LocationLogDetail lld : selectedLocationLog.getLocationLogDetailList()) {
                if (newInterval) {
                    length = 0.0d;
                    cummulativePositiveSpeeds = 0.0d;
                    speedStats = new DescriptiveStatistics();
                    heartRateStats = new DescriptiveStatistics();
                    stitchLatitude = lld.getLatitude();
                    stitchLongitude = lld.getLongitude();
                    speedAtStart = lld.getSpeed();
                    heartRateAtStart = lld.getHeartRate();
                    timeAtStart = lld.getTimeLog();
                    newInterval = false;
                }

                // Sólo consideramos el registro si tiene datos válidos.
                if (lld.getLatitude() != 0.0d && lld.getLongitude() != 0.0d) {
                    globalStats.addValue(lld.getSpeed());
                    speedStats.addValue(lld.getSpeed());
                    heartRateStats.addValue(lld.getHeartRate());
                    if (lldPrev.getLatitude() != 0.0d && lldPrev.getLongitude() != 0.0d) {
                        length += distFrom(lldPrev.getLatitude(), lldPrev.getLongitude(), lld.getLatitude(), lld.getLongitude());
                        // Análisis del PKE (Positive Kinetic Energy)
                        cummulativePositiveSpeeds += analyzePKE(lld, lldPrev);
                    }
                    lldPrev = lld;

                    if (length > intervalLength) {
                        intervalId++;
                        cummulativeLength += length;
                        IntervalData intervalData = new IntervalData();

                        intervalData.setIntervalId(intervalId);
                        intervalData.setLength(length);
                        intervalData.setMinSpeed(speedStats.getMin());
                        intervalData.setMaxSpeed(speedStats.getMax());
                        intervalData.setAverageSpeed(speedStats.getMean());
                        intervalData.setMedianSpeed(speedStats.getPercentile(50));
                        intervalData.setStandardDeviationSpeed(speedStats.getStandardDeviation());
                        intervalData.setCummulativeLength(cummulativeLength);
                        intervalData.setPke(cummulativePositiveSpeeds / length);
                        intervalData.setStartLatitude(stitchLatitude);
                        intervalData.setStartLongitude(stitchLongitude);
                        intervalData.setEndLatitude(lld.getLatitude());
                        intervalData.setEndLongitude(lld.getLongitude());
                        intervalData.setSpeedAtStart(speedAtStart);
                        intervalData.setSpeedAtEnd(lld.getSpeed());
                        intervalData.setStartDate(timeAtStart);
                        intervalData.setEndDate(lld.getTimeLog());
                        intervalData.setMinHeartRate((int) heartRateStats.getMin());
                        intervalData.setMaxHeartRate((int) heartRateStats.getMax());
                        intervalData.setAverageHeartRate(heartRateStats.getMean());
                        intervalData.setMedianHeartRate(heartRateStats.getPercentile(50));
                        intervalData.setStandardDeviationHeartRate(heartRateStats.getStandardDeviation());
                        intervalData.setHeartRateAtStart(heartRateAtStart);
                        intervalData.setHeartRateAtEnd(lld.getHeartRate());

                        // Nivel de estrés
                        intervalData.setStress(getHeartRateAverageDeviation(selectedLocationLog, intervalData.getAverageHeartRate()));

                        intervalDataList.add(intervalData);
                        newInterval = true;
                    }
                }
            }
        }
    }

    public void generateIntervalDataFromSelected() {
        selectedIntervalDataList = new ArrayList<>();

        for (LocationLog ll : locationLogList) {
            DescriptiveStatistics speedStats = new DescriptiveStatistics();
            DescriptiveStatistics heartRateStats = new DescriptiveStatistics();
            boolean enterInterval = false;
            double length = 0.0d;
            double cummulativePositiveSpeeds = 0.0d;
            LocationLogDetail lldPrev = ll.getLocationLogDetailList().get(0);
            double currentMinDistance = distFrom(lldPrev.getLatitude(), lldPrev.getLongitude(), selectedInterval.getStartLatitude(), selectedInterval.getStartLongitude());
            Date timeAtStart = lldPrev.getTimeLog();
            double speedAtStart = lldPrev.getSpeed();
            int heartRateAtStart = lldPrev.getHeartRate();
            IntervalData previousInterval = null;

            for (int i = 0; i < ll.getLocationLogDetailList().size(); i++) {
                LocationLogDetail lld = ll.getLocationLogDetailList().get(i);

                if (!enterInterval) {
                    currentMinDistance = checkInOutInterval(lld, selectedInterval.getStartLatitude(), selectedInterval.getStartLongitude(), currentMinDistance);
                    // Si la distancia es 0, quiere decir que ya estamos en el tramo.
                    if (currentMinDistance == 0.0d) {
                        enterInterval = true;
                        // Hemos entrado en el tramo. Analizamos la distancia del tramo hacia atrás, para establecer los antecedentes.
                        previousInterval = analyzePreviousInterval(ll, i, selectedInterval.getLength());
                        timeAtStart = lld.getTimeLog();
                        speedAtStart = lld.getSpeed();
                        heartRateAtStart = lld.getHeartRate();
                        // Consideraremos ahora la distancia con el punto de salida.
                        currentMinDistance = checkInOutInterval(lld, selectedInterval.getEndLatitude(), selectedInterval.getEndLongitude(), Double.MAX_VALUE);
                    }
                }

                // Comprobamos si hemos entrado en un tramo, por el criterio anterior.
                if (enterInterval) {
                    currentMinDistance = checkInOutInterval(lld, selectedInterval.getEndLatitude(), selectedInterval.getEndLongitude(), currentMinDistance);
                    if (currentMinDistance != 0.0d && length <= selectedInterval.getLength()) {
                        length += distFrom(lldPrev.getLatitude(), lldPrev.getLongitude(), lld.getLatitude(), lld.getLongitude());
                        // Análisis del PKE (Positive Kinetic Energy)
                        speedStats.addValue(lld.getSpeed());
                        heartRateStats.addValue(lld.getHeartRate());
                        cummulativePositiveSpeeds += analyzePKE(lld, lldPrev);
                    } else {
                        IntervalData intervalData = new IntervalData();

                        intervalData.setDate(ll.getDateLog());
                        intervalData.setLength(length);
                        intervalData.setMinSpeed(speedStats.getMin());
                        intervalData.setMaxSpeed(speedStats.getMax());
                        intervalData.setAverageSpeed(speedStats.getMean());
                        intervalData.setMedianSpeed(speedStats.getPercentile(50));
                        intervalData.setStandardDeviationSpeed(speedStats.getStandardDeviation());
                        intervalData.setPke(cummulativePositiveSpeeds / length);
                        intervalData.setSpeedAtStart(speedAtStart);
                        intervalData.setSpeedAtEnd(lld.getSpeed());
                        intervalData.setStartDate(timeAtStart);
                        intervalData.setEndDate(lld.getTimeLog());
                        intervalData.setMinHeartRate((int) heartRateStats.getMin());
                        intervalData.setMaxHeartRate((int) heartRateStats.getMax());
                        intervalData.setAverageHeartRate(heartRateStats.getMean());
                        intervalData.setMedianHeartRate(heartRateStats.getPercentile(50));
                        intervalData.setStandardDeviationHeartRate(heartRateStats.getStandardDeviation());
                        intervalData.setHeartRateAtStart(heartRateAtStart);
                        intervalData.setHeartRateAtEnd(lld.getHeartRate());

                        // Nivel de estrés
                        intervalData.setStress(getHeartRateAverageDeviation(ll, intervalData.getAverageHeartRate()));

                        // Añadimos la información del intervalo previo.
                        intervalData.setPreviousIntervalData(previousInterval);

                        selectedIntervalDataList.add(intervalData);
                        // Fin del intervalo. Pasamos al siguiente LocationLog.
                        break;
                    }
                }
                lldPrev = lld;
            }
        }
    }

    public void generateAllMonthIntervalData() {
        try {
            // Creamos un directorio temporal para contener los archivos generados.
            Path tempDir = Files.createTempDirectory("Hermes_web");
            String tempDirPath = tempDir.toAbsolutePath().toString() + File.separator;
            log.log(Level.INFO, "generateAllMonthIntervalData() - Directorio temporal para almacenar los CSV: {0}", tempDirPath);

            // Procesamos todos los días del mes actual.
            LocalDate firstDayMonth = new LocalDate(mapDate).dayOfMonth().withMinimumValue();
            int days = Days.daysBetween(firstDayMonth, firstDayMonth.dayOfMonth().withMaximumValue()).getDays() + 1;
            for (int i = 0; i < days; i++) {
                LocalDate currentDay = new LocalDate(firstDayMonth).plusDays(i);
                List<LocationLog> locationLogList = locationLogMap.get(currentDay.toDate());
                if (locationLogList != null && !locationLogList.isEmpty()) {
                    CSVUtil<IntervalData> csvUtil = new CSVUtil<IntervalData>();
                    for (LocationLog ll : locationLogList) {
                        selectedLocationLog = ll;
                        generateIntervalData();
                        // Creamos un archivo temporal por cada lista de intervalos.
                        String fileName = Constants.dfSmartDriver.format(ll.getDateLog()) + "_" + Constants.dfTimeSmartDriver.format(ll.getLocationLogDetailList().get(0).getTimeLog()) + ".csv";
                        log.log(Level.INFO, "generateAllMonthIntervalData() - Generando archivo CSV: {0}", fileName);
                        File file = new File(tempDir.toUri().getPath(), fileName);
                        csvUtil.getFileData(new IntervalData(), this, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE, false, file);
                    }
                }
            }

            // Creamos el archivo ZIP.
            Path zipFile = Files.createTempFile(Constants.dfMonth.format(mapDate) + "_", ".zip");
            ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFile));
            try {
                log.log(Level.INFO, "generateAllMonthIntervalData() - Generando ZIP: {0}", zipFile.getFileName().toString());

                // Recorremos los archivos CSV del directorio temporal.
                Files.walk(Paths.get(tempDirPath)).filter(p -> p.toString().endsWith(".csv")).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        // Para almacenar los archivos en el ZIP, sin directorio.
                        String sp = filePath.toAbsolutePath().toString().replace(tempDirPath, "");
                        ZipEntry zipEntry = new ZipEntry(sp);
                        try {
                            zs.putNextEntry(zipEntry);
                            zs.write(Files.readAllBytes(filePath));
                            zs.closeEntry();
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "generateAllMonthIntervalData() - No se ha podido meter el archivo: {0}", sp);
                        }
                    }
                });
            } finally {
                zs.close();
            }

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            ServletContext servletContext = (ServletContext) externalContext.getContext();

            response.reset();
            response.setContentType(servletContext.getMimeType(zipFile.getFileName().toString()));
            response.setContentLength((int) zipFile.toFile().length());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + (zipFile.getFileName().toString()).split("_")[0] + "\"");

            ServletOutputStream out = null;

            try {
                FileInputStream input = new FileInputStream(zipFile.toFile());
                byte[] buffer = new byte[1024];
                out = response.getOutputStream();
                int i = 0;
                while ((i = input.read(buffer)) != -1) {
                    out.write(buffer);
                    out.flush();
                }
                FacesContext.getCurrentInstance().getResponseComplete();
            } catch (IOException err) {
                err.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }

            facesContext.responseComplete();

        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private IntervalData analyzePreviousInterval(LocationLog ll, int pos, double length) {
        IntervalData previousIntervalData = new IntervalData();
        double cummulativeLength = 0.0d;
        LocationLogDetail lldPrev = ll.getLocationLogDetailList().get(pos);
        DescriptiveStatistics speedStats = new DescriptiveStatistics();
        DescriptiveStatistics heartRateStats = new DescriptiveStatistics();
        double cummulativePositiveSpeeds = 0.0d;

        for (int i = pos - 1; i > 0; i--) {
            LocationLogDetail lld = ll.getLocationLogDetailList().get(i);
            // Sólo consideramos el registro si tiene datos válidos.
            if (lld.getLatitude() != 0.0d && lld.getLongitude() != 0.0d) {
                if (lldPrev.getLatitude() != 0.0d && lldPrev.getLongitude() != 0.0d) {
                    cummulativeLength += distFrom(lldPrev.getLatitude(), lldPrev.getLongitude(), lld.getLatitude(), lld.getLongitude());
                }
                if (cummulativeLength >= length) {
                    // Ya se ha recorrido la distancia del tramo hacia atrás.
                    // Creamos el intervalo y salimos del bucle.
//                previousIntervalData.setIntervalId(intervalId);
                    previousIntervalData.setLength(cummulativeLength);
                    previousIntervalData.setMinSpeed(speedStats.getMin());
                    previousIntervalData.setMaxSpeed(speedStats.getMax());
                    previousIntervalData.setAverageSpeed(speedStats.getMean());
                    previousIntervalData.setMedianSpeed(speedStats.getPercentile(50));
                    previousIntervalData.setStandardDeviationSpeed(speedStats.getStandardDeviation());
                    previousIntervalData.setCummulativeLength(cummulativeLength);
                    previousIntervalData.setPke(cummulativePositiveSpeeds / cummulativeLength);
                    previousIntervalData.setStartLatitude(lld.getLongitude());
                    previousIntervalData.setStartLongitude(lld.getLatitude());
                    previousIntervalData.setEndLatitude(ll.getLocationLogDetailList().get(pos).getLongitude());
                    previousIntervalData.setEndLongitude(ll.getLocationLogDetailList().get(pos).getLatitude());
                    previousIntervalData.setSpeedAtStart(lld.getSpeed());
                    previousIntervalData.setSpeedAtEnd(ll.getLocationLogDetailList().get(pos).getSpeed());
                    previousIntervalData.setStartDate(lld.getTimeLog());
                    previousIntervalData.setEndDate(ll.getLocationLogDetailList().get(pos).getTimeLog());
                    previousIntervalData.setMinHeartRate((int) heartRateStats.getMin());
                    previousIntervalData.setMaxHeartRate((int) heartRateStats.getMax());
                    previousIntervalData.setAverageHeartRate(heartRateStats.getMean());
                    previousIntervalData.setMedianHeartRate(heartRateStats.getPercentile(50));
                    previousIntervalData.setStandardDeviationHeartRate(heartRateStats.getStandardDeviation());
                    previousIntervalData.setHeartRateAtStart(lld.getHeartRate());
                    previousIntervalData.setHeartRateAtEnd(ll.getLocationLogDetailList().get(pos).getHeartRate());
                    break;
                } else {
                    // Análisis del PKE (Positive Kinetic Energy)
                    speedStats.addValue(lld.getSpeed());
                    heartRateStats.addValue(lld.getHeartRate());
                    cummulativePositiveSpeeds += analyzePKE(lld, lldPrev);
                }
            }

            lldPrev = lld;
        }

        return previousIntervalData;
    }

    private double checkInOutInterval(LocationLogDetail lld, double latitude, double longitude, double currentMinDistance) {
        double distanceFromIntervalStart = distFrom(lld.getLatitude(), lld.getLongitude(), latitude, longitude);

        // Definimos un margen de 50m para empezar a analizar las posiciones, es decir, si la distancia hasta el inicio del tramo es menor que este margen,
        // comenzamos a analizar los puntos.
        if (distanceFromIntervalStart <= 50.0d) {
            // Analizamos las localizaciones en búsqueda de la más cercana al inicio del tramo seleccionado.
            if (distanceFromIntervalStart < currentMinDistance) {
                return distanceFromIntervalStart;
            } else {
                // Nos alejamos, considetamos que el punto de entrada es el anterior, que era el más próximo al de inicio del tramo seleccionado.
                // Devolvemos 0 para indicar que ya hemos entrado en el tramo con el punto anterior.
                return 0.0d;
            }
        }

        return currentMinDistance;
    }

    private double analyzePKE(LocationLogDetail lld, LocationLogDetail lldPrev) {
        // Convertimos los Km/h en m/s.
        double currentSpeedMS = lld.getSpeed() / 3.6d;
        double previousSpeedMS = lldPrev.getSpeed() / 3.6d;

        double speedDifference = currentSpeedMS - previousSpeedMS;
        // Analizamos la diferencia de velocidad.
        if (speedDifference > 0.0d) {
            // Si la diferencia de velocidades es positiva, se tiene en cuenta para el sumatorio.
            return Math.pow(currentSpeedMS, 2) - Math.pow(previousSpeedMS, 2);
        }

        return 0.0d;
    }

    // Implementación de la Fórmula de Haversine.
    // https://es.wikipedia.org/wiki/Fórmula_del_Haversine
    private double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000.0d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist;
    }

    public List<IntervalData> getIntervalDataList() {
        return intervalDataList;
    }

    public List<LocationLog> getTracks() {
        if (locationLogMap != null) {
            return locationLogMap.get(mapDate);
        }

        return null;
    }

    public void trackSelectionChanged(AjaxBehaviorEvent event) {
        SelectOneMenu selectMenu = (SelectOneMenu) event.getSource();
        String filename = (String) selectMenu.getSubmittedValue();

        for (LocationLog ll : locationLogMap.get(mapDate)) {
            if (ll.getFilename().equals(filename)) {
                selectedLocationLog = ll;
                break;
            }
        }
        LocationLog p = (LocationLog) ((UIOutput) event.getSource()).getValue();

        initChartModel();
    }

    private void createMapModel() {
        mapModel = new DefaultMapModel();
        List<LocationLogDetail> locationLogDetailList = selectedLocationLog.getLocationLogDetailList();

        Collections.sort(locationLogDetailList, (LocationLogDetail pos1, LocationLogDetail pos2) -> pos1.getTimeLog().compareTo(pos2.getTimeLog()));

        if (locationLogDetailList != null && !locationLogDetailList.isEmpty()) {
            Polyline polyline = new Polyline();
            polyline.setStrokeWeight(4);
            polyline.setStrokeOpacity(0.7);
            polyline.setStrokeColor("#00FF00");
            int type = 0;
            LocationLogDetail lld = locationLogDetailList.get(0);

            if (lld.getHeartRate() >= 80 && lld.getHeartRate() < 100) {
                polyline.setStrokeColor("#FFFF00");
                type = 1;
            } else if (lld.getHeartRate() >= 100) {
                polyline.setStrokeColor("#FF0000");
                type = 2;
            }

            // Procesamos los datos de ubicación.
            for (LocationLogDetail locationLogDetail : locationLogDetailList) {
                // Sólo nos interesan los datos que tengan latitud y longitud, que serán los que se puedan representar.
                if (locationLogDetail.getLatitude() != 0.0d && locationLogDetail.getLongitude() != 0.0d) {
                    // Creamos la ruta.
                    LatLng ll = new LatLng(locationLogDetail.getLatitude(), locationLogDetail.getLongitude());
                    // TODO: ¿Definir los rangos en función de la persona? ¿Configurables?
                    if (locationLogDetail.getHeartRate() < 80) {
                        if (type != 0) {
                            polyline.getPaths().add(ll);
                            mapModel.addOverlay(polyline);
                            type = 0;
                            polyline = new Polyline();
                            polyline.setStrokeColor("#00FF00");
                            polyline.setStrokeWeight(4);
                            polyline.setStrokeOpacity(0.7);
                        }
                    } else if (locationLogDetail.getHeartRate() >= 80 && locationLogDetail.getHeartRate() < 100) {
                        if (type != 1) {
                            polyline.getPaths().add(ll);
                            mapModel.addOverlay(polyline);
                            type = 1;
                            polyline = new Polyline();
                            polyline.setStrokeColor("#FFFF00");
                            polyline.setStrokeWeight(4);
                            polyline.setStrokeOpacity(0.7);
                        }
                    } else if (locationLogDetail.getHeartRate() >= 100) {
                        if (type != 2) {
                            polyline.getPaths().add(ll);
                            mapModel.addOverlay(polyline);
                            type = 2;
                            polyline = new Polyline();
                            polyline.setStrokeColor("#FF0000");
                            polyline.setStrokeWeight(4);
                            polyline.setStrokeOpacity(0.7);
                        }
                    }
                    polyline.getPaths().add(ll);

                    // TODO: ¿Poner sólo algunas? Quizás todas sean demasiadas.
                    // Añadimos todas las marcas y después mostraremos sólo las que cumplan los requisitos definidos en el formulario.
                    mapModel.addOverlay(createMarker(locationLogDetail, false, "https://maps.google.com/mapfiles/ms/micons/red.png"));
                }
            }
            mapModel.addOverlay(polyline);
        }
        // Ponemos la marca inicial en el primer elemento.
        if (mapModel.getMarkers() != null && !mapModel.getMarkers().isEmpty()) {
            marker = mapModel.getMarkers().get(0);
        } else {
            // Si no hubiera marcas, pondremos el mapa centrado en Sevilla ;)
            marker = new Marker(new LatLng(37.400165d, -5.993729d));
        }

        // Añadimos la marca de velocidad máxima.
        maximumSpeedMarker = createMarker(selectedLocationLog.getMaximumSpeedLocation(), false, "https://maps.google.com/mapfiles/kml/pal4/icon15.png");
        mapModel.addOverlay(maximumSpeedMarker);
        // Añadimos la marca del pulso mínimo.
        minimumHeartRateMarker = createMarker(selectedLocationLog.getMinimumHeartRateLocation(), false, "https://maps.google.com/mapfiles/kml/pal2/icon28.png");
        mapModel.addOverlay(minimumHeartRateMarker);
        // Añadimos la marca de pulso máximo.
        maximumHeartRateMarker = createMarker(selectedLocationLog.getMaximumHeartRateLocation(), false, "https://maps.google.com/mapfiles/kml/pal4/icon63.png");
        mapModel.addOverlay(maximumHeartRateMarker);

        // Inicializamos el gráfico de velocidad del trayecto seleccionado.
        initSpeedLineChart();
        // Inicializamos el gráfico de ritmo cardíaco del trayecto seleccionado.
        initHeartRateLineChart();
    }

    private void updateMapModel() {
        // En primer lugar ocultamos todas las marcas. 
        for (Marker m : mapModel.getMarkers()) {
            m.setVisible(false);
        }

        // Mostramos las marcas indicadas por el usuario.
        for (Marker m : mapModel.getMarkers()) {
            boolean show = false;
            LocationLogDetail locationLogDetail = (LocationLogDetail) m.getData();
            // Pondremos una marca en aquellos puntos en los que el pulso y la velocidad estén entre los márgenes definidos por el usuario.
            if (filterByHeartRate) {
                show = checkIfInRange(locationLogDetail.getHeartRate(), selectedLocationLog.getMinimumHeartRateLocation().getHeartRate(), selectedLocationLog.getMaximumHeartRateLocation().getHeartRate());
            }
            if (show && filterBySpeed) {
                show = checkIfInRange(locationLogDetail.getSpeed(), 0.0d, selectedLocationLog.getMaximumSpeedLocation().getSpeed());
            }
            if (show) {
                m.setVisible(true);
            }
        }

        updateCheckboxMarkers();
    }

    public void updateCheckboxMarkers() {
        // Marca de velocidad máxima.
        maximumSpeedMarker.setVisible(showMaximumSpeedLocation);

        // Marca de pulso mínimo.
        minimumHeartRateMarker.setVisible(showMinimumHeartRateLocation);

        // Marca de pulso máximo.
        maximumHeartRateMarker.setVisible(showMaximumHeartRateLocation);
    }

    public MapModel getMapModel() {
        // FIXME: Poner para que no se consulte en cada recarga.
        updateCheckboxMarkers();
        return mapModel;
    }

    private boolean checkIfInRange(Object value, Object min, Object max) {
        boolean inRange = true;

        if (value instanceof Integer) {
            Integer intValue = (Integer) value;
            Integer intMin = (Integer) min;
            Integer intMax = (Integer) max;

            if (intValue < intMin || intValue > intMax) {
                inRange = false;
            }
        } else if (value instanceof Double) {
            double dvalue = (double) value;
            double dMin = (double) min;
            double dMax = (double) max;

            if ((dvalue < dMin) || (dvalue > dMax)) {

                inRange = false;
            }
        }

        return inRange;
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        try {
            marker = (Marker) event.getOverlay();
            if (marker != null) {
                // FIXME: Ver si se puede añadir salto de línea. No funciona '\n' ni '<br/>'
                String sb = marker.getTitle();
                marker.setTitle(sb);
            }
        } catch (ClassCastException ex) {
        }
    }

    public Marker getMarker() {
        return marker;
    }

    public String getMarkerLatitudeLongitude() {
        return marker.getLatlng().getLat() + "," + marker.getLatlng().getLng();
    }

    private Marker createMarker(LocationLogDetail locationLogDetail, boolean showMarker, String iconUrl) {
        LatLng ll = new LatLng(locationLogDetail.getLatitude(), locationLogDetail.getLongitude());

        StringBuilder sb = new StringBuilder();
        sb.append(ResourceBundle.getBundle("/Bundle").getString("Time")).append(": ").append(Constants.dfTime.format(locationLogDetail.getTimeLog()));
        sb.append(" ");
        sb.append(ResourceBundle.getBundle("/Bundle").getString("HeartRate")).append(": ").append(Integer.toString(locationLogDetail.getHeartRate()));
        sb.append(" ");
        sb.append(ResourceBundle.getBundle("/Bundle").getString("Speed")).append(": ").append(locationLogDetail.getSpeed());
        sb.append(" (").append(locationLogDetail.getLatitude()).append(", ").append(locationLogDetail.getLongitude()).append(")");

        Marker m = new Marker(ll, sb.toString(), locationLogDetail, iconUrl);
        m.setVisible(showMarker);

        return m;
    }

    public void initHeartRateLineChart() {
        heartRateLineChartModel = new LineChartModel();
        LineChartSeries series = new LineChartSeries();

        // Rellenamos la serie con las horas y el pulso.
        for (LocationLogDetail locationLogDetail : selectedLocationLog.getLocationLogDetailList()) {
            series.set(locationLogDetail.getTimeLog().getTime(), locationLogDetail.getHeartRate());
        }

        heartRateLineChartModel.setShowPointLabels(false);
        heartRateLineChartModel.setShowDatatip(true);
        heartRateLineChartModel.setMouseoverHighlight(true);
        heartRateLineChartModel.setSeriesColors("2DC800");
        heartRateLineChartModel.setAnimate(true);
        heartRateLineChartModel.setZoom(true);

        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Time"));
        heartRateLineChartModel.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = heartRateLineChartModel.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("HeartRate"));
        yAxis.setMin(0);

        heartRateLineChartModel.setExtender("locationLogChartExtender");
        if (!series.getData().isEmpty()) {
            heartRateLineChartModel.addSeries(series);
        }
    }

    public LineChartModel getHeartRateLineChartModel() {
        return heartRateLineChartModel;
    }

    public void initSpeedLineChart() {
        speedLineChartModel = new LineChartModel();
        LineChartSeries series = new LineChartSeries();

        double previousSpeed = 0.0d;
        // Rellenamos la serie con las horas y la velocidad.
        for (LocationLogDetail locationLogDetail : selectedLocationLog.getLocationLogDetailList()) {
            series.set(locationLogDetail.getTimeLog().getTime(), locationLogDetail.getSpeed());
        }

        speedLineChartModel.setShowPointLabels(false);
        speedLineChartModel.setShowDatatip(true);
        speedLineChartModel.setMouseoverHighlight(true);
        speedLineChartModel.setSeriesColors("4444FF");
        speedLineChartModel.setAnimate(true);
        speedLineChartModel.setZoom(true);

        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Time"));
        speedLineChartModel.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = speedLineChartModel.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("Speed"));
        yAxis.setMin(0);

        speedLineChartModel.setExtender("locationLogChartExtender");
        if (!series.getData().isEmpty()) {
            speedLineChartModel.addSeries(series);
        }
    }

    public LineChartModel getSpeedLineChartModel() {
        return speedLineChartModel;
    }

    public IntervalData getSelectedInterval() {
        return selectedInterval;
    }

    public void setSelectedInterval(IntervalData selectedInterval) {
        this.selectedInterval = selectedInterval;
    }

    public List<IntervalData> getSelectedIntervalDataList() {
        return selectedIntervalDataList;
    }

    public void setSelectedIntervalDataList(List<IntervalData> selectedIntervalDataList) {
        this.selectedIntervalDataList = selectedIntervalDataList;
    }

    public String getEnabledDays() {
        Set<Date> datesSet = this.locationLogMap.keySet();
        String[] stringDates = new String[datesSet.size()];

        int i = 0;
        for (Date d : datesSet) {
            stringDates[i] = Constants.df.format(d);
            i++;
        }

        return new org.primefaces.json.JSONArray(stringDates).toString();
    }

    private void prepareLocationLogMap() {
        this.locationLogMap = new TreeMap<>();

        if (locationLogList != null) {
            for (LocationLog l : locationLogList) {
                if (l.getDateLog() != null) {
                    List<LocationLog> lll = locationLogMap.get(l.getDateLog());
                    if (lll == null) {
                        lll = new ArrayList();
                    }
                    lll.add(l);
                    Collections.sort(lll, (LocationLog ll1, LocationLog ll2) -> ll1.getFilename().compareTo(ll2.getFilename()));
                    locationLogMap.put(l.getDateLog(), lll);
                }
            }
        }
    }

    public void handleImportLocationFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();

        // Procesamos el archivo CSV.
        if (file != null) {
            try {
                log.log(Level.INFO, "handleImportLocationFileUpload() - Archivo de localizaciones: {0}", file.getFileName());
                // En primer lugar, probamos a hacer la lectura del CSV con el 'parser' de la versión más moderna de SmartDriver.
                try {
                    LocationLogCSVController2 llc2 = new LocationLogCSVController2();
                    llc2.processFile(file);
                    LocationLog locationLog = llc2.getLocationLog();
                    if (locationLog != null && locationLog.getDateLog() != null) {
                        log.log(Level.INFO, "handleImportLocationFileUpload() - Archivo correcto: {0}", file.getFileName());
                        locationLog.setPerson(person);
                        locationLog.setFilename(file.getFileName());
                        // Comprobamos si existe previamente, para borrarlo e insertar el nuevo.
                        for (LocationLog ll : person.getLocationLogList()) {
                            if (ll.getFilename().equals(locationLog.getFilename())) {
                                getFacade().remove(ll);
                                break;
                            }
                        }
                        getFacade().edit(locationLog);
                    } else {
                        throw new HermesException();
                    }
                } catch (HermesException ex) {
                    log.log(Level.SEVERE, "handleImportLocationFileUpload() - Error al procesar el archivo de localizaciones con el formato moderno, probamos con el formato antiguo");
                    LocationLogCSVController llc = new LocationLogCSVController();
                    llc.processFile(file);
                    LocationLog locationLog = llc.getLocationLog();
                    if (locationLog != null && locationLog.getDateLog() != null) {
                        log.log(Level.INFO, "handleImportLocationFileUpload() - Archivo correcto: {0}", file.getFileName());
                        locationLog.setPerson(person);
                        locationLog.setFilename(file.getFileName());
                        // Comprobamos si existe previamente, para borrarlo e insertar el nuevo.
                        for (LocationLog ll : person.getLocationLogList()) {
                            if (ll.getFilename().equals(locationLog.getFilename())) {
                                getFacade().remove(ll);
                                break;
                            }
                        }
                        getFacade().edit(locationLog);
                    } else {
                        log.log(Level.SEVERE, "handleImportLocationFileUpload() - Localizaciones nulas");
                        JsfUtil.addErrorMessageTag("importMessages", MessageFormat.format(LocaleBean.getBundle().getString("InvalidFile"), file.getFileName()));
                    }
                }
            } catch (HermesException ex) {
                log.log(Level.SEVERE, "handleImportLocationFileUpload() - Error al procesar el archivo de localizaciones", ex);
                JsfUtil.addErrorMessage(ex, ex.getMessage());
            }
        } else {
            log.log(Level.SEVERE, "handleImportLocationFileUpload() - Archivo no válido");
            JsfUtil.addErrorMessage(MessageFormat.format(LocaleBean.getBundle().getString("InvalidFile"), ""));
        }
    }

    private LocationLogFacade getFacade() {
        return ejbFacade;
    }

    public void refreshPersonLocationLogs() {
        person.setLocationLogList(ejbFacade.getEntityManager().createNamedQuery("LocationLog.findByPerson")
                .setParameter("personId", person.getPersonId())
                .getResultList());
        initLocationLogMapModel(person.getLocationLogList());
    }

    public void speedItemSelect(ItemSelectEvent event) {
        mapModel.getMarkers().remove(whitePushpin);
        whitePushpin = createMarker(selectedLocationLog.getLocationLogDetailList().get(event.getItemIndex()), true, "https://maps.google.com/mapfiles/kml/pal5/icon14.png");
        mapModel.addOverlay(whitePushpin);
        marker = whitePushpin;
    }

    public void heartRateItemSelect(ItemSelectEvent event) {
        mapModel.getMarkers().remove(whitePushpin);
        whitePushpin = createMarker(selectedLocationLog.getLocationLogDetailList().get(event.getItemIndex()), true, "https://maps.google.com/mapfiles/kml/pal5/icon14.png");
        mapModel.addOverlay(whitePushpin);
        marker = whitePushpin;
    }

    public int getStressPercentThreshold() {
        return stressPercentThreshold;
    }

    public void setStressPercentThreshold(int stressPercentThreshold) {
        this.stressPercentThreshold = stressPercentThreshold;
    }

    private double getHeartRateAverageDeviation(LocationLog locationLog, double intervalAverageHeartRate) {
        if (locationLog.getAvgHeartRate() == 0.0d) {
            return Double.NaN;
        }

        return 100.0d - (intervalAverageHeartRate * 100.0d / locationLog.getAvgHeartRate());
    }

    public String getStressColor(double value) {
        if (value >= stressPercentThreshold) {
            return "red";
        } else if (value <= -stressPercentThreshold) {
            return "green";
        }

        return "black";
    }

    @Override
    public void processReadElement(IntervalData element) throws HermesException {
        // No se usará porque sólo vamos a exportar los datos de los intervalos, no a importarlos.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IntervalData> getItems() {
        // Devolvemos la lista de intervalos para poder generar el CSV de intervalos.
        return this.intervalDataList;
    }
}
