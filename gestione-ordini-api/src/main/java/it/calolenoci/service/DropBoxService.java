package it.calolenoci.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import io.quarkus.logging.Log;
import it.calolenoci.entity.GoOrdine;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Singleton
public class DropBoxService {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private final static String URL = "https://content.dropboxapi.com/2/files/download";

    @ConfigProperty(name = "dropbox.key")
    String key;

    public void download(List<GoOrdine> list) {
       try {
           Log.info(key);
           HttpRequest request = HttpRequest.newBuilder()
                   //.POST(HttpRequest.BodyPublishers.ofString("{\"path\":\"/schede tecniche/Paffoni_007008/Braccio a soffitto ZSOF064-soffioni-showerheads-2023-05-25.pdf\"}"))
                   .POST(HttpRequest.BodyPublishers.noBody())
                   .uri(URI.create(URL))
                   .header("Accept", "application/json")
                   .header("Authorization", "Bearer " + key)
                   .header("Dropbox-API-Arg", "{\"path\":\"/Home/Applicazioni/GO_doc_ordini_cliente/schede tecniche/Paffoni_007008/Braccio a soffitto ZSOF064-soffioni-showerheads-2023-05-25.pdf\"}")
                   .build();
           HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
           if (StringUtils.isNotBlank(response.body())) {
               Log.info(response.body());
           }
       } catch (Exception e){
           Log.error(e.getMessage());
       }
    }

    public void list() throws DbxException {
        // Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("GO_doc_ordini_cliente/1.0").build();
        DbxClientV2 client = new DbxClientV2(config, key);
        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());
    }
}
