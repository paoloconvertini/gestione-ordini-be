package it.calolenoci.mapper;

import it.calolenoci.dto.ArticoloClasseFornitoreDto;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.entity.ArticoloClasseFornitore;
import it.calolenoci.entity.GoOrdineDettaglio;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;


@ApplicationScoped
public class ArticoloClasseFornitoreMapper {

    public void fromDtoToEntity (ArticoloClasseFornitoreDto dto, ArticoloClasseFornitore a) {
        a.setDescrUser(dto.getDescrUser());
        a.setDescrUser2(dto.getDescrUser2());
        if(StringUtils.isNotBlank(dto.getDescrUser3())){
            a.setDescrUser3(dto.getDescrUser3());
        }
    }

    public ArticoloClasseFornitoreDto fromEntityToDto (ArticoloClasseFornitore a) {
        ArticoloClasseFornitoreDto dto = new ArticoloClasseFornitoreDto();
        dto.setCodice(a.getCodice());
        dto.setDescrizione(a.getDescrizione());
        dto.setDescrUser(a.getDescrUser());
        dto.setDescrUser2(a.getDescrUser2());
        dto.setDescrUser3(a.getDescrUser3());
        return dto;
    }
}
