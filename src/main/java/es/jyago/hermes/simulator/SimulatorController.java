package es.jyago.hermes.simulator;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import es.jyago.hermes.bean.LocaleBean;
import es.jyago.hermes.location.detail.LocationLogDetail;
import es.jyago.hermes.google.directions.GeocodedWaypoints;
import es.jyago.hermes.google.directions.Leg;
import es.jyago.hermes.google.directions.Location;
import es.jyago.hermes.google.directions.PolylineDecoder;
import es.jyago.hermes.google.directions.Route;
import es.jyago.hermes.location.LocationLog;
import es.jyago.hermes.location.LocationLogFacade;
import es.jyago.hermes.openStreetMap.GeomWay;
import es.jyago.hermes.openStreetMap.GeomWaySteps;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import es.jyago.hermes.smartDriver.DataSection;
import es.jyago.hermes.smartDriver.DataSectionHermesZtreamyFacade;
import es.jyago.hermes.smartDriver.LocationHermesZtreamyFacade;
import es.jyago.hermes.smartDriver.RoadSection;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import es.jyago.hermes.util.Util;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.LocalTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;

@Named("simulatorController")
@SessionScoped
public class SimulatorController implements Serializable {

    private static final Logger LOG = Logger.getLogger(SimulatorController.class.getName());

    private static final Location SEVILLE = new Location(37.3898358, -5.986069);
    private static final String MARKER_ICON_PATH = "http://maps.google.com/mapfiles/kml/pal4/icon15.png";
    private static final String MARKER_FINISH_ICON_PATH = "http://maps.google.com/mapfiles/kml/pal5/icon13.png";
    // FIXME: ¿Dividir simulaciones entre los hilos o que cada hilo cree la misma cantidad de trayectos?
    private static final int NUM_THREADS = 100;
    private volatile int ztreamyErrors;
    private volatile boolean allSimulationsFinished;
    private volatile String simulationFinishedMessage;
    // Parámetros recogidos de SmartDriver.
    private static final String APPLICATION_ID = "SmartDriver";
    private static final int ZTREAMY_SEND_INTERVAL_MILLISECONDS = 10000;
    private static final int ZTREAMY_SEND_INTERVAL_METERS = 500;
    private static final double HIGH_ACCELERATION_THRESHOLD = 2.5d;
    private static final double HIGH_DECELERATION_THRESHOLD = -3.5d;

    public static enum Track_Simulation_Method {
        GOOGLE, OPENSTREETMAP
    };

    private Marker marker;

    private int distance;
    private int distanceFromSevilleCenter;
    private int tracksAmount;
    private boolean createSimulatedUser;
    private List<TrackInfo> trackInfoList;
    private MapModel simulatedMapModel;
    private Track_Simulation_Method simulationMethod;
    private ArrayList<LocationLogWrapper> locationLogList;

    private Timer simulationTimers[];
    private long elapsedTime;
    private int simulatedSmartDrivers;
    private String url;

    @Inject
    private PersonFacade personFacade;

    @Inject
    private LocationLogFacade locationLogFacade;

    public SimulatorController() {
    }

    @PostConstruct
    public void init() {
        distanceFromSevilleCenter = 1;
        distance = 10;
        tracksAmount = 1;
        simulatedSmartDrivers = 1;
        createSimulatedUser = false;
        simulationMethod = Track_Simulation_Method.GOOGLE;
        marker = new Marker(new LatLng(SEVILLE.getLat(), SEVILLE.getLng()));
        ztreamyErrors = 0;
        allSimulationsFinished = false;

        // Los parámetros de configuración de Ztreamy estarán en la tabla de configuración.
        url = Constants.getInstance().getConfigurationValueByKey("ZtreamyUrl");
        generateSimulatedTracks();
    }

    public String getMarkerLatitudeLongitude() {
        if (marker != null) {
            return marker.getLatlng().getLat() + "," + marker.getLatlng().getLng();
        }

        return "";
    }

    public Marker getMarker() {
        return marker;
    }

