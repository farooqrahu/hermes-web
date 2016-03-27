/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.service;

import es.jyago.hermes.AbstractFacade;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.util.Constants;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import static javax.faces.component.UIInput.isEmpty;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Jorge Yago Ejemplo de URL para probar:
 * http://localhost:8080/HermesWeb/webresources/hermes.citizen.sleep/jorgeyago@gmail.com/2015-11-23
 * Es útil el plugin de Chrome: Advanced REST Client
 */
@Stateless
@Path("hermes.citizen.sleep")
public class SleepLogFacadeREST extends AbstractFacade<SleepLog> {

    private static final Logger LOG = Logger.getLogger(SleepLogFacadeREST.class.getName());

    @PersistenceContext(unitName = "HermesWeb_PU")
    private EntityManager em;

    public SleepLogFacadeREST() {
        super(SleepLog.class);
    }

    @GET
    @Path("/{emailSHA}/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public SleepLog getSleepLog(@PathParam("emailSHA") String emailSHA, @PathParam("date") String date) {

        Date requestedDate = null;

        if (isEmpty(date)) {
            requestedDate = new Date();
        }

        try {
            requestedDate = Constants.dfFitbit.parse(date);
            try {
                SleepLog sl = (SleepLog) em.createNamedQuery("SleepLog.findByEmailSHAAndDate")
                        .setParameter("emailSHA", emailSHA)
                        .setParameter("date", requestedDate)
                        .getSingleResult();

                // FIXME: Ver por qué no se puede enviar directamente el sl.
                //        ¿Puede ser por los datos de Person?
                //        He puesto @JsonIgnore en varios atributos, pero parece que no funciona.
                //        Ver las dependencias del POM estrictamente necesarias.
                SleepLog sl2 = new SleepLog();
                sl2.setDateLog(sl.getDateLog());
                sl2.setMinutesInBed(sl.getMinutesInBed());
                sl2.setMinutesAsleep(sl.getMinutesAsleep());
                sl2.setAwakenings(sl.getAwakenings());
                sl2.setStartTime(sl.getStartTime());
                sl2.setEndTime(sl.getEndTime());

                return sl2;
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "getSleepLog() - No se ha encontrado datos de sueño para el usuario con SHA de email {0} y la fecha {1}", new Object[]{emailSHA, Constants.df.format(requestedDate)});
            }
        } catch (ParseException e) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
                    .entity("No es posible convertir la fecha: " + e.getMessage())
                    .build());
        }

        return null;
    }

    @GET
    @Path("/{emailSHA}")
    @Produces(MediaType.APPLICATION_JSON)
    public SleepLog getSleepLog(@PathParam("emailSHA") String emailSHA) {

        return getSleepLog(emailSHA, Constants.dfFitbit.format(new Date()));
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // FIXME: Probar
// JYFR: Para usarlo con Jersey, la clase interna debe ser pública y estática.
//    public static class SleepLogREST {
//
//        private Date dateLog;
//        private int minutesInBed;
//        private int minutesAsleep;
//        private int awakenings;
//        private Date startTime;
//        private Date endTime;
//
//        public SleepLogREST() {
//        }
//
//        public SleepLogREST(SleepLog sl) {
//            this.dateLog = sl.getDateLog();
//            this.minutesInBed = sl.getMinutesInBed();
//            this.minutesAsleep = sl.getMinutesAsleep();
//            this.awakenings = sl.getAwakenings();
//            this.startTime = sl.getStartTime();
//            this.endTime = sl.getEndTime();
//        }
//
//        public Date getDateLog() {
//            return dateLog;
//        }
//
//        public void setDateLog(Date dateLog) {
//            this.dateLog = dateLog;
//        }
//
//        public int getMinutesInBed() {
//            return minutesInBed;
//        }
//
//        public void setMinutesInBed(int minutesInBed) {
//            this.minutesInBed = minutesInBed;
//        }
//
//        public int getMinutesAsleep() {
//            return minutesAsleep;
//        }
//
//        public void setMinutesAsleep(int minutesAsleep) {
//            this.minutesAsleep = minutesAsleep;
//        }
//
//        public int getAwakenings() {
//            return awakenings;
//        }
//
//        public void setAwakenings(int awakenings) {
//            this.awakenings = awakenings;
//        }
//
//        public Date getStartTime() {
//            return startTime;
//        }
//
//        public void setStartTime(Date startTime) {
//            this.startTime = startTime;
//        }
//
//        public Date getEndTime() {
//            return endTime;
//        }
//
//        public void setEndTime(Date endTime) {
//            this.endTime = endTime;
//        }
//    }
}
