package com.foxconn.fii.security.config;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomerAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        if (e instanceof BadCredentialsException && "PWD_EXPIRED".equalsIgnoreCase(((BadCredentialsException) e).getMessage())) {
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/change-password?error=PWD_EXPIRED");
        } else {
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login?error=" + e.getMessage());
        }
    }
}
