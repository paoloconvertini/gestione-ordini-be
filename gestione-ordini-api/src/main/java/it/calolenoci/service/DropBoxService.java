package it.calolenoci.service;

import io.quarkus.logging.Log;
import it.calolenoci.dto.Coordinate;
import it.calolenoci.entity.GoOrdine;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;

import javax.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Singleton
public class DropBoxService {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private final static String URL = "";

    public void createFolder(List<GoOrdine> list) {
       try {
           HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(URL)).header("Accept", "application/json").build();
           HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
           if (StringUtils.isNotBlank(response.body())) {
           }
       } catch (Exception e){
           Log.error(e.getMessage());
       }
    }
}
