package it.calolenoci.service;

import it.calolenoci.dto.RiordinoDto;
import it.calolenoci.entity.ConsegnaLibera;
import it.calolenoci.entity.GoOrdVeicolo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class GiroConsegneService {
    public void riordinaConsegne(List<RiordinoDto> lista) {
        for (RiordinoDto dto : lista) {
            if (dto.getOrdine() == null || dto.getOrdine() < 1) throw new BadRequestException("Posizione non valida");
            if (dto.getIdConsegnaLibera() != null) {
                ConsegnaLibera c = ConsegnaLibera.findById(dto.getIdConsegnaLibera());
                if (c == null) throw new NotFoundException("Consegna non trovata");
                c.setOrdine(dto.getOrdine());
                continue;
            }

            GoOrdVeicolo v = GoOrdVeicolo.find(
                    "id.anno=?1 and id.serie=?2 and id.progressivo=?3",
                    dto.getAnno(),
                    dto.getSerie(),
                    dto.getProgressivo()
            ).firstResult();

            if (v != null) {
                v.setOrdine(dto.getOrdine());
            }

        }
    }

    private record Posizione(Long ordine, Consumer<Long> aggiorna) {}

    private List<Posizione> posizioni(Integer veicolo, LocalDate data, Character fascia) {
        List<Posizione> result = new ArrayList<>();
        List<GoOrdVeicolo> ordini = GoOrdVeicolo.list(
                "idVeicolo = ?1 and dataConsegna = ?2 and oraConsegna = ?3", veicolo, data, fascia);
        ordini.forEach(v -> result.add(new Posizione(v.getOrdine(), v::setOrdine)));
        List<ConsegnaLibera> libere = ConsegnaLibera.list(
                "idVeicolo = ?1 and dataConsegna = ?2 and oraConsegna = ?3", veicolo, data, fascia);
        libere.forEach(v -> result.add(new Posizione(v.getOrdine(), v::setOrdine)));
        result.sort(Comparator.comparing(Posizione::ordine, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    public void riordina(Integer veicolo, LocalDate data, Character fascia) {
        long ordine = 1;
        for (Posizione p : posizioni(veicolo, data, fascia)) {
            p.aggiorna().accept(ordine++);
        }
    }

    public Long inserisci(Integer veicolo, LocalDate data, Character fascia, Long richiesta) {
        List<Posizione> posizioni = posizioni(veicolo, data, fascia);
        if (richiesta == null) {
            long ordine = 1;
            for (Posizione p : posizioni) {
                if (p.ordine() == null || p.ordine() != ordine) break;
                ordine++;
            }
            return ordine;
        }
        if (posizioni.stream().anyMatch(p -> richiesta.equals(p.ordine()))) {
            for (Posizione p : posizioni) {
                if (p.ordine() != null && p.ordine() >= richiesta) {
                    p.aggiorna().accept(p.ordine() + 1);
                }
            }
        }
        return richiesta;
    }
}
