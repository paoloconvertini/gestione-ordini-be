package it.calolenoci.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.calolenoci.config.MicrosoftProperties;
import it.calolenoci.dto.OutlookEventDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ApplicationScoped
public class OutlookCalendarService {

    private static final Logger log = Logger.getLogger(OutlookCalendarService.class);

    @Inject
    MicrosoftProperties microsoftProperties;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

    public String createEvent(OutlookEventDto dto) throws Exception {

        if (!microsoftProperties.enabled()) {
            return null;
        }

        String token = getAccessToken();
        String url = getCalendarBaseUrl() + "/events";
        String payload = buildPayload(dto);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        JsonNode json = objectMapper.readTree(response.body());
        return json.get("id").asText();
    }

    public void updateEvent(String eventId,
                            OutlookEventDto dto) throws Exception {

        if (!microsoftProperties.enabled()) {
            return;
        }

        if (StringUtils.isBlank(eventId)) {
            return;
        }

        String token = getAccessToken();

        String url =
                getCalendarBaseUrl()
                        + "/events/"
                        + eventId;

        String payload =
                buildPayload(dto);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .method("PATCH",
                                HttpRequest.BodyPublishers.ofString(payload))
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
    }

    public void deleteEvent(String eventId) throws Exception {

        if (!microsoftProperties.enabled()) {
            return;
        }

        if (StringUtils.isBlank(eventId)) {
            return;
        }

        String token = getAccessToken();

        String url =
                getCalendarBaseUrl()
                        + "/events/"
                        + eventId;

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Authorization", "Bearer " + token)
                        .DELETE()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            log.info("Evento Outlook " + eventId + " già eliminato");
            return;
        }

        if (response.statusCode() != 204
                && response.statusCode() != 200) {

            throw new RuntimeException(
                    "Errore Outlook delete: "
                            + response.statusCode()
                            + " - "
                            + response.body());
        }
    }

    private String getCalendarBaseUrl() {

        return "https://graph.microsoft.com/v1.0/users/"
                + microsoftProperties.userEmail()
                + "/calendars/"
                + microsoftProperties.calendarId();
    }

    private String buildPayload(OutlookEventDto dto) throws Exception {

        ObjectNode root = objectMapper.createObjectNode();

        root.put("subject", dto.getSubject());

        ObjectNode body = objectMapper.createObjectNode();
        body.put("contentType", "Text");
        body.put("content", dto.getDescription());

        root.set("body", body);

        ObjectNode start = objectMapper.createObjectNode();
        start.put("dateTime", dto.getStart().toString());
        start.put("timeZone", "Europe/Rome");

        root.set("start", start);

        ObjectNode end = objectMapper.createObjectNode();
        end.put("dateTime", dto.getEnd().toString());
        end.put("timeZone", "Europe/Rome");

        root.set("end", end);

        ObjectNode location = objectMapper.createObjectNode();
        location.put("displayName", dto.getLocation());

        root.set("location", location);

        return objectMapper.writeValueAsString(root);
    }

    private void validateResponse(HttpResponse<String> response) {

        if (response.statusCode() < 200
                || response.statusCode() > 299) {

            throw new RuntimeException(
                    "Errore Outlook: "
                            + response.statusCode()
                            + " - "
                            + response.body());
        }
    }

    private String getAccessToken()
            throws IOException, InterruptedException {

        String url =
                "https://login.microsoftonline.com/"
                        + microsoftProperties.tenantId()
                        + "/oauth2/v2.0/token";

        String body =
                "client_id="
                        + encode(microsoftProperties.clientId())
                        + "&scope="
                        + encode("https://graph.microsoft.com/.default")
                        + "&client_secret="
                        + encode(microsoftProperties.clientSecret())
                        + "&grant_type=client_credentials";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        JsonNode json =
                objectMapper.readTree(response.body());

        return json.get("access_token").asText();
    }

    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8);
    }

    public boolean eventExists(String eventId) throws Exception {

        if (!microsoftProperties.enabled()) {
            return false;
        }

        if (StringUtils.isBlank(eventId)) {
            return false;
        }

        String token = getAccessToken();

        String url =
                getCalendarBaseUrl()
                        + "/events/"
                        + eventId;

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return false;
        }

        validateResponse(response);

        return true;
    }
}