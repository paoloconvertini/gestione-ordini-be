package it.calolenoci.service;

import it.calolenoci.dto.SaldiMagazzinoDTO;
import it.calolenoci.entity.FattureDettaglio;
import it.calolenoci.entity.GoTmpScarico;
import it.calolenoci.entity.SaldiMagazzino;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

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
