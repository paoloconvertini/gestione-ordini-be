package it.calolenoci.service;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import io.quarkus.logging.Log;
import it.calolenoci.dto.DbxDto;
import it.calolenoci.dto.DbxMultipartBody;
import it.calolenoci.dto.DbxSearchDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.handler.DBXHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Singleton
public class DropBoxService {

    @Inject
    DBXHandler dbxHandler;

    private static final String URL = "/Applicazioni/GO_doc_ordini_cliente/schede tecniche";

    public DbxDto cercaSchedeTecniche(List<OrdineDettaglioDto> list) {
        try {
            DbxDto dbxDto = new DbxDto();
            DbxClientV2 client = dbxHandler.getClient();
            if (client == null) {
                throw new DbxException("Errore creazione dbx client");
            }
            for (OrdineDettaglioDto dto : list) {
                if (StringUtils.isBlank(dto.getCodArtFornitore())) {
                    continue;
                }
                SearchV2Builder searchBuilder = client.files().searchV2Builder(dto.getCodArtFornitore());
                SearchOptions searchOptions = SearchOptions.newBuilder()
                        .withPath("/schede tecniche")
                        .withFilenameOnly(Boolean.TRUE)
                        .build();
                try {
                    SearchV2Result searchResult = searchBuilder.withOptions(searchOptions).start();
                    List<SearchMatchV2> matches = searchResult.getMatches();
                    if (!matches.isEmpty()) {
                        for (SearchMatchV2 match : matches) {
                            String path = match.getMetadata().getMetadataValue().getPathDisplay();
                            String fileName = match.getMetadata().getMetadataValue().getName();
                            dbxDto.getSearchDTOS().add(new DbxSearchDTO(fileName, path));
                        }
                    } else {
                        dbxDto.getCodArtFornList().add(dto.getCodArtFornitore());
                    }
                } catch (DbxException e) {
                    Log.error("Errore dbx per la query: " + dto.getCodArtFornitore(), e);
                }
            }
            return dbxDto;
        } catch (Exception e) {
            Log.error("errore search documenti", e);
        }
        return null;
    }

    public List<String> cercaCartelleSchedeTecniche(String folder) {
        try {
            List<String> returnList = new ArrayList<>();
            DbxClientV2 client = dbxHandler.getClient();
            if (client == null) {
                throw new DbxException("Errore creazione dbx client");
            }
            try {
                String path = "/schede tecniche";
                ListFolderResult listFolderResult = client.files().listFolder(path);
                if (listFolderResult != null && !listFolderResult.getEntries().isEmpty()) {
                    if (StringUtils.isNotBlank(folder)) {
                       return listFolderResult.getEntries().stream().map(Metadata::getName).filter(name ->StringUtils.equals(folder, name)).collect(Collectors.toList());
                    }
                    for (Metadata match : listFolderResult.getEntries()) {
                        returnList.add(match.getName());
                    }
                }
            } catch (DbxException e) {
                Log.error("Errore dbx get cartella: ", e);
                return null;
            }
            return returnList;
        } catch (Exception e) {
            Log.error("errore search folders", e);
        }
        return null;
    }

    public File scaricaSchedeTecniche(List<DbxSearchDTO> list) {
        try {
            List<File> fileList = new ArrayList<>();
            // Create Dropbox client
            DbxClientV2 client = dbxHandler.getClient();
            if (client == null) {
                throw new DbxException("Errore creazione dbx client");
            }
            for (DbxSearchDTO dto : list) {
                fileList.add(downloadFromDropbox(dto.getFileName(), dto.getPath(), client));
            }

            if (!fileList.isEmpty()) {
                File file = zipFile(fileList);
                fileList.forEach(FileUtils::deleteQuietly);
                return file;
            }
        } catch (Exception e) {
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

    public boolean uploadSchedaTecnica(DbxMultipartBody dbx) {
        try {
            // Create Dropbox client
            DbxClientV2 client = dbxHandler.getClient();
            if (client == null) {
                Log.error("Errore creazione dbx client");
                return false;
            }
            List<String> folderList = this.cercaCartelleSchedeTecniche(dbx.folder);
            if(folderList == null){
                return false;
            }
            if (folderList.isEmpty()) {
                client.files().createFolderV2("/schede tecniche/" + dbx.folder);
            }
            String path = "/schede tecniche/" + dbx.folder + "/" + dbx.filename;
            client.files().uploadBuilder(path).uploadAndFinish(dbx.file);
        } catch (Exception e) {
            Log.error("errore upload scheda tecnica", e);
            return false;
        }
        return true;
    }
}
