/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.login;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jorge Yago
 */
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        LoginBean loginBean = (LoginBean) req.getSession().getAttribute("userLogin");

        if (loginBean != null && loginBean.isLoggedIn()) {
            // El usuario está registrado, lo redirigimos a la página solicitada.
            chain.doFilter(request, response);
        } else {
            // El usuario no está registrado, lo redirigimos a la página de login.
            HttpServletResponse res = (HttpServletResponse) response;
//            request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
//            res.sendRedirect("http://localhost:8080/AnalizaMisPasos");
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
