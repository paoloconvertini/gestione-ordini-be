package it.calolenoci.service;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.*;
import it.calolenoci.entity.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ConsegnaLiberaServiceTest {
    @Inject ConsegnaLiberaService service;
    @Inject OrdineService ordini;
    @Inject GiroConsegneService giro;
    @Inject EntityManager em;

    @Test @TestTransaction
    void creaIntestazioneLiberaSenzaOrdineOAnagrafica() {
        ConsegnaLiberaDto request = richiesta(veicolo());
        request.setIntestazione("  Destinatario libero  ");
        OrdineDTO dto = service.salva(null, request);
        em.flush(); em.clear();
        assertNotNull(dto.getIdConsegnaLibera());
        assertNull(dto.getAnno()); assertNull(dto.getSottoConto());
        assertEquals("Destinatario libero", dto.getIntestazione());
        assertEquals(1L, dto.getOrdine());
        assertEquals(request.getIndirizzo(), dto.getIndirizzo());
        assertEquals(1, service.cerca(request.getDataConsegna(), request.getDataConsegna(), request.getVeicolo()).size());
    }

    @Test @TestTransaction
    void modificaConservaPosizioneESpostamentoRiordinaIlGiroPrecedente() {
        ConsegnaLiberaDto r = richiesta(veicolo());
        OrdineDTO prima = service.salva(null, r);
        OrdineDTO seconda = service.salva(null, r);
        r.setIntestazione("Altro destinatario");
        assertEquals(1L, service.salva(prima.getIdConsegnaLibera(), r).getOrdine());
        r.setDataConsegna(r.getDataConsegna().plusDays(1));
        OrdineDTO spostata = service.salva(prima.getIdConsegnaLibera(), r);
        em.flush(); em.clear();
        assertEquals(1L, spostata.getOrdine());
        assertEquals(1L, ((ConsegnaLibera) ConsegnaLibera.findById(seconda.getIdConsegnaLibera())).getOrdine());
    }

    @Test @TestTransaction
    void inserimentoEdEliminazioneRiordinanoUnGiroMisto() {
        int v = veicolo();
        ConsegnaLiberaDto r = richiesta(v);
        GoOrdVeicolo ordine = ordine(v, r.getDataConsegna(), 1L);
        OrdineDTO libera = service.salva(null, r);
        assertEquals(2L, libera.getOrdine());
        ProgrammaConsegnaDto p = new ProgrammaConsegnaDto();
        p.setAnno(1902); p.setSerie("TST"); p.setProgressivo(-Math.abs(UUID.randomUUID().hashCode()));
        p.setVeicolo(v); p.setDataConsegna(r.getDataConsegna()); p.setOraConsegna('M'); p.setOrdine(1L);
        assertTrue(ordini.programmaConsegna(p, ""));
        em.flush();
        assertEquals(3L, ((ConsegnaLibera) ConsegnaLibera.findById(libera.getIdConsegnaLibera())).getOrdine());
        ordini.eliminaProgrammazione(p.getAnno(), p.getSerie(), p.getProgressivo()); em.flush();
        assertEquals(2L, ((ConsegnaLibera) ConsegnaLibera.findById(libera.getIdConsegnaLibera())).getOrdine());
        service.elimina(libera.getIdConsegnaLibera()); em.flush();
        assertNull(ConsegnaLibera.findById(libera.getIdConsegnaLibera()));
        assertEquals(1L, ordine.getOrdine());
    }

    @Test @TestTransaction
    void dragDropRiordinaOrdiniEConsegneLibere() {
        int v = veicolo(); ConsegnaLiberaDto r = richiesta(v);
        GoOrdVeicolo ordine = ordine(v, r.getDataConsegna(), 1L);
        OrdineDTO libera = service.salva(null, r);
        RiordinoDto primo = new RiordinoDto();
        primo.setIdConsegnaLibera(libera.getIdConsegnaLibera()); primo.setOrdine(1L);
        RiordinoDto secondo = new RiordinoDto();
        secondo.setAnno(ordine.getId().getAnno()); secondo.setSerie(ordine.getId().getSerie());
        secondo.setProgressivo(ordine.getId().getProgressivo()); secondo.setOrdine(2L);
        giro.riordinaConsegne(List.of(primo, secondo)); em.flush();
        assertEquals(1L, ((ConsegnaLibera) ConsegnaLibera.findById(libera.getIdConsegnaLibera())).getOrdine());
        assertEquals(2L, ordine.getOrdine());
    }

    @Test @TestTransaction
    void plannerIncludeConsegneLibereERispettaIlFiltroMezzo() throws Exception {
        int v = veicolo(); ConsegnaLiberaDto r = richiesta(v);
        r.setDataConsegna(LocalDate.now().with(DayOfWeek.MONDAY));
        OrdineDTO libera = service.salva(null, r);
        FiltroOrdini f = new FiltroOrdini(); f.setVeicolo(v);
        ConsegneSettimanaliDto result = ordini.consegneSettimanali(f);
        assertEquals(1, result.getGiorni().getFirst().getNumeroConsegne());
        assertEquals(libera.getIdConsegnaLibera(), result.getGiorni().getFirst().getFasce().getFirst()
                .getVeicoli().getFirst().getConsegne().getFirst().getIdConsegnaLibera());
        f.setVeicolo(veicolo());
        assertTrue(ordini.consegneSettimanali(f).getGiorni().stream().allMatch(g -> g.getNumeroConsegne() == 0));
    }

    @Test @TestTransaction
    void validaCampiMezzoCoordinateEId() {
        ConsegnaLiberaDto r = richiesta(veicolo());
        assertThrows(BadRequestException.class, () -> service.salva(null, new ConsegnaLiberaDto()));
        r.setOraConsegna('X'); assertThrows(BadRequestException.class, () -> service.salva(null, r));
        r.setOraConsegna('M'); r.setLatitudine(95d); r.setLongitudine(15d);
        assertThrows(BadRequestException.class, () -> service.salva(null, r));
        r.setLatitudine(null); r.setLongitudine(null);
        assertThrows(NotFoundException.class, () -> service.salva(Long.MIN_VALUE, r));
        assertThrows(NotFoundException.class, () -> service.elimina(Long.MIN_VALUE));
        r.setVeicolo(Integer.MIN_VALUE); assertThrows(BadRequestException.class, () -> service.salva(null, r));
    }

    private int veicolo() {
        int id = -Math.abs(UUID.randomUUID().hashCode());
        em.createNativeQuery("INSERT INTO GO_VEICOLO (id, descrizione) VALUES (:id, :descrizione)")
                .setParameter("id", id).setParameter("descrizione", "TEST " + UUID.randomUUID()).executeUpdate();
        return id;
    }
    private ConsegnaLiberaDto richiesta(int veicolo) {
        ConsegnaLiberaDto r = new ConsegnaLiberaDto();
        r.setIntestazione("Destinatario test"); r.setIndirizzo("Via di prova 1"); r.setLocalita("Martina Franca");
        r.setVeicolo(veicolo); r.setDataConsegna(LocalDate.of(1902, 3, 3)); r.setOraConsegna('M'); return r;
    }
    private GoOrdVeicolo ordine(int veicolo, LocalDate data, Long posizione) {
        GoOrdVeicolo v = new GoOrdVeicolo();
        v.setId(new GoOrdVeicoloPK(1902, "TST", -Math.abs(UUID.randomUUID().hashCode())));
        v.setIdVeicolo(veicolo); v.setDataConsegna(data); v.setOraConsegna('M');
        v.setVenditore(false); v.setOrdine(posizione); v.persist(); return v;
    }
}
