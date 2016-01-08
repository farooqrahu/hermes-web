/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.fitbit;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class FitbitResetRequestsScheduledTask {

    private static final Logger log = Logger.getLogger(FitbitResetRequestsScheduledTask.class.getName());
    private static final int REQUESTS_RATE_LIMIT = 100;
    private static HashMap<String, Integer> remainingRequests;

    @PostConstruct
    public void onStartup() {
        log.log(Level.INFO, "onStartup() - Inicialización del temporizador programado de reinicio de límite de peticiones a Fitbit");
        remainingRequests = new HashMap();
    }

    // Los reinicios de los límites de peticiones a Fitbit se harán cada hora en punto.
    @Schedule(hour = "*", minute = "0", persistent = false)
    public void run() {
        log.log(Level.INFO, "run() - Reinicio de las peticiones a Fitbit");
        remainingRequests.clear();
    }

    public static int getRemainingRequests(String fitbitId) {
        if (fitbitId != null) {
            // Obtenemos los límites actuales del usuario.
            Integer personRemainingRequests = remainingRequests.get(fitbitId);

            if (personRemainingRequests == null) {
                personRemainingRequests = REQUESTS_RATE_LIMIT;
                remainingRequests.put(fitbitId, personRemainingRequests);
            }

            log.log(Level.INFO, "getRemainingRequests() - Hay disponibles {0} peticiones a Fitbit para el usuario con id: {1}", new Object[]{personRemainingRequests, fitbitId});

            return personRemainingRequests;
        }

        return 0;
    }

    public static void setUsedRequests(String fitbitId, int usedRequests) {
        if (usedRequests > 0) {
            remainingRequests.put(fitbitId, getRemainingRequests(fitbitId) - usedRequests);
        }

        log.log(Level.INFO, "setUsedRequests() - Usadas {0} peticiones a Fitbit del usuario con id: {1}", new Object[]{usedRequests, fitbitId});
    }
}
