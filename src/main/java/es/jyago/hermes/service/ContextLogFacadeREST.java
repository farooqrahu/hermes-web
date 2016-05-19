package es.jyago.hermes.service;

import es.jyago.hermes.AbstractFacade;
import es.jyago.hermes.contextLog.ContextLog;
import es.jyago.hermes.contextLog.ContextLogDetail;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import es.jyago.hermes.util.Constants;
import es.jyago.hermes.util.HermesException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

/**
 * Ejemplo de URL para probar:
 * http://localhost:8080/HermesWeb/webresources/hermes.citizen.context/create
 * Pasándole el JSON correspondiente. Es útil el plugin de Chrome: Advanced REST
 * Client
 */
@Stateless
@Path("hermes.citizen.context")
public class ContextLogFacadeREST extends AbstractFacade<ContextLog> {

    private static final Logger LOG = Logger.getLogger(ContextLogFacadeREST.class.getName());

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @Inject
    private PersonFacade personFacade;

    public ContextLogFacadeREST() {
        super(ContextLog.class);
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public int create(AndroidContext androidContext) {
        try {
            if (androidContext != null) {
                processAndroidContexts(androidContext);
                return Constants.REST_OK;
            }
        } catch (HermesException ex) {
            LOG.log(Level.SEVERE, "create() - Error controlado", ex.getMessage());
            return ex.getCode();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "create() - Error no controlado", ex.getMessage());
            return Constants.REST_ERROR;
        }

        return Constants.REST_ERROR;
    }

