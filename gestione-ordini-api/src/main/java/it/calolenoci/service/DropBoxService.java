package it.calolenoci.service;

import com.dropbox.core.*;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.FullAccount;
import io.quarkus.logging.Log;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.GoOrdine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;

import javax.inject.Singleton;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Singleton
public class DropBoxService {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    @ConfigProperty(name = "dropbox.auth.url")
    String url;

    @ConfigProperty(name = "dropbox.key")
    String key;

    @ConfigProperty(name = "dropbox.secret")
    String secret;

    @ConfigProperty(name = "dropbox.refresh.token")
    String refreshToken;

    public File list(List<OrdineDettaglioDto> list) throws DbxException {
        try {
            List<File> fileList = new ArrayList<>();
            // Create Dropbox client
            DbxRequestConfig config = DbxRequestConfig.newBuilder(key).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=refresh_token&refresh_token=" + refreshToken + "&client_id=" + key + "&client_secret=" + secret))
                    .uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (StringUtils.isNotBlank(response.body())) {
                Log.info(response.body());
                JSONParser parser = new JSONParser();
                JSONObject jobj = (JSONObject) parser.parse(response.body());
                String accessToken = (String)jobj.get("access_token");
                DbxCredential credential = new DbxCredential(accessToken);
                DbxClientV2 client = new DbxClientV2(config, credential);
                for (OrdineDettaglioDto dto : list) {
                    SearchV2Builder searchBuilder = client.files().searchV2Builder(dto.getCodArtFornitore());
                    SearchOptions searchOptions = SearchOptions.newBuilder()
                            .withPath("/schede tecniche")
                            .withFilenameOnly(Boolean.TRUE)
                            .build();
                    SearchV2Result searchResult = searchBuilder.withOptions(searchOptions).start();
                    List<SearchMatchV2> matches = searchResult.getMatches();
                    if(!matches.isEmpty()) {
                        for (SearchMatchV2 match : matches) {
                            String path = match.getMetadata().getMetadataValue().getPathDisplay();
                            String fileName = match.getMetadata().getMetadataValue().getName();
                            fileList.add(downloadFromDropbox(fileName, path, client));
                        }
                    }
                }
            }


            if(!fileList.isEmpty()) {
                File file = zipFile(fileList);
                fileList.forEach(FileUtils::deleteQuietly);
                return file;
            }
        } catch (Exception e ) {
            Log.error("errore search documenti", e);
        }
        return null;
    }

    private File downloadFromDropbox(String fileName, String path, DbxClientV2 client) {
        File f = new File(fileName);
        try (FileOutputStream outputStream = new FileOutputStream(f, false)) {
            DbxDownloader<FileMetadata> downloadedFile = client.files().download(path);
            InputStream inputStream = downloadedFile.getInputStream();
            inputStream.transferTo(outputStream);
        } catch (Exception e) {
            Log.error("Errore download da DPX", e);
        }
        return f;
    }

    private File zipFile(List<File> fileList) throws IOException {
        File zip = new File("schede_tecniche.zip");
        final FileOutputStream fos = new FileOutputStream(zip, false);
        try (fos; ZipOutputStream zipOut = new ZipOutputStream(fos)) {


            for (File fileToZip : fileList) {
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            return zip;
        } catch (Exception e) {
            Log.error("Error zipping file", e);
        }
        return null;
    }
}
