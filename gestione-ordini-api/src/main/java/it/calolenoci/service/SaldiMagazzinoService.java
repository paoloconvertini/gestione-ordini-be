package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.CaricoMagazzinoDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.SaldiMagazzinoDTO;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.MagazzinoMapper;
import it.calolenoci.mapper.SaldiMagazzinoMapper;
import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.sql.Date;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SaldiMagazzinoService {

    @Transactional
    public void findCaricoMagazzino() {
        List<SaldiMagazzinoDTO> list = GoTmpScarico.find("SELECT t, s, f " +
                "FROM  GoTmpScarico t " +
                "JOIN SaldiMagazzino s ON t.id.mmagazzino = s.mmagazzino AND t.id.marticolo = s.marticolo " +
                "JOIN FattureDettaglio f ON t.id.idBolla = f.progrGenerale " +
                "WHERE t.attivo = 'T' ").project(SaldiMagazzinoDTO.class).list();
        if(!list.isEmpty()){
            List<SaldiMagazzino> saldiMagazzinoList = new ArrayList<>();
            List<GoTmpScarico> goTmpScaricoList = new ArrayList<>();
            list.forEach(s -> {
                FattureDettaglio bolla = s.getFattureDettaglio();
                SaldiMagazzino saldiMagazzino = s.getSaldiMagazzino();
                double qtaScarico = (saldiMagazzino.getQscarichi()==null?0: saldiMagazzino.getQscarichi()) + (bolla.getQuantita()==null?0:bolla.getQuantita());
                qtaScarico = Math.round(qtaScarico * 100.0) / 100.0;
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

    @Inject
    MagazzinoMapper magazzinoMapper;

    @Inject
    SaldiMagazzinoMapper saldiMagazzinoMapper;

    @Transactional
    public String save(CaricoMagazzinoDto carico, String user) throws Exception {
        String result = null;
        try {
            List<OrdineDettaglio> articoli = carico.getArticoli();
            Integer progressivo = Magazzino.find("select ISNULL(MAX(m.magazzinoId.progressivo)+1, 1) from Magazzino m WHERE m.magazzinoId.anno=:anno and m.magazzinoId.serie = 'CF'",
                    Parameters.with("anno", Year.now().getValue())).project(Integer.class).firstResult();
            Integer progressivoGen = Magazzino.find("select MAX(m.progrgenerale) from Magazzino m").project(Integer.class).firstResult();
            List<Magazzino> magazzinoList = new ArrayList<>();
            List<SaldiMagazzino> saldiMagazzinoList = new ArrayList<>();
            for (int i = 0; i < articoli.size(); i++) {
                OrdineDettaglio articolo = articoli.get(i);
                Optional<FornitoreArticolo> fornitoreOptional = FornitoreArticolo.find(
                                "fornitoreArticoloId.articolo = :articolo",
                                Parameters.with("articolo", articolo.getFArticolo()))
                        .firstResultOptional();
                if(fornitoreOptional.isEmpty()) {
                    Log.error("Errore! Articolo non trovato con id = " + articolo.getFArticolo());
                    continue;
                }
                FornitoreArticoloId fornitoreId = fornitoreOptional.get().getFornitoreArticoloId();
                String contoFornitore = StringUtils.leftPad(fornitoreId.getConto(), 6, '0');
                MagazzinoId id = new MagazzinoId(Year.now().getValue(), "CF", progressivo, " ", i+1);
                Magazzino magazzino = magazzinoMapper.buildMagazzino(id, ++progressivoGen, articolo, fornitoreId.getGruppo(),
                        contoFornitore, carico.getNumDoc(), carico.getCausale(),
                        carico.getVettore(), carico.getDataOperazione(), user, carico.getDataDocumento());
                magazzinoList.add(magazzino);
                Optional<SaldiMagazzino> optional = SaldiMagazzino.find("marticolo =:art and  mmagazzino = :mag",
                        Parameters.with("art", articolo.getFArticolo()).and("mag", articolo.getMagazz())).firstResultOptional();

                if (optional.isPresent()) {
                    Log.debug("*** CREA CARICO: " + articolo.getFArticolo() + ". Qta: " + articolo.getQuantita());
                    SaldiMagazzino saldiMagazzino = optional.get();
                    double qtaCarico = (saldiMagazzino.getQcarichi()==null?0: saldiMagazzino.getQcarichi()) + (articolo.getQuantita()==null?0:articolo.getQuantita());
                    qtaCarico = Math.round(qtaCarico * 100.0) / 100.0;
                    Double qtaGiacenza = qtaCarico - (saldiMagazzino.getQscarichi()==null?0: saldiMagazzino.getQscarichi());
                    saldiMagazzino.setQcarichi(qtaCarico);
                    saldiMagazzino.setQgiacenza(qtaGiacenza);
                } else {
                    saldiMagazzinoList.add(saldiMagazzinoMapper.buildSaldiMagazzino(articolo, user));
                }
            }
            Magazzino.persist(magazzinoList);
            SaldiMagazzino.persist(saldiMagazzinoList);
            if(!magazzinoList.isEmpty()){
                result = StringUtils.join("Creato carico di magazzino n. ", magazzinoList.get(0).getMagazzinoId().getAnno(),
                        "/", magazzinoList.get(0).getMagazzinoId().getSerie(), "/", magazzinoList.get(0).getMagazzinoId().getProgressivo());
            }
        } catch (Exception e) {
            Log.error("Errore carico magazzino: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
        return result;
    }


}
