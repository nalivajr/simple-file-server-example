package org.remdev.services.fileserver.filters;

import org.remdev.services.fileserver.auth.AuthService;
import org.remdev.services.fileserver.models.ClientData;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebFilter("/api/*")
public class ApiAuthFilter implements Filter {

    public static final String CLIENT_ID_ATTRIBUTE = "ClientID";
    public static final String HEADER_TOKEN = "AuthToken";

    protected final AuthService authService;

    public ApiAuthFilter() {
        authService = null;
    }

    public ApiAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (authService == null) {
            throw new RuntimeException("Can not build filter");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Optional<String> tokenData = Optional.empty();
        if (request instanceof HttpServletRequest) {
            tokenData = Optional.ofNullable(((HttpServletRequest) request).getHeader(HEADER_TOKEN));
        }
        if (tokenData.isPresent() == false) {
            tokenData = Optional.ofNullable(request.getParameter(HEADER_TOKEN));
        }
        Optional<String> clientId = tokenData.map(authService::getClient).map(ClientData::getLogin);
        if (clientId.isPresent() == false) {
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            response.getOutputStream().write("Authorization failed".getBytes());
            return;
        }
        request.setAttribute(CLIENT_ID_ATTRIBUTE, clientId.get());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
