package com.foxconn.fii.security.ajax;

import com.foxconn.fii.main.data.model.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.foxconn.fii.main.service.UserService;

@Slf4j
@Component
public class AjaxAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.notNull(authentication, "No authentication data provided");

        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        UserDetails user = userService.loadUserByUsername(username);
        System.out.println("########: "+encoder.encode(password));
        System.out.println("########: "+user.getPassword());


        if (!encoder.matches(password, user.getPassword())) {
            userService.increaseFailedLoginNumber(user.getUsername());

            throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
        }

        userService.resetFailedLoginNumber(user.getUsername());

        if (((UserContext) user).getUser().getPwdExpiredTime() == null || ((UserContext) user).getUser().getPwdExpiredTime().getTime() - System.currentTimeMillis() < 0) {
            throw new BadCredentialsException("PWD_EXPIRED");
        }

        if (user.getAuthorities() == null) {
            throw new InsufficientAuthenticationException("User has no roles assigned");
        }

//        UserContext userContext = new UserContext(user.getUsername(), user.getPassword(), new ArrayList<>(user.getAuthorities()), user);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
