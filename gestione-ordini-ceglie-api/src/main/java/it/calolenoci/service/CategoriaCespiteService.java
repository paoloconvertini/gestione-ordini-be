package it.calolenoci.service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.calolenoci.dto.FiscaleRiepilogo;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import it.calolenoci.mapper.AmmortamentoCespiteMapper;
import it.calolenoci.mapper.CategoriaCespiteMapper;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

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
