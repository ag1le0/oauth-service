package com.foxconn.fii.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@Data
@ConfigurationProperties("security")
public class SecurityProperties {

    private JwtProperties jwt;

    @Data
    public static class JwtProperties {

        private Resource keyStore;

        private String keyStorePassword;

        private String keyPairAlias;

        private String keyPairPassword;
    }
}
