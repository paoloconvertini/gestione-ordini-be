package it.calolenoci.mapper;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.RegistroAzioniDto;
import it.calolenoci.entity.OrdineDettaglio;
import it.calolenoci.entity.RegistroAzioni;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;


@ApplicationScoped
public class RegistroAzioniMapper {

    public RegistroAzioni fromDtoToEntity(Integer anno, String serie, Integer progressivo, String username, String azione, Integer rigo, String tono, Float quantita) {
        RegistroAzioni r = new RegistroAzioni();
        r.setAnno(anno);
        r.setProgressivo(progressivo);
        if (rigo != null) {
            r.setRigo(rigo);
        }
        r.setSerie(serie);
        r.setData(new Date());
        r.setUser(username);
        r.setAzione(azione);
        if (quantita != null) {
            r.setQuantita(quantita);
        }
        if (StringUtils.isNotBlank(tono)) {
            r.setTono(tono);
        }
        return r;
    }

    public RegistroAzioniDto fromEntityToDto(RegistroAzioni r) {
        RegistroAzioniDto dto = new RegistroAzioniDto();
        dto.setAnno(r.getAnno());
        dto.setProgressivo(r.getProgressivo());
        dto.setRigo(r.getRigo());
        dto.setSerie(r.getSerie());
        dto.setData(r.getData());
        dto.setUser(r.getUser());
        dto.setAzione(r.getAzione());
        dto.setTono(r.getTono());
        dto.setQuantita(r.getQuantita());
        return dto;
    }
}