    public void generateSimulatedTracks() {
        simulatedMapModel = new DefaultMapModel();
        trackInfoList = new ArrayList();
        locationLogList = new ArrayList<>();

        for (int i = 0; i < tracksAmount; i++) {
            LocationLogWrapper llw = new LocationLogWrapper();

            Location origin = getRandomLocation(SEVILLE.getLat(), SEVILLE.getLng(), distanceFromSevilleCenter);
            Location destination = getRandomLocation(origin.getLat(), origin.getLng(), distance);
            TrackInfo trackInfo = null;
            Date currentTime = new Date();

            // Creamos un objeto de localizaciones de 'SmartDriver'.
            LocationLog ll = new LocationLog();
            ll.setDateLog(currentTime);

            if (simulationMethod.equals(Track_Simulation_Method.GOOGLE)) {
                Future future = GoogleWebService.submitTask(new Callable() {
                    @Override
                    public String call() {
                        String json = null;
                        try {
                            json = IOUtils.toString(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.getLat() + "," + origin.getLng() + "&destination=" + destination.getLat() + "," + destination.getLng()), "UTF-8");
                        } catch (IOException ex) {
                            LOG.log(Level.SEVERE, "Error al obtener el JSON de la ruta", ex);
                        }

                        return json;
                    }
                });

                try {
                    String json = (String) future.get();
                    Gson gson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create();
                    GeocodedWaypoints gcwp = gson.fromJson(json, GeocodedWaypoints.class);
                    trackInfo = createTrack(gcwp, ll, llw.getRandomFactor());
                } catch (InterruptedException | ExecutionException | JsonSyntaxException ex) {
                    LOG.log(Level.SEVERE, "Error al decodificar el JSON de la ruta", ex);
                }
            } else if (simulationMethod.equals(Track_Simulation_Method.OPENSTREETMAP)) {
                try {
                    String json = IOUtils.toString(new URL("http://cronos.lbd.org.es/hermes/api/smartdriver/network/route?fromLat=" + origin.getLat() + "&fromLng=" + origin.getLng() + "&toLat=" + destination.getLat() + "&toLng=" + destination.getLng()), "UTF-8");
                    Type listType = new TypeToken<ArrayList<GeomWaySteps>>() {
                    }.getType();
                    List<GeomWaySteps> gws = new Gson().fromJson(json, listType);
                    trackInfo = createTrack(gws, ll, llw.getRandomFactor());
                } catch (IOException | JsonSyntaxException ex) {
                    LOG.log(Level.SEVERE, "Error al decodificar el JSON de la ruta", ex);
                }
            }

            // Si alguna petición fallase, saltamos esta iteración del bucle.
            if (trackInfo == null || trackInfo.getTotalLocations() == 0) {
                continue;
            }

            // Creamos un usuario simulado, al que le asignaremos el trayecto.
            Person person = createSimPerson(currentTime.getTime());
            ll.setPerson(person);
            ll.setFilename(person.getFullName());

            if (createSimulatedUser) {
                // Pero sólo lo grabaremos en la B.D. si lo indica el usuario.
                personFacade.create(person);
                locationLogFacade.create(ll);
            }

            llw.setLocationLog(ll);
            llw.setBaseTime(ll.getLocationLogDetailList().get(0).getTimeLog().getTime());
            llw.setDetailPosition(0);

            locationLogList.add(llw);
            trackInfoList.add(trackInfo);
        }
    }

    private Person createSimPerson(long currentTime) {
        Person person = new Person();
        String name = "Sim_" + currentTime;
        person.setFullName(name);
        person.setUsername(name);
        person.setPassword("hermes");
        person.setEmail(name + "@sim.com");

        return person;
    }

    public MapModel getSimulatedMapModel() {
        return simulatedMapModel;
    }

    private TrackInfo createTrack(List<GeomWaySteps> geomWayStepsList, LocationLog ll, Double randomFactor) {
        TrackInfo trackInfo = new TrackInfo();

        if (geomWayStepsList != null && !geomWayStepsList.isEmpty()) {
            Polyline polyline = new Polyline();
            polyline.setStrokeWeight(4);
            polyline.setStrokeOpacity(0.7);

            Random rand = new Random();

            // Hacemos que las rutas sean variaciones de azul.
            polyline.setStrokeColor("#0000" + String.format("%02x", rand.nextInt(0x100)));

            // Resumen que mostraremos por pantalla.
            SectionInfo summary = trackInfo.getSummary();
//                    summary.setDistance(.getDistance().getValue());
//                    summary.setDuration(l.getDuration().getValue());
//                    summary.setStartLocation(l.getStartLocation());
//                    summary.setStartAddress(l.getStartAddress());
//                    summary.setEndLocation(l.getEndLocation());
//                    summary.setEndAddress(l.getEndAddress());

            // Listado de posiciones que componen el trayecto de SmartDriver.
            ArrayList<LocationLogDetail> locationLogDetailList = new ArrayList<>();

            double speed;

            // Analizamos la información obtenida de la consulta a OpenStreetMap.
            for (GeomWaySteps gws : geomWayStepsList) {
                GeomWay gw = gws.getGeomWay();

                for (int i = 0; i < gw.getCoordinates().size(); i++) {
                    // Viene en formato: longitud, latitud.
                    List<Double> coordinates = gw.getCoordinates().get(i);

                    // Añadimos un nuevo punto en la polilínea que se dibujará por pantalla.
                    LatLng latlng = new LatLng(coordinates.get(1), coordinates.get(0));
                    polyline.getPaths().add(latlng);

                    // Creamos una marca con la información detallada, para poder mostrarla cuando se pulse en la posición en Google Maps.
                    StringBuilder sb = new StringBuilder();
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("Time")).append(": ").append(Constants.dfTime.format(location.getTimeLog()));
//        sb.append(" ");
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("HeartRate")).append(": ").append(Integer.toString(location.getHeartRate()));
//        sb.append(" ");
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("Speed")).append(": ").append(location.getSpeed());
//        sb.append(" (").append(location.getLatitude()).append(", ").append(location.getLongitude()).append(")");

// FIXME: ¿Poner de nuevo?
//                    Marker m = new Marker(latlng, sb.toString(), "https://maps.google.com/mapfiles/ms/micons/blue.png");
//                    m.setVisible(false);
//
//                    simulatedMapModel.addOverlay(m);
                }

                simulatedMapModel.addOverlay(polyline);

