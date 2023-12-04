package it.calolenoci.mapper;

import it.calolenoci.dto.QuadraturaCespiteRequest;
import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import it.calolenoci.entity.QuadraturaCespite;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class QuadCespiteMapper {
    public QuadraturaCespite fromDtoToEntity(QuadraturaCespiteRequest quad) {
        QuadraturaCespite q = new QuadraturaCespite();
        q.setIdCespite(quad.getIdCespite());
        q.setAnno(quad.getAnno());
        q.setAmmortamento(quad.getAmmortamento());
        return q;
    }


}

