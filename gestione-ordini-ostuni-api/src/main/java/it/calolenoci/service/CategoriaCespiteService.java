package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.CategoriaCespiteResponse;
import it.calolenoci.entity.CategoriaCespite;
import it.calolenoci.mapper.CategoriaCespiteMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class CategoriaCespiteService {

    @Inject
    CategoriaCespiteMapper mapper;

    @Transactional
    public void save(CategoriaCespiteResponse categoriaCespiteResponse) {
        Optional<CategoriaCespite> optionalCategoriaCespite = CategoriaCespite.find("tipoCespite=:t",
                Parameters.with("t", categoriaCespiteResponse.getTipoCespite())).firstResultOptional();
        if(optionalCategoriaCespite.isPresent()) {
            mapper.fromDtoToEntity(optionalCategoriaCespite.get(), categoriaCespiteResponse);
            optionalCategoriaCespite.get().persist();
        } else {
            CategoriaCespite categoriaCespite = new CategoriaCespite();
            mapper.fromDtoToEntity(categoriaCespite, categoriaCespiteResponse);
            categoriaCespite.persist();
        }
    }
}
