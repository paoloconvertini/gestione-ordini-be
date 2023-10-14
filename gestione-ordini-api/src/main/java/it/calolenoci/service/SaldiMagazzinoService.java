package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.enums.AzioneEnum;
import it.calolenoci.enums.StatoOrdineEnum;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.GoOrdineDettaglioMapper;
import it.calolenoci.mapper.RegistroAzioniMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class SaldiMagazzinoService {

    @Transactional
    public void findCaricoMagazzino() {
        List<SaldiMagazzinoDTO> list = GoTmpScarico.find("SELECT t, s, f " +
                "FROM  GoTmpScarico t " +
                "JOIN SaldiMagazzino s ON t.id.mmagazzino = s.mmagazzino AND t.id.marticolo = s.marticolo " +
                "JOIN FattureDettaglio f ON t.idBolla = f.progrGenerale " +
                "WHERE t.attivo = 'T' ").project(SaldiMagazzinoDTO.class).list();
        if(!list.isEmpty()){
            List<SaldiMagazzino> saldiMagazzinoList = new ArrayList<>();
            List<GoTmpScarico> goTmpScaricoList = new ArrayList<>();
            list.forEach(s -> {
                FattureDettaglio bolla = s.getFattureDettaglio();
                SaldiMagazzino saldiMagazzino = s.getSaldiMagazzino();
                double qtaScarico = (saldiMagazzino.getQscarichi()==null?0: saldiMagazzino.getQscarichi()) + (bolla.getQuantita()==null?0:bolla.getQuantita());
                Double qtaGiacenza = (saldiMagazzino.getQcarichi()==null?0: saldiMagazzino.getQcarichi()) - qtaScarico;
                saldiMagazzino.setQscarichi(qtaScarico);
                saldiMagazzino.setQgiacenza(qtaGiacenza);
                saldiMagazzinoList.add(saldiMagazzino);
                s.getGoTmpScarico().setAttivo(Boolean.FALSE);
                goTmpScaricoList.add(s.getGoTmpScarico());
            });
            if(!saldiMagazzinoList.isEmpty()){
                SaldiMagazzino.persist(saldiMagazzinoList);
            }
            if(!goTmpScaricoList.isEmpty()){
                GoTmpScarico.persist(goTmpScaricoList);
            }
        }

    }
}
