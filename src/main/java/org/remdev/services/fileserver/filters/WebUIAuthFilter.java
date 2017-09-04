package org.remdev.services.fileserver.filters;

import org.remdev.services.fileserver.auth.AuthService;
import org.remdev.services.fileserver.models.ClientData;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@WebFilter("/ui/*")
public class WebUIAuthFilter implements Filter {

    public static final String CLIENT_ID_ATTRIBUTE = "ClientID";
    public static final String TOKEN_ATTRIBUTE = "token";
    public static final String HEADER_TOKEN = "AuthToken";

    protected final AuthService authService;

    public WebUIAuthFilter() {
        authService = null;
    }

    public WebUIAuthFilter(AuthService authService) {
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
        // try take from cookies
        if (tokenData.isPresent() == false && request instanceof HttpServletRequest) {
            Cookie[] cookies = ((HttpServletRequest) request).getCookies();
            tokenData = Optional.ofNullable(cookies)
                    .map(Stream::of)
                    .map(cookieStream -> cookieStream.filter(cookie -> cookie.getName().equals(HEADER_TOKEN)).findAny().orElse(null))
                    .map(Cookie::getValue);
        }
        if (tokenData.isPresent() == false) {
            tokenData = Optional.ofNullable(request.getParameter(HEADER_TOKEN));
        }
        Optional<String> clientId = tokenData.map(authService::getClient).map(ClientData::getLogin);
        if (clientId.isPresent() == false && response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ((HttpServletResponse) response).addCookie(generateCookie(null));
                ((HttpServletResponse) response).sendRedirect("/login.html");
            return;
        }
        request.setAttribute(CLIENT_ID_ATTRIBUTE, clientId.get());
        request.setAttribute(TOKEN_ATTRIBUTE, tokenData.get());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }

    public static Cookie generateCookie(ClientData clientData) {
        Cookie cookie;
        if (clientData != null) {
            cookie = new Cookie(WebUIAuthFilter.HEADER_TOKEN, clientData.getToken());
        } else {
            cookie = new Cookie(WebUIAuthFilter.HEADER_TOKEN, "");
            cookie.setMaxAge(0);
        }
        cookie.setPath("/");
        return cookie;
    }
}
