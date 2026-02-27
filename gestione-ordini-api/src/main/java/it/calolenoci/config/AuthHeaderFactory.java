package it.calolenoci.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@ApplicationScoped
public class AuthHeaderFactory implements ClientHeadersFactory {

    @Override
    public MultivaluedMap<String, String> update(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        if (incomingHeaders.containsKey("Authorization")) {
            clientOutgoingHeaders.put("Authorization",
                    incomingHeaders.get("Authorization"));
        }

        return clientOutgoingHeaders;
    }
}