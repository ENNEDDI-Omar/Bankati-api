package org.projects.eBankati.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthenticationFilter implements Filter {
    private List<String> excludedUrls;

    @Override
    public void init(FilterConfig filterConfig) {
        String excludePattern = filterConfig.getInitParameter("excludePatterns");
        excludedUrls = excludePattern != null ?
                Arrays.asList(excludePattern.split(",")) :
                Arrays.asList("/api/auth/login", "/api/auth/register");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Log pour le débogage
        log.debug("Processing request to path: {}", path);
        log.debug("Excluded URLs: {}", excludedUrls);

        // Permettre les requêtes OPTIONS (CORS preflight)
        if (httpRequest.getMethod().equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // Vérifier si l'URL est exclue
        if (isExcludedUrl(path)) {
            log.debug("URL is excluded from authentication: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // Vérifier la session
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userEmail") == null) {
            log.debug("Unauthorized access attempt to: {}", path);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("User not authenticated");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExcludedUrl(String path) {
        return excludedUrls.stream().anyMatch(path::endsWith);
    }

    @Override
    public void destroy() {
        // Cleanup si nécessaire
    }
}