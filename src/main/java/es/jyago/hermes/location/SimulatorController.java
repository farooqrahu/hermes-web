package es.jyago.hermes.location;

import com.google.gson.Gson;
import es.jyago.hermes.location.detail.LocationLogDetail;
import es.jyago.hermes.location.google.GeocodedWaypoints;
import es.jyago.hermes.location.google.Leg;
import es.jyago.hermes.location.google.Location;
import es.jyago.hermes.location.google.PolylineDecoder;
import es.jyago.hermes.location.google.Route;
import es.jyago.hermes.location.google.SimpleStep;
import es.jyago.hermes.location.google.Step;
import es.jyago.hermes.location.google.TrackInfo;
import es.jyago.hermes.util.Constants;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;

/**
 *
 * @author Jorge Yago
 */
@Named("simulatorController")
@SessionScoped
public class SimulatorController implements Serializable {

    private static final Logger LOG = Logger.getLogger(SimulatorController.class.getName());

    private static final Location SEVILLE = new Location(37.3898358, -5.986069);

    private Marker marker;

    private int distance;
    private int distanceFromSevilleCenter;
    private int tracksAmount;
    private List<TrackInfo> trackInfoList;
    private MapModel simulatedMapModel;

    public SimulatorController() {
    }

    @PostConstruct
    public void init() {
        distanceFromSevilleCenter = 0;
        distance = 10;
        tracksAmount = 1;
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
                Gson gson = new Gson();
                GeocodedWaypoints gcwp = gson.fromJson(json, GeocodedWaypoints.class);
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
//            polyline.setStrokeColor("#00FF00");
            Random rand = new Random();
            polyline.setStrokeColor(String.format("#%02x", rand.nextInt(0x100)) + "FF" + String.format("%02x", rand.nextInt(0x100)));

            for (Route r : gcwp.getRoutes()) {
                if (r.getLegs() != null) {
                    for (Leg l : r.getLegs()) {
                        SimpleStep summary = new SimpleStep();
                        summary.setDistance(l.getDistance().getValue());
                        summary.setDuration(l.getDuration().getValue());
                        summary.setStartLocation(l.getStartLocation());
                        summary.setStartAddress(l.getStartAddress());
                        summary.setEndLocation(l.getEndLocation());
                        summary.setEndAddress(l.getEndAddress());
                        trackInfo.setSummary(summary);
                        int totalLocations = 2;

                        if (l.getSteps() != null) {
                            for (Step s : l.getSteps()) {
                                Location start = s.getStartLocation();
                                LatLng lls = new LatLng(start.getLat(), start.getLng());
                                polyline.getPaths().add(lls);
                                if (s.getPolyline() != null) {
                                    ArrayList<Location> plist = PolylineDecoder.decodePoly(s.getPolyline().getPoints());
                                    if (plist != null) {
                                        for (Location location : plist) {
                                            LatLng ll = new LatLng(location.getLat(), location.getLng());
                                            polyline.getPaths().add(ll);
                                            totalLocations++;
                                            simulatedMapModel.addOverlay(createMarker(location, false, "https://maps.google.com/mapfiles/ms/micons/red.png"));
                                        }
                                    }
                                }
                                Location end = s.getEndLocation();
                                LatLng lle = new LatLng(end.getLat(), end.getLng());
                                polyline.getPaths().add(lle);
                                totalLocations++;
                                simulatedMapModel.addOverlay(createMarker(end, false, "https://maps.google.com/mapfiles/ms/micons/red.png"));
                            }
                        }
                        trackInfo.setTotalLocations(totalLocations);
                    }
                }
            }
            simulatedMapModel.addOverlay(polyline);
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

    private Marker createMarker(Location location, boolean showMarker, String iconUrl) {
        LatLng ll = new LatLng(location.getLat(), location.getLng());

        StringBuilder sb = new StringBuilder();
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("Time")).append(": ").append(Constants.dfTime.format(location.getTimeLog()));
//        sb.append(" ");
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("HeartRate")).append(": ").append(Integer.toString(location.getHeartRate()));
//        sb.append(" ");
//        sb.append(ResourceBundle.getBundle("/Bundle").getString("Speed")).append(": ").append(location.getSpeed());
//        sb.append(" (").append(location.getLatitude()).append(", ").append(location.getLongitude()).append(")");

        Marker m = new Marker(ll, sb.toString(), location, iconUrl);
        m.setVisible(showMarker);

        return m;
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
}
