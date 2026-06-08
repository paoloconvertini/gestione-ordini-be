package it.calolenoci.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "microsoft")
public interface MicrosoftProperties {

    boolean enabled();

    String tenantId();

    String clientId();

    String clientSecret();

    String userEmail();

    String calendarId();
}