//                // Asignamos un 'marker' con la posición inicial de cada trayecto.
//                LatLng latLng = new LatLng(currentLocationLogDetail.getLatitude(), currentLocationLogDetail.getLongitude());
//                Marker m = new Marker(latLng, "", "https://maps.google.com/mapfiles/ms/micons/cabs.png");
//                m.setVisible(true);
//                simulatedMapModel.addOverlay(m);
            }
            trackInfo.setTotalLocations(locationLogDetailList.size());
            trackInfo.setAverageLocationsDistance(summary.getDistance() / locationLogDetailList.size());

            // Asignamos el listado de posiciones.
            ll.setLocationLogDetailList(locationLogDetailList);
        }

        return trackInfo;
    }

    private TrackInfo createTrack(GeocodedWaypoints gcwp, LocationLog ll, Double randomFactor) {
        TrackInfo trackInfo = new TrackInfo();

        if (gcwp.getRoutes() != null) {
            Polyline polyline = new Polyline();
            polyline.setStrokeWeight(4);
            polyline.setStrokeOpacity(0.7);

            Random rand = new Random();
            // Hacemos que las rutas sean variaciones de verde.
            polyline.setStrokeColor("#00" + String.format("%02x", rand.nextInt(0x100)) + "00");

            // Listado de posiciones que componen el trayecto de SmartDriver.
            ArrayList<LocationLogDetail> locationLogDetailList = new ArrayList<>();

            // Analizamos la información obtenida de la consulta a Google Directions.
            // Nuestra petición sólo devolverá una ruta.
            if (gcwp.getRoutes() != null && !gcwp.getRoutes().isEmpty()) {
                Route r = gcwp.getRoutes().get(0);
                // Comprobamos que traiga información de la ruta.
                if (r.getLegs() != null) {
                    Leg l = r.getLegs().get(0);

                    // Resumen que mostraremos por pantalla.
                    SectionInfo summary = trackInfo.getSummary();
                    summary.setDistance(l.getDistance().getValue());
                    summary.setDuration((int) (l.getDuration().getValue() * randomFactor)); // Para darle un poco de aletoriedad.
                    summary.setStartLocation(new LocationInfo(l.getStartLocation().getLat(), l.getStartLocation().getLng()));
                    summary.setStartAddress(l.getStartAddress());
                    summary.setEndLocation(new LocationInfo(l.getEndLocation().getLat(), l.getEndLocation().getLng()));
                    summary.setEndAddress(l.getEndAddress());

                    double speed;
                    LocalTime localTime = new LocalTime();
                    ArrayList<Location> locationList = PolylineDecoder.decodePoly(r.getOverviewPolyline().getPoints());
                    Location previous = locationList.get(0);

                    // FIXME: ¿Interpolación de velocidades? Otra opción es consultar a Google Distance Matrix para consultar el tiempo que se tarda entre 2 puntos (le afecta el tráfico) y sacar la velocidad.
//                PolynomialFunction p = new PolynomialFunction(new double[]{speed, averagePolylineSpeed,});
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = locationList.get(i);

                        // Añadimos un nuevo punto en la polilínea que se dibujará por pantalla.
                        LatLng latlng = new LatLng(location.getLat(), location.getLng());
                        polyline.getPaths().add(latlng);

                        // Creamos un nodo del trayecto, como si usásemos SmartDriver.
                        LocationLogDetail lld = new LocationLogDetail();
                        lld.setLocationLog(ll);
                        lld.setLatitude(location.getLat());
                        lld.setLongitude(location.getLng());

                        // Calculamos la distancia en metros entre los puntos previo y actual, así como el tiempo necesario para recorrer dicha distancia.
                        Double pointDistance = Util.distanceHaversine(previous.getLat(), previous.getLng(), location.getLat(), location.getLng());
                        // Calculamos el tiempo en segundos que tarda en recorrer la distancia entre los puntos.
                        Double pointDuration = summary.getDuration() * pointDistance / summary.getDistance();

                        // Convertimos la velocidad a Km/h.
                        speed = pointDuration > 0 ? pointDistance * 3.6 / pointDuration : 0.0d;
                        lld.setSpeed(speed);
                        // Añadimos los segundos correspondientes a la distancia recorrida entre puntos.
                        localTime = localTime.plusSeconds(pointDuration.intValue());
                        lld.setTimeLog(localTime.toDateTimeToday().toDate());

                        locationLogDetailList.add(lld);

                        // Asignamos el actual al anterior, para poder seguir calculando las distancias y tiempos respecto al punto previo.
                        previous = location;
                    }

                    simulatedMapModel.addOverlay(polyline);

                    // Asignamos un 'marker' con la posición inicial de cada trayecto y cada hilo
                    LocationLogDetail startPosition = locationLogDetailList.get(0);
                    LatLng latLng = new LatLng(startPosition.getLatitude(), startPosition.getLongitude());
                    // Creamos una marca con la información detallada, para poder mostrarla cuando se pulse en la posición en Google Maps.
                    StringBuilder sb = new StringBuilder();
                    sb.append(ResourceBundle.getBundle("/Bundle").getString("Time")).append(": ").append(Constants.dfTime.format(startPosition.getTimeLog()));
                    sb.append(" ");
//                        sb.append(ResourceBundle.getBundle("/Bundle").getString("HeartRate")).append(": ").append(Integer.toString(location.getHeartRate()));
//                        sb.append(" ");
                    sb.append(ResourceBundle.getBundle("/Bundle").getString("Speed")).append(": ").append(startPosition.getSpeed());
                    sb.append(" (").append(startPosition.getLatitude()).append(", ").append(startPosition.getLongitude()).append(")");
                    for (int i = 0; i < NUM_THREADS; i++)
                    {
                        Marker m = new Marker(latLng, sb.toString(), i, MARKER_ICON_PATH);
                        m.setVisible(true);
                        simulatedMapModel.addOverlay(m);
                    }

                    trackInfo.setTotalLocations(locationLogDetailList.size());
                    trackInfo.setAverageLocationsDistance(summary.getDistance() / locationLogDetailList.size());

                    // Asignamos el listado de posiciones.
                    ll.setLocationLogDetailList(locationLogDetailList);
                }
            }
        }

        return trackInfo;
    }

    private Location getRandomLocation(double latitude, double longitude, int radius) {
        Random random = new Random();

        // TODO: Comprobar que es una localización que no sea 'unnamed'
        // El radio se considerará en kilómetros. Lo convertimos a grados.
        double radiusInDegrees = radius / 111f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        double new_x = x / Math.cos(latitude);

        double foundLongitude = new_x + longitude;
        double foundLatitude = y + latitude;

        LOG.log(Level.FINE, "getRandomLocation() - Longitud: {0}, Latitud: {1}", new Object[]{foundLongitude, foundLatitude});

        Location result = new Location();
        result.setLat(foundLatitude);
        result.setLng(foundLongitude);

        return result;
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

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistanceFromSevilleCenter() {
        return distanceFromSevilleCenter;
    }

    public void setDistanceFromSevilleCenter(int distanceFromSevilleCenter) {
        this.distanceFromSevilleCenter = distanceFromSevilleCenter;
    }

    public int getTracksAmount() {
        return tracksAmount;
    }

    public void setTracksAmount(int tracksAmount) {
        this.tracksAmount = tracksAmount;
    }

    public List<TrackInfo> getTrackInfoList() {
        return trackInfoList;
    }

    public boolean isCreateSimulatedUser() {
        return createSimulatedUser;
    }

    public void setCreateSimulatedUser(boolean createSimulatedUser) {
        this.createSimulatedUser = createSimulatedUser;
    }

    public int getSimulationMethod() {
        return simulationMethod.ordinal();
    }

    public void setSimulationMethod(int value) {
        switch (value) {
            case 0:
                simulationMethod = Track_Simulation_Method.GOOGLE;
                break;
            case 1:
                simulationMethod = Track_Simulation_Method.OPENSTREETMAP;
                break;
            default:
                simulationMethod = Track_Simulation_Method.GOOGLE;
        }
    }

    public int getSimulatedSmartDrivers() {
        return simulatedSmartDrivers;
    }

    public void setSimulatedSmartDrivers(int simulatedSmartDrivers) {
        this.simulatedSmartDrivers = simulatedSmartDrivers;
    }

    public boolean isSimulating() {
        return simulationTimers != null;
    }

    public void getCurrentLatLng() {
        try {
            for (int i = 0; i < simulatedMapModel.getMarkers().size(); i++) {
                Marker m = simulatedMapModel.getMarkers().get(i);
                LatLng latLng = m.getLatlng();
                if (latLng != null) {
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.addCallbackParam("latitude" + i + "-" + m.getId(), latLng.getLat());
                    context.addCallbackParam("longitude" + i + "-" +  m.getId(), latLng.getLng());
                }
                String icon = m.getIcon();
                RequestContext context = RequestContext.getCurrentInstance();
                context.addCallbackParam("icon" + i + "-" +  m.getId(), icon);
            }
        } catch (Exception ex) {
        }
    }

    public void realTimeSimulate() {
        // Si el temporizador está instanciado, es que hay una simulación en marcha y se quiere parar.
        if (simulationTimers != null && simulationTimers.length > 0) {
            simulationFinishedMessage = MessageFormat.format(LocaleBean.getBundle().getString("ZtreamyErrors"), ztreamyErrors);
            if (ztreamyErrors > 0) {
                LOG.log(Level.SEVERE, "realTimeSimulate() - Ha habido {0} errores en el envío de datos a Ztreamy", ztreamyErrors);
            } else {
                LOG.log(Level.INFO, "realTimeSimulate() - Los envíos a Ztreamy se han realizado correctamente");
            }

            for (Timer timer : simulationTimers) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }

            simulationTimers = null;
        } else {
            // No hay simulación en marcha, la comenzamos.
            elapsedTime = 0l;
            // Reiniciamos los valores que pudieran tener los trayectos.
            locationLogList.stream().forEach((llw) -> {
                llw.reset();
            });

            // Reiniciamos los 'marker'.
            for (Marker m : simulatedMapModel.getMarkers()) {
                m.setIcon(MARKER_ICON_PATH);
            }

            // FIXME: Varios hilos para más salida?
            simulationTimers = new Timer[NUM_THREADS];

            for (int i = 0; i < NUM_THREADS; i++) {
                simulationTimers[i] = new Timer();
                simulationTimers[i].scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {

                        allSimulationsFinished = true;

                        for (int pos = 0; pos < locationLogList.size(); pos++) {
                            LocationLogWrapper llw = locationLogList.get(pos);
                            LocationLog currentLocationLog = llw.getLocationLog();
                            LocationLogDetail currentLocationLogDetail = null;
                            if (!llw.isFinished()) {
                                allSimulationsFinished = false;
                                for (int i = llw.getDetailPosition(); i < currentLocationLog.getLocationLogDetailList().size(); i++) {
                                    currentLocationLogDetail = currentLocationLog.getLocationLogDetailList().get(i);
                                    // Con el tiempo transcurrido, obtenemos la posición por la que estará.
                                    if (((currentLocationLogDetail.getTimeLog().getTime() - llw.getBaseTime()) >= elapsedTime)
                                            && (i > llw.getDetailPosition())) {
                                        // Avanzamos de posición.
                                        int previousPosition = llw.getDetailPosition();
                                        llw.setDetailPosition(i);
                                        Marker trackMarker = simulatedMapModel.getMarkers().get(pos);
                                        trackMarker.setLatlng(new LatLng(currentLocationLogDetail.getLatitude(), currentLocationLogDetail.getLongitude()));
                                        LocationLogDetail previousLocationLogDetail = currentLocationLog.getLocationLogDetailList().get(previousPosition);
                                        // Acumulamos la distancia recorrida.
                                        llw.addSectionDistance(Util.distanceHaversine(previousLocationLogDetail.getLatitude(), previousLocationLogDetail.getLongitude(), currentLocationLogDetail.getLatitude(), currentLocationLogDetail.getLongitude()));
                                        // Análisis del PKE (Positive Kinetic Energy)
                                        llw.addCummulativePositiveSpeeds(analyzePKE(currentLocationLogDetail, previousLocationLogDetail));

                                        // Comprobamos si hemos llegado al destino.
                                        if (llw.getDetailPosition() == currentLocationLog.getLocationLogDetailList().size() - 1) {
                                            llw.setFinished(true);
                                            trackMarker.setIcon(MARKER_FINISH_ICON_PATH);
                                        }
                                        break;
                                    }
                                }
                            }

                            if (currentLocationLogDetail != null) {
                                // Creamos un elementos de tipo 'RoadSection', para añadirlo al 'DataSection' que se envía a 'Ztreamy' cada 500 metros.
                                RoadSection rd = new RoadSection();
                                rd.setTime(currentLocationLogDetail.getTimeLog().getTime());
                                rd.setLatitude(currentLocationLogDetail.getLatitude());
                                rd.setLongitude(currentLocationLogDetail.getLongitude());
                                rd.setSpeed(currentLocationLogDetail.getSpeed());

                                llw.getRoadSectionList().add(rd);
                            }

                            // Se envían los datos a Ztreamy cada 10 segundos.
                            if (!llw.isFinished()) {
                                if (elapsedTime % ZTREAMY_SEND_INTERVAL_MILLISECONDS == 0) {
                                    sendLocationToZtreamy(currentLocationLogDetail);
                                }

                                // Se enviará un resumen cada 500 metros.
                                if (llw.getSectionDistance() > ZTREAMY_SEND_INTERVAL_METERS) {
                                    sendDataSectionToZtreamy(llw);
                                    llw.getRoadSectionList().clear();
                                    llw.resetCummulativePositiveSpeeds();
                                    llw.resetSectionDistance();
                                }
                            }
                        }

                        if (allSimulationsFinished) {
                            realTimeSimulate();
                        }

                        elapsedTime += 1000;
                    }
                }, 0, 1000);
            }
        }
    }

    private void sendLocationToZtreamy(LocationLogDetail lld) {
        try {
            // Creamos un objeto de tipo 'Location' de los que 'SmartDriver' envía a 'Ztreamy'.
            es.jyago.hermes.smartDriver.Location smartDriverLocation = new es.jyago.hermes.smartDriver.Location();
            smartDriverLocation.setLatitude(lld.getLatitude());
            smartDriverLocation.setLongitude(lld.getLongitude());
            smartDriverLocation.setSpeed(lld.getSpeed());
            smartDriverLocation.setAccuracy(0);
            smartDriverLocation.setScore(0);
            smartDriverLocation.setTimeStamp(Constants.dfISO8601.format(lld.getTimeLog()));

            // Inicialmente, vamos a hacer un envío como si fuera un autobus con X usuarios de SmartDriver, que iniciasen la aplicación al mismo tiempo.
            for (int i = 0; i < simulatedSmartDrivers; i++) {
                LocationHermesZtreamyFacade locationZtreamy = new LocationHermesZtreamyFacade(APPLICATION_ID, smartDriverLocation, lld.getLocationLog().getPerson().getSha() + i, url);

                if (locationZtreamy.send()) {
                    LOG.log(Level.FINER, "sendLocationToZtreamy() - Localización de trayecto simulado enviada correctamante. SmartDriver: {0}", i);
                } else {
                    LOG.log(Level.SEVERE, "sendLocationToZtreamy() - Error al enviar la localización del trayacto simulado. SmartDriver: {0}", i);
                }
            }
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "sendLocationToZtreamy() - Error en la URL", ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "sendLocationToZtreamy() - Error de I/O: {0} - Trama: {1}", new Object[]{ex.getMessage(), Constants.dfISO8601.format(lld.getTimeLog())});
            ztreamyErrors++;
        } catch (HermesException ex) {
            LOG.log(Level.SEVERE, "sendLocationToZtreamy() - Error al enviar datos a Ztreamy");
        }
    }

    private void sendDataSectionToZtreamy(LocationLogWrapper llw) {
        // Creamos un objeto de tipo 'DataSection' de los que 'SmartDriver' envía a 'Ztreamy'.
        DataSection dataSection = new DataSection();
        try {
            dataSection.setRoadSection(llw.getRoadSectionList());

            DescriptiveStatistics speedStats = new DescriptiveStatistics();
            DescriptiveStatistics heartRateStats = new DescriptiveStatistics();
            DescriptiveStatistics rrStats = new DescriptiveStatistics();
            DescriptiveStatistics accelerationStats = new DescriptiveStatistics();
            DescriptiveStatistics decelerationStats = new DescriptiveStatistics();
            RoadSection rdPrevious = llw.getRoadSectionList().get(0);
            speedStats.addValue(rdPrevious.getSpeed());
            int numHighAccelerations = 0;
            int numHighDecelerations = 0;

            for (int i = 1; i < llw.getRoadSectionList().size(); i++) {
                RoadSection rd = llw.getRoadSectionList().get(i);
                speedStats.addValue(rd.getSpeed());

                double vDiff = rd.getSpeed() - rdPrevious.getSpeed();
                double tDiff = (rd.getTime() - rdPrevious.getTime()) / 3600000.0;
                double acceleration = tDiff > 0.0d ? vDiff / tDiff : 0.0d;

                if (acceleration > 0.0d) {
                    accelerationStats.addValue(acceleration);
                    if (acceleration > HIGH_ACCELERATION_THRESHOLD) {
                        numHighAccelerations++;
                    }
                } else if (acceleration < 0.0d) {
                    decelerationStats.addValue(acceleration);
                    if (numHighDecelerations < HIGH_DECELERATION_THRESHOLD) {
                        numHighDecelerations++;
                    }
                }

                // FIXME: Simular datos de ritmo cardíaco.
                heartRateStats.addValue(80);
                rrStats.addValue(580);

                rdPrevious = rd;
            }

            dataSection.setAverageAcceleration(accelerationStats.getN() > 0 ? accelerationStats.getMean() : 0.0d);
            dataSection.setAverageDeceleration(decelerationStats.getN() > 0 ? decelerationStats.getMean() : 0.0d);
            dataSection.setAverageHeartRate(heartRateStats.getN() > 0 ? heartRateStats.getMean() : 0.0d);
            dataSection.setAverageRR(rrStats.getN() > 0 ? rrStats.getMean() : 0.0d);
            dataSection.setAverageSpeed(speedStats.getN() > 0 ? speedStats.getMean() : 0.0d);
            dataSection.setNumHighAccelerations(numHighAccelerations);
            dataSection.setNumHighDecelerations(numHighDecelerations);
            dataSection.setMaxSpeed(speedStats.getN() > 0 ? speedStats.getMax() : 0.0d);
            dataSection.setMedianSpeed(speedStats.getN() > 0 ? speedStats.getPercentile(50) : 0.0d);
            dataSection.setMinSpeed(speedStats.getN() > 0 ? speedStats.getMin() : 0.0d);
            dataSection.setPke(llw.getSectionDistance() > 0.0d ? (llw.getCummulativePositiveSpeeds() / llw.getSectionDistance()) : 0.0d);
            List<Integer> rrSectionList = new ArrayList();
            for (double rr : rrStats.getValues()) {
                rrSectionList.add((int) rr);
            }
            dataSection.setRrSection(rrSectionList);
            dataSection.setStandardDeviationHeartRate(heartRateStats.getN() > 0 ? heartRateStats.getStandardDeviation() : 0.0d);
            dataSection.setStandardDeviationRR(rrStats.getN() > 0 ? rrStats.getStandardDeviation() : 0.0d);
            dataSection.setStandardDeviationSpeed(speedStats.getN() > 0 ? speedStats.getStandardDeviation() : 0.0d);

            // Asignamos la lista de datos del tramo.
            dataSection.setRoadSection(llw.getRoadSectionList());

            DataSectionHermesZtreamyFacade dataSectionHermesZtreamy = new DataSectionHermesZtreamyFacade(APPLICATION_ID, dataSection, llw.getLocationLog().getPerson().getSha(), url);

            // Inicialmente, vamos a hacer un envío como si fuera un autobus con X usuarios de SmartDriver, que iniciasen la aplicación al mismo tiempo.
            for (int i = 0; i < simulatedSmartDrivers; i++) {
                if (dataSectionHermesZtreamy.send()) {
                    LOG.log(Level.FINER, "sendDataSectionToZtreamy() - Localización de trayecto simulado enviada correctamante. SmartDriver: {0}", i);
                } else {
                    LOG.log(Level.SEVERE, "sendDataSectionToZtreamy() - Error al enviar la localización del trayacto simulado. SmartDriver: {0}", i);
                }
            }
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "sendDataSectionToZtreamy() - Error en la URL", ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "sendLocationToZtreamy() - Error de I/O: {0} - Primera trama de la sección: {1}", new Object[]{ex.getMessage(), dataSection.getRoadSection().get(0).getTimeStamp()});
            ztreamyErrors++;
        } catch (HermesException ex) {
            LOG.log(Level.SEVERE, "sendDataSectionToZtreamy() - Error al enviar datos a Ztreamy");
        }
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

    public boolean isAllSimulationsFinished() {
        return allSimulationsFinished;
    }

    public String getSimulationFinishedMessage() {
        return simulationFinishedMessage;
    }

    class LocationLogWrapper {

        private int detailPosition;
        private long baseTime;
        private boolean finished;
        private LocationLog locationLog;
        private double sectionDistance;
        private List<RoadSection> roadSectionList;
        private double cummulativePositiveSpeeds;
        private final double randomFactor;

        public LocationLogWrapper() {
            this.detailPosition = 0;
            this.baseTime = 0l;
            this.finished = false;
            this.locationLog = null;
            this.sectionDistance = 0.0d;
            this.roadSectionList = new ArrayList();
            this.cummulativePositiveSpeeds = 0.0d;
            this.randomFactor = 1.0d - new Random().nextDouble() * 0.1d; // Para evitar un posible valor 0.0
        }

        public int getDetailPosition() {
            return detailPosition;
        }

        public void setDetailPosition(int detailPosition) {
            this.detailPosition = detailPosition;
        }

        public long getBaseTime() {
            return baseTime;
        }

        public void setBaseTime(long baseTime) {
            this.baseTime = baseTime;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public LocationLog getLocationLog() {
            return locationLog;
        }

        public void setLocationLog(LocationLog locationLog) {
            this.locationLog = locationLog;
        }

        public double getSectionDistance() {
            return sectionDistance;
        }

        public void resetSectionDistance() {
            this.sectionDistance = 0.0d;
        }

        public void addSectionDistance(double sectionDistance) {
            this.sectionDistance += sectionDistance;
        }

        public List<RoadSection> getRoadSectionList() {
            return roadSectionList;
        }

        public void setRoadSectionList(List<RoadSection> roadSectionList) {
            this.roadSectionList = roadSectionList;
        }

        public double getCummulativePositiveSpeeds() {
            return cummulativePositiveSpeeds;
        }

        public void addCummulativePositiveSpeeds(double cummulativePositiveSpeeds) {
            this.cummulativePositiveSpeeds += cummulativePositiveSpeeds;
        }

        public void resetCummulativePositiveSpeeds() {
            this.cummulativePositiveSpeeds = 0.0d;
        }

        public double getRandomFactor() {
            return randomFactor;
        }

        public void reset() {
            this.detailPosition = 0;
            this.finished = false;
            this.sectionDistance = 0.0d;
            this.roadSectionList = new ArrayList();
            this.cummulativePositiveSpeeds = 0.0d;
        }
    }
}
