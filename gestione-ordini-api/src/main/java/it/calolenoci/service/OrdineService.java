package it.calolenoci.service;

import it.calolenoci.dto.OrdineClienteTestataDto;
import it.calolenoci.entity.OrdineClienteTestata;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.PianoContiId;
import it.calolenoci.mapper.OrdineTestataMapper;
import it.calolenoci.repository.OrdineTestataRepository;
import it.calolenoci.repository.PianoContiRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrdineService {

    @Inject
    OrdineTestataMapper ordineTestataMapper;

    @Autowired
    protected OrdineTestataRepository ordineTestataRepository;

    @Autowired
    protected PianoContiRepository pianoContiRepository;

    public List<OrdineClienteTestataDto> findAll() {
        List<OrdineClienteTestataDto> list = new ArrayList<>();
        Iterable<OrdineClienteTestata> testatas = ordineTestataRepository.findAll();
        testatas.forEach( o -> {
            PianoConti p;
            Optional<PianoConti> byId = pianoContiRepository.findById(new PianoContiId(o.getGruppoCliente(), o.getContoCliente()));
            if(byId.isPresent()) {
                p = byId.get();
                OrdineClienteTestataDto ordineTestataDto = ordineTestataMapper.fromEntityToDto(o, p);
                list.add(ordineTestataDto);
            }
        });
        return list;
    }
}
