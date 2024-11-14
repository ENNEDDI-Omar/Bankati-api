package org.projects.eBankati.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthenticationFilter implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Autoriser l'accès aux endpoints d'authentification
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        // Vérifier la session
//        HttpSession session = httpRequest.getSession(false);
//        if (session == null || session.getAttribute("userEmail") == null) {
//            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
//            return;
//        }

        chain.doFilter(request, response);
    }
}