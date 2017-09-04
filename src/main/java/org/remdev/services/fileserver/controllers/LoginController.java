package org.remdev.services.fileserver.controllers;

import org.remdev.services.fileserver.auth.AuthService;
import org.remdev.services.fileserver.filters.WebUIAuthFilter;
import org.remdev.services.fileserver.models.ClientData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@RestController
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final AuthService authService;

    @Autowired
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/dologin")
    public ResponseEntity<String> login(
            @RequestParam("username") String login,
            @RequestParam("password") String password,
            HttpServletResponse response) throws IOException {

        log.debug("Authenticating user login = {}", login);

        Objects.requireNonNull(login);
        Objects.requireNonNull(password);

        ClientData clientData = authService.authenticate(login, password);
        if (clientData == null) {
            response.sendRedirect("/ui/home.html");
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }
        Cookie cookie = WebUIAuthFilter.generateCookie(clientData);
        response.addCookie(cookie);
        response.sendRedirect("/ui/home.html");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/ui/logout")
    public ResponseEntity<String> logout(
            @RequestAttribute(WebUIAuthFilter.TOKEN_ATTRIBUTE) String token,
            HttpServletResponse response) throws IOException {

        log.debug("Authenticating user logout = {}", token);

        Objects.requireNonNull(token);

        ClientData clientData = authService.removeToken(token);
        if (clientData == null) {
            response.sendRedirect("/login.html");
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }
        Cookie cookie = WebUIAuthFilter.generateCookie(clientData);
        response.addCookie(cookie);
        response.sendRedirect("/login.html");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
