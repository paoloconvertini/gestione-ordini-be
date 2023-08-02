package it.calolenoci.service;

import io.quarkus.logging.Log;
import it.calolenoci.dto.Coordinate;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;

import javax.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
public class GeoLocationService {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    public Coordinate getLonLat(String url) {
       try {
           HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).header("Accept", "application/json").build();
           HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
           if (StringUtils.isNotBlank(response.body())) {
               JSONParser parser = new JSONParser();
               JSONObject jobj = (JSONObject) parser.parse(response.body());
               JSONArray resourceSets = (JSONArray) jobj.get("resourceSets");
               if (resourceSets != null && !resourceSets.isEmpty()) {
                   JSONObject rsSet = (JSONObject) resourceSets.get(0);
                   JSONArray resources = (JSONArray) rsSet.get("resources");
                   if (resources != null && !resources.isEmpty()) {
                       JSONObject rs = (JSONObject) resources.get(0);
                       if (rs != null) {
                           JSONObject point = (JSONObject) rs.get("point");
                           if (point != null) {
                               JSONArray coords = (JSONArray) point.get("coordinates");
                               if (!coords.isEmpty()) {
                                   return new Coordinate((Double) coords.get(0), (Double) coords.get(1));
                               }
                           }
                       }
                   }
               }
           }
       } catch (Exception e){
           Log.error(e.getMessage());
       }
       return null;
    }
}
