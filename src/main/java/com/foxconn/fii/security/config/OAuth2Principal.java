package com.foxconn.fii.security.config;

import com.foxconn.fii.main.data.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

public class OAuth2Principal extends OAuth2Authentication {

    private User user;

    public OAuth2Principal(OAuth2Authentication auth2Authentication, User user) {
        super(auth2Authentication.getOAuth2Request(), auth2Authentication.getUserAuthentication());
        this.user = user;
    }

    public OAuth2Principal(OAuth2Request storedRequest, Authentication userAuthentication) {
        super(storedRequest, userAuthentication);
    }

    public User getUser() {
        return user;
    }
}
