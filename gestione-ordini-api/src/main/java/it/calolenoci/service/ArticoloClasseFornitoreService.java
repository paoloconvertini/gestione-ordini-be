package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import it.calolenoci.dto.ArticoloClasseFornitoreDto;
import it.calolenoci.entity.ArticoloClasseFornitore;
import it.calolenoci.mapper.ArticoloClasseFornitoreMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ArticoloClasseFornitoreService {

    @Inject
    ArticoloClasseFornitoreMapper mapper;

    @Transactional
    public void save(ArticoloClasseFornitoreDto dto) {
        Optional<ArticoloClasseFornitore> classeFornitoreOptional = ArticoloClasseFornitore.find("codice =:c",
                Parameters.with("c", dto.getCodice())).firstResultOptional();
        if(classeFornitoreOptional.isPresent()){
            ArticoloClasseFornitore a = classeFornitoreOptional.get();
            mapper.fromDtoToEntity(dto, a);
            a.persist();
        }
    }

    public List<ArticoloClasseFornitoreDto> getClassi() {
        List<ArticoloClasseFornitoreDto> list = new ArrayList<>();
        List<ArticoloClasseFornitore> articoli = ArticoloClasseFornitore.findAll().list();
        articoli.forEach(a -> list.add(mapper.fromEntityToDto(a)));
        return list;
    }

    public ArticoloClasseFornitoreDto getClasse(String codice) {
        ArticoloClasseFornitoreDto dto = new ArticoloClasseFornitoreDto();
        Optional<ArticoloClasseFornitore> classeFornitoreOptional = ArticoloClasseFornitore.find("codice =:c",
                Parameters.with("c", codice)).firstResultOptional();
        if(classeFornitoreOptional.isPresent()){
            dto = mapper.fromEntityToDto(classeFornitoreOptional.get());
        }
        return dto;
    }

}
