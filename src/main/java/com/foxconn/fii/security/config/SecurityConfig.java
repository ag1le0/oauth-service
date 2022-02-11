package com.foxconn.fii.security.config;

import com.foxconn.fii.security.ajax.AjaxAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import java.util.Arrays;

@Order(1)
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AjaxAuthenticationProvider ajaxAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
//                .ignoringAntMatchers("/oauth/recognize", "/api/user/forgot-password")
//                .and()
                .disable()
                .logout().logoutUrl("/sign-out")
                .and()
                .formLogin()
                .loginPage("/login")
                .failureHandler(new CustomerAuthenticationFailureHandler())
                .permitAll()
                .and()
                .logout()
                .and()
                .requestMatchers()
                .antMatchers("/login", "/logout", "/sign-out", "/oauth/authorize", "/login-with-icivet", "/login-with-sso")
                .antMatchers("/change-password", "/register", "/user-manager", "/user-information", "/")
                .antMatchers("/user/**", "/mng/**")
                .antMatchers("/oauth/recognize", "/oauth/recognize-information", "/api/user/reset-password", "/api/user/get-email", "/api/user/forgot-password", "/api/user/forgot-password/check-otp", "/api/user/unlock-account")
                .antMatchers("/oauth/user/information", "/oauth/user/change-password")
                .and()
                .authorizeRequests()
                .antMatchers("/login", "/login-with-icivet", "/login-with-sso").permitAll()
                .antMatchers("/register", "/change-password").permitAll()
                .antMatchers("/oauth/recognize", "/oauth/recognize-information", "/api/user/get-email", "/api/user/reset-password", "/api/user/forgot-password", "/api/user/forgot-password/check-otp", "/api/user/unlock-account").permitAll()
                .antMatchers("/oauth/user/information", "/oauth/user/change-password").permitAll()
                .antMatchers("/mng/**").hasAnyRole("OAUTH_ADMIN", "OAUTH_MANAGER")
                .anyRequest().authenticated()
        ;

        http.requestCache().requestCache(requestCache());

//        http.addFilterBefore(new LanguageFilter(), AbstractPreAuthenticatedProcessingFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/uploads/**", "/assets/**", "/templates/**", "/WEB-INF/jsp/**", "/favicon.ico");
    }

    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        auth.authenticationProvider(ajaxAuthenticationProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public RequestCache requestCache() {
        return new HttpSessionRequestCache();
    }
}
