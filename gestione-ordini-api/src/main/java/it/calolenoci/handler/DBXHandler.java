package it.calolenoci.handler;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jfree.util.Log;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;

import javax.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
public class DBXHandler {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    @ConfigProperty(name = "dropbox.auth.url")
    String url;

    @ConfigProperty(name = "dropbox.key")
    String key;

    @ConfigProperty(name = "dropbox.secret")
    String secret;

    @ConfigProperty(name = "dropbox.refresh.token")
    String refreshToken;

    public DbxClientV2 getClient(){
        try {
            DbxRequestConfig config = DbxRequestConfig.newBuilder(key).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=refresh_token&refresh_token=" + refreshToken + "&client_id=" + key + "&client_secret=" + secret))
                    .uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (StringUtils.isNotBlank(response.body())) {
                JSONParser parser = new JSONParser();
                JSONObject jobj = (JSONObject) parser.parse(response.body());
                String accessToken = (String) jobj.get("access_token");
                DbxCredential credential = new DbxCredential(accessToken);
                return new DbxClientV2(config, credential);
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.error("Error getClient", e);
        }
        return null;
    }
}
