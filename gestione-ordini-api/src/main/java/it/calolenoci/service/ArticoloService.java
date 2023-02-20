package it.calolenoci.service;

import it.calolenoci.dto.OrdineClienteDto;
import it.calolenoci.dto.OrdineClienteTestataDto;
import it.calolenoci.entity.OrdineCliente;
import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.PianoContiId;
import it.calolenoci.mapper.ArticoloMapper;
import it.calolenoci.mapper.OrdineTestataMapper;
import it.calolenoci.repository.OrdineClienteRepository;
import it.calolenoci.repository.OrdineTestataRepository;
import it.calolenoci.repository.PianoContiRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ArticoloService {

    @Autowired
    protected OrdineClienteRepository ordineClienteRepository;

    @Inject
    ArticoloMapper mapper;

    public List<OrdineClienteDto> findById(Integer anno, String serie, Integer progressivo) {
        List<OrdineClienteDto> list = new ArrayList<>();
        List<OrdineCliente> articoli = ordineClienteRepository.findByOrdineDettaglioIdAnnoAndOrdineDettaglioIdSerieAndOrdineDettaglioIdProgressivoOrderByOrdineDettaglioId(anno, serie, progressivo);
        articoli.forEach(a -> list.add(mapper.fromEntityToDto(a)));
        return list;
    }
}
