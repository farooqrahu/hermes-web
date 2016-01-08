package es.jyago.hermes.service;

import es.jyago.hermes.contextLog.ContextLog;
import es.jyago.hermes.contextLog.ContextLogDetail;
import es.jyago.hermes.contextLog.ContextLogFacade;
import es.jyago.hermes.person.Person;
import es.jyago.hermes.person.PersonFacade;
import es.jyago.hermes.util.HermesException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.joda.time.LocalDate;

/**
 *
 * @author Jorge Yago Ejemplo de URL para probar:
 * http://localhost:8080/HermesWeb/webresources/hermes.citizen.context/create
 * Pasándole el JSON correspondiente. Es útil el plugin de Chrome: Advanced REST
 * Client
 */
@Stateless
@Path("hermes.citizen.context")
public class ContextLogFacadeREST extends ContextLogFacade {

    private static final int ERROR = 0;
    private static final int OK = 1;
    private static final int USER_NOT_FOUND = 2;
    private static final int ERROR_IN_DATA = 3;
    private static final int NO_CONTEXT_DATA = 4;

    private static final Logger log = Logger.getLogger(ContextLogFacadeREST.class.getName());

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    @EJB
    private PersonFacade personFacade;

    @Resource
    private SessionContext ctx;

    public ContextLogFacadeREST() {
        super();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public int create(AndroidContext androidContext) {
        try {
            if (androidContext != null) {
                processAndroidContexts(androidContext);
                return OK;
            }
        } catch (HermesException ex) {
            ctx.setRollbackOnly();
            return ex.getCode();
        } catch (Exception ex) {
            ctx.setRollbackOnly();
            return ERROR;
        }

        return ERROR;
    }

    private void processAndroidContexts(AndroidContext androidContext) throws HermesException {
        Person person = null;

        // Buscamos la persona por su e-mail.
        try {
            person = (Person) personFacade.getEntityManager().createNamedQuery("Person.findByEmail")
                    .setParameter("email", androidContext.user)
                    .getSingleResult();

        } catch (Exception ex) {
            log.log(Level.SEVERE, "processAndroidContext() - No se ha encontrado el usuario por email: {0}", androidContext.getUser());
            throw new HermesException(USER_NOT_FOUND);
        }

        // Procesamos los datos de los contextos, ya que pueden venir de varios días, no sólo de 1.
        try {
            // En la B.D. tenemos una estructura maestro-detalle, mientras que de Android nos llegan los datos como elementos individuales.
            // Comprobamos si contiene contextos.
            if (androidContext.getContexts() != null && !androidContext.getContexts().isEmpty()) {
                AndroidContextDetail acd = androidContext.getContexts().get(0);

                ContextLog contextLog;
                LocalDate parentDate = new LocalDate(acd.getTime());
                // Buscamos por si existe un contexto previo registrado en la B.D.
                List<ContextLog> contextLogList = super.findByPersonAndDate(person, parentDate.toDate());
                if (contextLogList != null && !contextLogList.isEmpty()) {
                    contextLog = contextLogList.get(0);
                } else {
                    contextLog = new ContextLog();
                }
                // Al menos, nos está llegando un elemento, creamos el registro padre para almacenarlo en la B.D.
                contextLog.setDeviceId(androidContext.deviceId);
                contextLog.setPerson(person);
                contextLog.setDateLog(parentDate.toDate());
                // Creamos el listado de detalles.
                List<ContextLogDetail> contextLogDetailList = new ArrayList();

                ContextLogDetail cld = createContextLogDetail(acd);
                cld.setContextLog(contextLog);
                contextLogDetailList.add(cld);

                // Procesamos el resto de elementos, si los hubiera, teniendo en cuenta que si cambia de fecha, habrá que crear un elemento padre nuevo.
                for (int i = 1; i < androidContext.getContexts().size(); i++) {
                    acd = androidContext.getContexts().get(i);
                    LocalDate currentDate = new LocalDate(acd.getTime());
                    // Si tienen la misma fecha, serán del mismo elemento padre.
                    if (!parentDate.equals(currentDate)) {
                        // Tienen fecha distinta -> Registramos el contexto padre actual con todos los detalles y creamos uno nuevo.
                        // Asignamos la lista de detalles actual.
                        contextLog.setContextLogDetailList(contextLogDetailList);
                        // Lo registramos en la B.D.
                        super.create(contextLog);

                        // Creamos un nuevo elemento padre con un detalle.
                        contextLog = new ContextLog();
                        contextLog.setDeviceId(androidContext.deviceId);
                        contextLog.setPerson(person);
                        parentDate = new LocalDate(acd.getTime());
                        contextLog.setDateLog(parentDate.toDate());
                        // Creamos el listado de detalles.
                        contextLogDetailList = new ArrayList();
                    }
                    cld = createContextLogDetail(acd);
                    cld.setContextLog(contextLog);
                    contextLogDetailList.add(cld);
                }

                // Asignamos la lista de detalles.
                contextLog.setContextLogDetailList(contextLogDetailList);

                // Lo registramos en la B.D.
                super.create(contextLog);
            } else {
                log.log(Level.SEVERE, "processAndroidContext() - No hay datos de contextos");
                throw new HermesException(NO_CONTEXT_DATA);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "processAndroidContext() - Error al procesar los contextos", ex.getMessage());
            throw new HermesException(ERROR_IN_DATA);
        }
    }

    private ContextLogDetail createContextLogDetail(AndroidContextDetail acd) {
        ContextLogDetail cld = new ContextLogDetail();

        cld.setDetectedActivity(acd.getActivity());
        cld.setLatitude(acd.getLatitude());
        cld.setLongitude(acd.getLongitude());
        cld.setTimeLog(new Date(acd.getTime()));

        return cld;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // JYFR: Para usarlo con Jersey, la clase interna debe ser pública y estática.
    public static class AndroidContext {

        private String deviceId;
        private String user;
        private List<AndroidContextDetail> contexts;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public List<AndroidContextDetail> getContexts() {
            return contexts;
        }

        public void setContexts(List<AndroidContextDetail> contexts) {
            this.contexts = contexts;
        }
    }

    public static class AndroidContextDetail {

        private Integer activity;
        private Double latitude;
        private Double longitude;
        private Long time;

        public Integer getActivity() {
            return activity;
        }

        public void setActivity(Integer activity) {
            this.activity = activity;
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

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

    }
}
