package org.projects.eBankati.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter implements Filter {
    private List<String> excludedUrls;

    @Override
    public void init(FilterConfig filterConfig) {
        String excludePattern = filterConfig.getInitParameter("excludePatterns");
        if (excludePattern != null) {
            excludedUrls = Arrays.asList(excludePattern.split(","));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Vérifier si l'URL est exclue
        if (isExcludedUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Vérifier la session pour les autres URLs
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userEmail") == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExcludedUrl(String path) {
        if (excludedUrls == null) return false;
        return excludedUrls.stream().anyMatch(path::endsWith);
    }

    @Override
    public void destroy() {
        // Cleanup si nécessaire
    }
}