/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.login;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filtro de acceso por rol de ADMINISTRADOR. Para cualquier petición de URL, si
 * el usuario no es ADMINISTRADOR, se le manda a la página de inicio. Las URLs
 * que se filtrarán se definen en el 'web.xml'.
 *
 * @author Jorge Yago
 */
public class AdminRoleFilter implements Filter {

    // TODO: Cambiar la ruta filtrada, para adaptarla a los cambios de URL de PrettyFaces.
    private static final Logger log = Logger.getLogger(AdminRoleFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        LoginController loginBean = (LoginController) req.getSession().getAttribute("loginController");

        if (loginBean != null && loginBean.isLoggedIn() && loginBean.getPerson().isAdmin()) {
            // El usuario está registrado y es ADMINISTRADOR, lo redirigimos a la página solicitada.
            chain.doFilter(request, response);
        } else {
            // El usuario no está registrado o no es ADMINISTRADOR, lo redirigimos a la página de inicio.
            log.log(Level.SEVERE, "doFilter() - El usuario no tiene permiso");
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect(req.getContextPath() + "/faces/index.xhtml");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
