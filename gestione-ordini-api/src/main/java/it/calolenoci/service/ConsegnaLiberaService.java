package it.calolenoci.service;

import it.calolenoci.dto.ConsegnaLiberaDto;
import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.entity.ConsegnaLibera;
import it.calolenoci.entity.Veicolo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ConsegnaLiberaService {
    @Inject
    GiroConsegneService giro;

    @Transactional
    public OrdineDTO salva(Long id, ConsegnaLiberaDto dto) {
        valida(dto);
        ConsegnaLibera c = id == null ? new ConsegnaLibera() : trova(id);
        boolean stessoGiro = id != null && c.getIdVeicolo().equals(dto.getVeicolo())
                && c.getDataConsegna().equals(dto.getDataConsegna())
                && c.getOraConsegna().equals(dto.getOraConsegna());
        Integer precedenteVeicolo = c.getIdVeicolo();
        LocalDate precedenteData = c.getDataConsegna();
        Character precedenteFascia = c.getOraConsegna();
        Long posizione = stessoGiro ? c.getOrdine()
                : giro.inserisci(dto.getVeicolo(), dto.getDataConsegna(), dto.getOraConsegna(), null);
        c.setIntestazione(dto.getIntestazione().trim());
        c.setIndirizzo(dto.getIndirizzo().trim());
        c.setLocalita(dto.getLocalita().trim());
        c.setTelefono(StringUtils.trimToNull(dto.getTelefono()));
        c.setLatitudine(dto.getLatitudine());
        c.setLongitudine(dto.getLongitudine());
        c.setIdVeicolo(dto.getVeicolo());
        c.setDataConsegna(dto.getDataConsegna());
        c.setOraConsegna(dto.getOraConsegna());
        c.setOrdine(posizione);
        if (id == null) c.persist();
        if (precedenteVeicolo != null && !stessoGiro) {
            giro.riordina(precedenteVeicolo, precedenteData, precedenteFascia);
        }
        return toDto(c);
    }

    @Transactional
    public void elimina(Long id) {
        ConsegnaLibera c = trova(id);
        Integer veicolo = c.getIdVeicolo();
        LocalDate data = c.getDataConsegna();
        Character fascia = c.getOraConsegna();
        c.delete();
        giro.riordina(veicolo, data, fascia);
    }

    public List<OrdineDTO> cerca(LocalDate da, LocalDate a, Integer veicolo) {
        String query = "dataConsegna >= ?1 and dataConsegna <= ?2";
        List<ConsegnaLibera> list = veicolo == null
                ? ConsegnaLibera.list(query, da, a)
                : ConsegnaLibera.list(query + " and idVeicolo = ?3", da, a, veicolo);
        return list.stream().map(this::toDto).toList();
    }

    private ConsegnaLibera trova(Long id) {
        ConsegnaLibera c = ConsegnaLibera.findById(id);
        if (c == null) throw new NotFoundException("Consegna non trovata");
        return c;
    }

    private void valida(ConsegnaLiberaDto dto) {
        if (dto == null || StringUtils.isBlank(dto.getIntestazione()) || dto.getIntestazione().trim().length() > 255
                || StringUtils.isBlank(dto.getIndirizzo()) || dto.getIndirizzo().trim().length() > 255
                || StringUtils.isBlank(dto.getLocalita()) || dto.getLocalita().trim().length() > 255
                || (dto.getTelefono() != null && dto.getTelefono().length() > 50)
                || dto.getDataConsegna() == null || dto.getVeicolo() == null
                || (dto.getOraConsegna() == null || (dto.getOraConsegna() != 'M' && dto.getOraConsegna() != 'P'))) {
            throw new BadRequestException("Indicare intestazione, indirizzo, località, data, fascia e veicolo validi");
        }
        if (Veicolo.findById(dto.getVeicolo()) == null) throw new BadRequestException("Veicolo non valido");
        if ((dto.getLatitudine() == null) != (dto.getLongitudine() == null)
                || (dto.getLatitudine() != null && (!Double.isFinite(dto.getLatitudine())
                || !Double.isFinite(dto.getLongitudine()) || Math.abs(dto.getLatitudine()) > 90
                || Math.abs(dto.getLongitudine()) > 180))) {
            throw new BadRequestException("Coordinate non valide");
        }
    }

    private OrdineDTO toDto(ConsegnaLibera c) {
        OrdineDTO dto = new OrdineDTO();
        dto.setIdConsegnaLibera(c.getId());
        dto.setIntestazione(c.getIntestazione());
        dto.setIndirizzo(c.getIndirizzo());
        dto.setLocalita(c.getLocalita());
        dto.setTelefono(c.getTelefono());
        dto.setLatitudine(c.getLatitudine());
        dto.setLongitudine(c.getLongitudine());
        dto.setDataConsegna(c.getDataConsegna());
        dto.setOraConsegna(c.getOraConsegna());
        dto.setVeicolo(c.getIdVeicolo());
        dto.setOrdine(c.getOrdine());
        Veicolo v = Veicolo.findById(c.getIdVeicolo());
        dto.setDescVeicolo(v == null ? null : v.getDescrizione());
        return dto;
    }
}