    @POST
    @Path("/createRange")
    @Consumes(MediaType.APPLICATION_JSON)
    public int createRange(AndroidContext androidContext) {
        try {
            if (androidContext != null && androidContext.getItems() != null && !androidContext.getItems().isEmpty()) {
                LocalDate start = new LocalDate(androidContext.getItems().get(0).startTime);
                LOG.log(Level.INFO, "createRange() - Recepción de contexto de: {0} del día {1}", new Object[]{androidContext.getUser(), start});
                int days = Days.daysBetween(start, new LocalDate(androidContext.getItems().get(androidContext.getItems().size() - 1).endTime)).getDays() + 1;
                for (int i = 0; i < days; i++) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(start.toDate().getTime());
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);

                    DateTime firstMinute = new DateTime(c);
                    firstMinute = firstMinute.plusDays(i);
                    DateTime lastMinute = firstMinute.plusDays(1);
                    Interval dayInterval = new Interval(firstMinute, lastMinute);

                    AndroidContext clone = (AndroidContext) androidContext.clone();
                    for (int j = clone.getItems().size() - 1; j >= 0; j--) {
                        AndroidContextDetail acd = clone.getItems().get(j);

                        Interval itemInterval = new Interval(acd.getStartTime(), acd.getEndTime());
                        if (!dayInterval.overlaps(itemInterval)) {
                            // Eliminamos los intervalos que no estén en el día.
                            clone.getItems().remove(acd);
                        } else {
                            if (itemInterval.getStart().isBefore(dayInterval.getStart()) || itemInterval.getStart().isEqual(dayInterval.getStart())) {
                                // El intervalo comienza antes, lo redefinimos.
                                acd.setStartTime(dayInterval.getStartMillis());
                            }
                            if (itemInterval.getEnd().isAfter(dayInterval.getEnd())) {
                                // El intervalo acaba después, lo redefinimos.
                                acd.setEndTime(dayInterval.getEndMillis());
                            }
                        }
                    }

                    if (!clone.getItems().isEmpty()) {
                        processAndroidContexts(clone);
                    }
                }

                return Constants.REST_OK;
            }
        } catch (HermesException ex) {
            LOG.log(Level.SEVERE, "create() - Error controlado", ex.getMessage());
            return ex.getCode();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "create() - Error no controlado", ex.getMessage());
            return Constants.REST_ERROR;
        }

        return Constants.REST_ERROR;
    }

    private void processAndroidContexts(AndroidContext androidContext) throws HermesException {
        Person person = null;

        // Buscamos la persona por su e-mail.
        try {
            person = (Person) personFacade.getEntityManager().createNamedQuery("Person.findByEmail")
                    .setParameter("email", androidContext.user)
                    .getSingleResult();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "processAndroidContexts() - No se ha encontrado el usuario por email: {0}", androidContext.getUser());
            throw new HermesException(Constants.REST_ERROR_USER_NOT_FOUND);
        }

        // Cada objeto traerá información de 1 día.
        try {
            // En la B.D. tenemos una estructura maestro-detalle, mientras que de Android nos llegan los datos como elementos individuales.
            // Comprobamos si contiene contextos.
            if (androidContext.getItems() != null && !androidContext.getItems().isEmpty()) {

                ContextLog contextLog;
                // Los datos de Android nos llegan por intervalos temporales.
                // Tomamos la fecha de inicio del intervalo.
                LocalDate parentDate = new LocalDate(androidContext.getItems().get(0).getStartTime());

                // Buscamos por si existe un contexto previo registrado en la B.D.
                List<ContextLog> contextLogList = em.createNamedQuery("ContextLog.findByPersonAndDateLog").setParameter("personId", person.getPersonId()).setParameter("dateLog", parentDate.toDate()).getResultList();
                if (contextLogList != null && !contextLogList.isEmpty()) {
                    // Actualizamos los datos del registro de contexto.
                    contextLog = contextLogList.get(0);
                    List<ContextLogDetail> acdList = new ArrayList<>();
                    // Procesamos el conjunto de datos.
                    for (AndroidContextDetail acd : androidContext.getItems()) {
                        acdList.addAll(createContextLogDetail(acd, contextLog));
                    }
                    List<ContextLogDetail> removeList = new ArrayList();
                    for (ContextLogDetail cld : contextLog.getContextLogDetailList()) {
                        for (int i = acdList.size() - 1; i >= 0; i--) {
                            ContextLogDetail newAcd = acdList.get(i);
                            if (Constants.dfSimpleTime.format(newAcd.getTimeLog()).equals(Constants.dfSimpleTime.format(cld.getTimeLog()))) {
                                if (cld.getAccuracy() == null) {
                                    cld.setAccuracy(newAcd.getAccuracy());
                                    cld.setSent(false);
                                }
                                if (cld.getLatitude() == null) {
                                    cld.setLatitude(newAcd.getLatitude());
                                    cld.setSent(false);
                                }
                                if (cld.getLongitude() == null) {
                                    cld.setLongitude(newAcd.getLongitude());
                                    cld.setSent(false);
                                }
                                if (cld.getDetectedActivity() == null) {
                                    cld.setDetectedActivity(newAcd.getDetectedActivity());
                                    cld.setSent(false);
                                }
                                removeList.add(acdList.get(i));
                                break;
                            }
                        }
                    }
                    acdList.removeAll(removeList);
                    contextLog.getContextLogDetailList().addAll(acdList);
                    contextLog.setSent(false);
                } else {
                    // Es un registro de contexto nuevo.
                    contextLog = new ContextLog();
                    // Creamos el registro padre para almacenarlo en la B.D.
                    contextLog.setPerson(person);
                    contextLog.setDateLog(parentDate.toDate());
                    contextLog.setContextLogDetailList(new ArrayList());
                    contextLog.setSent(false);
                    // Procesamos el conjunto de datos.
                    for (AndroidContextDetail acd : androidContext.getItems()) {
                        contextLog.getContextLogDetailList().addAll(createContextLogDetail(acd, contextLog));
                    }
                }

                // Lo registramos en la B.D.
                super.create(contextLog);
            } else {
                LOG.log(Level.SEVERE, "processAndroidContexts() - No hay datos de contextos");
                throw new HermesException(Constants.REST_ERROR_NO_CONTEXT_DATA);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "processAndroidContexts() - Error al procesar los contextos", ex.getMessage());
            throw new HermesException(Constants.REST_ERROR_IN_DATA);
        }
    }

    private List<ContextLogDetail> createContextLogDetail(AndroidContextDetail acd, ContextLog contextLog) {
        Calendar start = Calendar.getInstance();
        start.setTime(new Date(acd.getStartTime()));
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.SECOND, 0);
        Calendar end = Calendar.getInstance();
        end.setTime(new Date(acd.getEndTime()));
        end.set(Calendar.MILLISECOND, 0);
        end.set(Calendar.SECOND, 0);
        DateTime min = new DateTime(start);
        DateTime max = new DateTime(end);
        int minutes = org.joda.time.Minutes.minutesBetween(min, max).getMinutes();
        List<ContextLogDetail> cldList = new ArrayList<>();

        for (int i = 0; i < minutes; i++) {
            DateTime t = min.withFieldAdded(DurationFieldType.minutes(), i);
            ContextLogDetail cld = new ContextLogDetail();

            cld.setDetectedActivity(acd.getName());
            cld.setLatitude(acd.getLatitude());
            cld.setLongitude(acd.getLongitude());
            cld.setAccuracy(acd.getAccuracy());
            cld.setDetectedActivity(acd.getName());
            cld.setTimeLog(new Date(t.getMillis()));

            cld.setContextLog(contextLog);

            cldList.add(cld);
        }

        return cldList;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // JYFR: Para usarlo con Jersey, la clase interna debe ser pública y estática.
    public static class AndroidContext implements Cloneable {

        private String user;
        private List<AndroidContextDetail> items;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public List<AndroidContextDetail> getItems() {
            return items;
        }

        public void setItems(List<AndroidContextDetail> items) {
            this.items = items;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            AndroidContext obj = null;
            try {
                obj = (AndroidContext) super.clone();
                List<AndroidContextDetail> details = new ArrayList<AndroidContextDetail>(this.getItems().size());
                for (AndroidContextDetail item : getItems()) {
                    details.add((AndroidContextDetail) item.clone());
                }
                obj.setItems(details);
            } catch (CloneNotSupportedException ex) {
                LOG.log(Level.SEVERE, "clone() - Error al clonar el objeto de tipo 'AndroidContext", ex.getMessage());
            }
            return obj;
        }

    }

    public static class AndroidContextDetail implements Cloneable {

        private String name;
        private Double latitude;
        private Double longitude;
        private Float accuracy;
        private Long startTime;
        private Long endTime;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Float getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(Float accuracy) {
            this.accuracy = accuracy;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            AndroidContextDetail obj = null;

            try {
                obj = (AndroidContextDetail) super.clone();
            } catch (CloneNotSupportedException ex) {
                LOG.log(Level.SEVERE, "clone() - Error al clonar el objeto de tipo 'AndroidContextDetail", ex.getMessage());
            }

            return obj;
        }
    }
}
