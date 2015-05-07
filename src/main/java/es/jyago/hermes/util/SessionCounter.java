/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.util;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 * @author Jorge Yago
 */
public class SessionCounter implements HttpSessionListener {
    private static final List sessions = new ArrayList();
 
    public SessionCounter() {
    }
 
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        sessions.add(session.getId());
 
        session.setAttribute("sessionsCounter", this);
    }
 
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        sessions.remove(session.getId());
 
        session.setAttribute("sessionsCounter", this);
    }
 
    public int getActiveSessionNumber() {
        return sessions != null ? sessions.size() : 0;
    }
}
