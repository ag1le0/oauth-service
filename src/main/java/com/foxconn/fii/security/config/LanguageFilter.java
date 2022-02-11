package com.foxconn.fii.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class LanguageFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest &&
                servletResponse instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            if (request.getRequestURI().contains("/oauth/authorize")) {
                String redirectUri = request.getParameter("redirect_uri");
                if (!StringUtils.isEmpty(redirectUri)) {
                    response.addCookie(new Cookie("redirect_uri", redirectUri.replace("/login", "/")));
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
