package ru.pricat.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotFilteredResources {
    protected static boolean getExcludedPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.debug("getExcludedPath -> path: {}", path);
        return path.startsWith("/webjars/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/actuator/") ||
               path.equals("/configuration/ui") ||
               path.equals("/configuration/security") ||
               path.equals("/login") ||
               path.equals("/register") ||
               path.startsWith("/internal/") ||
               path.startsWith("/api/v1/auth/");
    }
}
