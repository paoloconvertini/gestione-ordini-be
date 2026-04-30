package it.calolenoci.mapper;

import it.calolenoci.entity.Ordine;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

@ApplicationScoped
public class OrdineMapper {

    public Ordine copia(Ordine src,
                        Integer nuovoProgressivo, Integer anno) {

        Ordine dst = new Ordine();

        // ======================
        // 🔹 CHIAVI
        // ======================
        dst.setAnno(anno);
        dst.setSerie(src.getSerie());
        dst.setProgressivo(nuovoProgressivo);

        // ======================
        // 🔹 CLIENTE
        // ======================
        dst.setGruppoCliente(src.getGruppoCliente());
        dst.setContoCliente(src.getContoCliente());
        dst.setRiferimento(src.getRiferimento());

        dst.setGruppoFattura(src.getGruppoFattura());
        dst.setContoFattura(src.getContoFattura());
        dst.setTipoFattura(src.getTipoFattura());

        // ======================
        // 🔹 DATI VARI
        // ======================
        dst.setProvvisorio(src.getProvvisorio());
        dst.setBancaAppoggio(src.getBancaAppoggio());
        dst.setNsBancaIncasso(src.getNsBancaIncasso());

        dst.setNumerocolli(src.getNumerocolli());
        dst.setFcoddiffpag(src.getFcoddiffpag());
        dst.setOggetto(src.getOggetto());

        dst.setSpesebollo(src.getSpesebollo());
        dst.setIvaprimascad(src.getIvaprimascad());

        dst.setAgente(src.getAgente());
        dst.setListino(src.getListino());
        dst.setModoconsegna(src.getModoconsegna());

        // ======================
        // 🔥 RESET DATE
        // ======================
        dst.setDataOrdine(LocalDate.now());
        dst.setDataRichiesta(src.getDataRichiesta());

        dst.setNumeroConferma((anno+nuovoProgressivo)+"/O");
        dst.setDataConferma(LocalDate.now());
        dst.setDataConfermaCli(null);

        dst.setCodicePagamento(src.getCodicePagamento());

        // ======================
        // 🔹 INDIRIZZI
        // ======================
        dst.setIntestdiverse(src.getIntestdiverse());
        dst.setIndirdiverse(src.getIndirdiverse());
        dst.setLocdiverse(src.getLocdiverse());
        dst.setCapdiverse(src.getCapdiverse());
        dst.setProvdiverse(src.getProvdiverse());

        // ======================
        // 🔹 AUDIT
        // ======================
        dst.setCreateDate(LocalDateTime.now());
        dst.setUpdateDate(LocalDateTime.now());

        return dst;
    }
}
