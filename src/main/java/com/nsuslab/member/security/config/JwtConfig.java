package com.nsuslab.member.security.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;


@Getter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private final String tokenPrefix;
    private final Integer tokenExpirationAfterDays;

    public JwtConfig(String tokenPrefix, Integer tokenExpirationAfterDays) {
        this.tokenPrefix = tokenPrefix;
        this.tokenExpirationAfterDays = tokenExpirationAfterDays;
    }

    public String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;
    }
}
