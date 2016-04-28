package es.jyago.hermes.location;

import com.google.gson.Gson;
import es.jyago.hermes.location.detail.LocationLogDetail;
import es.jyago.hermes.google.directions.GeocodedWaypoints;
import es.jyago.hermes.google.directions.Leg;
import es.jyago.hermes.google.directions.Location;
import es.jyago.hermes.google.directions.PolylineDecoder;
import es.jyago.hermes.google.directions.Route;
import es.jyago.hermes.google.directions.SimpleStep;
import es.jyago.hermes.google.directions.Step;
import es.jyago.hermes.google.directions.TrackInfo;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import es.jyago.hermes.util.Util;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalTime;
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

    private Marker marker;

    private int distance;
    private int distanceFromSevilleCenter;
    private int tracksAmount;
    private boolean createSimulatedUser;
    private List<TrackInfo> trackInfoList;
    private MapModel simulatedMapModel;

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
        createSimulatedUser = false;
        marker = new Marker(new LatLng(SEVILLE.getLat(), SEVILLE.getLng()));
        generateSimulatedTracks();
    }

    public String getMarkerLatitudeLongitude() {
        return marker.getLatlng().getLat() + "," + marker.getLatlng().getLng();
    }

    public void generateSimulatedTracks() {
        String json;
        simulatedMapModel = new DefaultMapModel();
        trackInfoList = new ArrayList();

        for (int i = 0; i < tracksAmount; i++) {
            try {
                Location origin = getRandomLocation(SEVILLE.getLat(), SEVILLE.getLng(), distanceFromSevilleCenter);
                Location destination = getRandomLocation(origin.getLat(), origin.getLng(), distance);
                json = IOUtils.toString(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.getLat() + "," + origin.getLng() + "&destination=" + destination.getLat() + "," + destination.getLng()), "UTF-8");
                GeocodedWaypoints gcwp = new Gson().fromJson(json, GeocodedWaypoints.class);
                TrackInfo trackInfo = createTrack(gcwp);
                if (trackInfo != null) {
                    trackInfoList.add(trackInfo);
                }

//            Circle circle1 = new Circle(originLatLng, distance * 1000);
//            circle1.setStrokeColor("#d93c3c");
//            circle1.setFillColor("#d93c3c");
//            circle1.setFillOpacity(0.2);
//            mapModel.addOverlay(circle1);
            } catch (IOException ex) {
                Logger.getLogger(LocationLogController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public MapModel getSimulatedMapModel() {
        return simulatedMapModel;
    }

    private TrackInfo createTrack(GeocodedWaypoints gcwp) {
        TrackInfo trackInfo = null;

        if (gcwp.getRoutes() != null) {
            trackInfo = new TrackInfo();
            Polyline polyline = new Polyline();
            polyline.setStrokeWeight(4);
            polyline.setStrokeOpacity(0.7);

//            Polyline polyline2 = new Polyline();
//            polyline2.setStrokeWeight(4);
//            polyline2.setStrokeOpacity(0.7);
//            polyline2.setStrokeColor("#FF0000");

//            polyline.setStrokeColor("#00FF00");
            Random rand = new Random();
//            polyline.setStrokeColor(String.format("#%02x", rand.nextInt(0x100)) + "FF" + String.format("%02x", rand.nextInt(0x100)));
            polyline.setStrokeColor("#00" + String.format("%02x", rand.nextInt(0x100)) + "00");

            LocationLog ll = new LocationLog();

            if (createSimulatedUser) {
                // Creamos un usuario simulado, al que le asignaremos el trayecto.
                Person person = new Person();
                Date currentTime = new Date();
                String name = "Sim_" + currentTime.getTime();
                person.setFullName(name);
                person.setUsername(name);
                person.setPassword("hermes");

                // Creamos un objeto de localizaciones de 'SmartDriver'.
                ll = new LocationLog();
                ll.setDateLog(currentTime);
                ll.setFilename(name);
                ll.setPerson(person);

                personFacade.create(person);
                locationLogFacade.create(ll);
            }

            ArrayList<LocationLogDetail> locationLogDetailList = new ArrayList<>();

            for (Route r : gcwp.getRoutes()) {
//                for (Location loc : PolylineDecoder.decodePoly(r.getOverviewPolyline().getPoints())) {
//                    LatLng latlng = new LatLng(loc.getLat(), loc.getLng());
//                    polyline2.getPaths().add(latlng);
//                    simulatedMapModel.addOverlay(createMarker(latlng, true, "https://maps.google.com/mapfiles/ms/micons/red.png"));
//                }
//                simulatedMapModel.addOverlay(polyline2);

                if (r.getLegs() != null) {
                    // Nuestra petición sólo devolverá una ruta.
                    for (Leg l : r.getLegs()) {
                        SimpleStep summary = trackInfo.getSummary();
                        summary.setDistance(l.getDistance().getValue());
                        summary.setDuration(l.getDuration().getValue());
                        summary.setStartLocation(l.getStartLocation());
                        summary.setStartAddress(l.getStartAddress());
                        summary.setEndLocation(l.getEndLocation());
                        summary.setEndAddress(l.getEndAddress());

                        if (l.getSteps() != null) {
                            // Google considera que en cada tramo vamos a una velocidad media durante todo el recorrido, lo cual no es real.
                            // Para acercarnos un poco a esa realidad, consideraremos que esa velocidad media se alcanza en la mitad de ese tramo,
                            // suavizando las uniones de tramos.
                            // Ejemplo:
                            //    - Antes: Tramo B -> 5 puntos -> Velocidad media 12.5Km/h -> 12.5Km/h en los 5 puntos.
                            //    - Ahora: Tramo B -> 5 puntos -> Velocidad media 12.5Km/h
                            //             Tramo A -> Acaba a 8Km/h
                            //             Tramo C -> Velocidad media 20Km/h y 3 puntos
                            //             Por tanto, quedaría algo como: 8Km/h, 10Km/h, 12.5Km/h, 15Km/h, 17Km/h ...
                            double speed = 0.0d;
                            // Recorremos los tramos del trayecto simulado.

                            LocalTime localTime = new LocalTime();
                            for (Step s : l.getSteps()) {
                                Location start = s.getStartLocation();
                                LatLng lls = new LatLng(start.getLat(), start.getLng());
                                polyline.getPaths().add(lls);
                                simulatedMapModel.addOverlay(createMarker(start, false, "https://maps.google.com/mapfiles/ms/micons/red.png"));

                                LocationLogDetail lld = new LocationLogDetail();
                                lld.setLocationLog(ll);
                                lld.setLatitude(start.getLat());
                                lld.setLongitude(start.getLng());
                                lld.setSpeed(speed);
                                lld.setTimeLog(localTime.toDateTimeToday().toDate());

                                int stepDistance = s.getDistance().getValue();
                                int stepDuration = s.getDuration().getValue();

                                locationLogDetailList.add(lld);

                                if (s.getPolyline() != null) {
                                    ArrayList<Location> polylinePointList = PolylineDecoder.decodePoly(s.getPolyline().getPoints());
                                    if (polylinePointList != null) {
                                        Location previous = start;
                                        PolylineStats ps = getPolylineStats(polylinePointList, stepDistance, stepDuration);
                                        if (ps.getMaxDistanceBetweenLocations() > trackInfo.getMaximumLocationsDistance()) {
                                            trackInfo.setMaximumLocationsDistance(ps.getMaxDistanceBetweenLocations());
                                        }
                                        // FIXME
//                                        PolynomialFunction p = new PolynomialFunction(new double[] {speed, averagePolylineSpeed, });

                                        for (int i = 1; i < polylinePointList.size() - 1; i++) {
                                            Location location = polylinePointList.get(i);
                                            polyline.getPaths().add(new LatLng(location.getLat(), location.getLng()));
                                            simulatedMapModel.addOverlay(createMarker(location, false, "https://maps.google.com/mapfiles/ms/micons/red.png"));

                                            // FIXME
//                                            String json = IOUtils.toString(new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.getLat() + "," + origin.getLng() + "&destination=" + destination.getLat() + "," + destination.getLng()), "UTF-8");
//                                            GeocodedWaypoints gcwp = new Gson().fromJson(json, GeocodedWaypoints.class);
//                                            TrackInfo trackInfo = createTrack(gcwp);
                                            lld = new LocationLogDetail();
                                            lld.setLocationLog(ll);
                                            lld.setLatitude(location.getLat());
                                            lld.setLongitude(location.getLng());

                                            // FIXME: Quitar
                                            Double pointDistance = Util.distanceHaversine(previous.getLat(), previous.getLng(), location.getLat(), location.getLng());
                                            Double pointDuration = stepDuration * pointDistance / stepDistance;
                                            // Convertimos la velocidad a Km/h.
                                            speed = pointDistance * 3.6 / pointDuration;
                                            lld.setSpeed(speed);
                                            localTime = localTime.plusSeconds(pointDuration.intValue());
                                            lld.setTimeLog(localTime.toDateTimeToday().toDate());

                                            locationLogDetailList.add(lld);
                                        }
                                    }
                                }

                                Location end = s.getEndLocation();
                                LatLng lle = new LatLng(end.getLat(), end.getLng());
                                polyline.getPaths().add(lle);
                                simulatedMapModel.addOverlay(createMarker(end, false, "https://maps.google.com/mapfiles/ms/micons/red.png"));

                                lld = new LocationLogDetail();
                                lld.setLocationLog(ll);
                                lld.setLatitude(end.getLat());
                                lld.setLongitude(end.getLng());
                                lld.setSpeed(speed);
                                lld.setTimeLog(localTime.toDateTimeToday().toDate());

                                locationLogDetailList.add(lld);

                            }
                        }
                        trackInfo.setTotalLocations(locationLogDetailList.size());
                        trackInfo.setAverageLocationsDistance(summary.getDistance() / locationLogDetailList.size());
                    }
                }
            }
            simulatedMapModel.addOverlay(polyline);

            if (createSimulatedUser) {
                ll.setLocationLogDetailList(locationLogDetailList);
                locationLogFacade.edit(ll);
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

        LOG.log(Level.FINE, "Longitud: {0}, Latitud: {1}", new Object[]{foundLongitude, foundLatitude});

        Location result = new Location();
        result.setLat(foundLatitude);
        result.setLng(foundLongitude);

        return result;
    }

    private Marker createMarker(LatLng latLng, boolean showMarker, String iconUrl) {
        StringBuilder sb = new StringBuilder();
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("Time")).append(": ").append(Constants.dfTime.format(location.getTimeLog()));
//        sb.append(" ");
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("HeartRate")).append(": ").append(Integer.toString(location.getHeartRate()));
//        sb.append(" ");
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("Speed")).append(": ").append(location.getSpeed());
//        sb.append(" (").append(location.getLatitude()).append(", ").append(location.getLongitude()).append(")");

        Marker m = new Marker(latLng, sb.toString(), iconUrl);
        m.setVisible(showMarker);

        return m;
    }

    private Marker createMarker(Location location, boolean showMarker, String iconUrl) {
        return createMarker(new LatLng(location.getLat(), location.getLng()), showMarker, iconUrl);
    }

    private PolylineStats getPolylineStats(ArrayList<Location> polylinePointList, int stepDistance, int stepDuration) {
        PolylineStats ps = new PolylineStats();
        // Comprobamos si tiene más de 2 puntos.
        if (polylinePointList != null && polylinePointList.size() > 2) {
            Location p1 = polylinePointList.get(0);
            Location p2 = polylinePointList.get(1);
            double pointDistance = Util.distanceHaversine(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
            double pointDuration = stepDuration * pointDistance / stepDistance;
            // Convertimos la velocidad a Km/h.
            ps.setAvgSpeed(pointDistance * 3.6 / pointDuration);

            for (int i = 1; i < polylinePointList.size(); i++) {
                p1 = polylinePointList.get(i);
                p2 = polylinePointList.get(i - 1);
                pointDistance = Util.distanceHaversine(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
                if (pointDistance > ps.getMaxDistanceBetweenLocations()) {
                    ps.setMaxDistanceBetweenLocations(pointDistance);
                }
                if (pointDistance < ps.getMinDistanceBetweenLocations()) {
                    ps.setMinDistanceBetweenLocations(pointDistance);
                }
            }
        }

        return ps;
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

    class PolylineStats {

        private double avgSpeed;
        private double maxDistanceBetweenLocations;
        private double minDistanceBetweenLocations;

        public PolylineStats() {
            avgSpeed = 0.0d;
            maxDistanceBetweenLocations = 0.0d;
            minDistanceBetweenLocations = Double.MAX_VALUE;
        }

        public double getAvgSpeed() {
            return avgSpeed;
        }

        public void setAvgSpeed(double avgSpeed) {
            this.avgSpeed = avgSpeed;
        }

        public double getMaxDistanceBetweenLocations() {
            return maxDistanceBetweenLocations;
        }

        public void setMaxDistanceBetweenLocations(double maxDistanceBetweenLocations) {
            this.maxDistanceBetweenLocations = maxDistanceBetweenLocations;
        }

        public double getMinDistanceBetweenLocations() {
            return minDistanceBetweenLocations;
        }

        public void setMinDistanceBetweenLocations(double minDistanceBetweenLocations) {
            this.minDistanceBetweenLocations = minDistanceBetweenLocations;
        }
    }
}
