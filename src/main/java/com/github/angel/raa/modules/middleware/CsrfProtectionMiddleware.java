package com.github.angel.raa.modules.middleware;

import com.github.angel.raa.modules.core.Request;
import com.github.angel.raa.modules.core.Response;
import org.json.JSONObject;

import java.util.UUID;

/***
 * Middleware para proteger contra ataques CSRF
 */
public class CsrfProtectionMiddleware implements Middleware {
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    private static final String CSRF_TOKEN_SESSION_KEY = "csrfToken";

    @Override
    public boolean handle(Request request, Response response, MiddlewareChain chain) {
        String sessionCsrfToken = (String) request.getSessionAttribute(CSRF_TOKEN_SESSION_KEY);
        if (sessionCsrfToken == null) {
            sessionCsrfToken = UUID.randomUUID().toString();
            request.setAttribute(CSRF_TOKEN_SESSION_KEY, sessionCsrfToken);
        }

        // Validar el token en solicitudes no seguras
        if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()) ||
                "DELETE".equalsIgnoreCase(request.getMethod())) {

            String requestCsrfToken = request.getHeader(CSRF_TOKEN_HEADER);
            if (requestCsrfToken == null || !requestCsrfToken.equals(sessionCsrfToken)) {
                response.setStatus(403); // Forbidden
                response.setBody(new JSONObject().put("error", "Invalid or missing CSRF token"));
                return false; // Detener la cadena
            }
        }

        response.addHeader(CSRF_TOKEN_HEADER, sessionCsrfToken);
        return chain.next(request, response);
    }
}
