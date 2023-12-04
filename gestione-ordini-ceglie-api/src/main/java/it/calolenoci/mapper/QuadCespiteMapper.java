package it.calolenoci.mapper;

import it.calolenoci.dto.QuadraturaCespiteRequest;
import it.calolenoci.entity.QuadraturaCespite;

import javax.enterprise.context.ApplicationScoped;

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

