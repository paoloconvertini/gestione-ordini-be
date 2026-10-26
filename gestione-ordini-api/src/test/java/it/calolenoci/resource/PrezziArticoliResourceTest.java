package it.calolenoci.resource;

import it.calolenoci.dto.AggiornamentoPrezziDto;
import it.calolenoci.dto.PrezziArticoliDto;
import it.calolenoci.dto.SogliaRicaricoDto;
import java.math.BigDecimal;
import it.calolenoci.service.PrezziArticoliService;
import it.calolenoci.service.ArticoloClasseFornitoreService;
import jakarta.json.Json;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PrezziArticoliResourceTest {
    private PrezziArticoliResource resource;

    @BeforeEach
    void setup() {
        resource = new PrezziArticoliResource();
        resource.jwt = mock(JsonWebToken.class);
        resource.service = mock(PrezziArticoliService.class);
        resource.user = "TEST";
        resource.classiService = mock(ArticoloClasseFornitoreService.class);
    }

    @Test
    void adminSenzaPermessoNonPuoLeggereOAggiornare() {
        when(resource.jwt.getGroups()).thenReturn(Set.of("Admin"));
        when(resource.jwt.getClaim("permissions")).thenReturn(Json.createArrayBuilder().add("ordini_clienti.view").build());
        assertEquals(403, resource.cerca("414", null, false).getStatus());
        assertEquals(403, resource.aggiorna(new AggiornamentoPrezziDto()).getStatus());
        assertEquals(403, resource.classiFornitore().getStatus());
        assertEquals(403, resource.salvaSoglia(new SogliaRicaricoDto()).getStatus());
        assertEquals(403, resource.storico("414").getStatus());
        verifyNoInteractions(resource.service, resource.classiService);
    }

    @Test
    void ruoloDiversoConPermessoPuoLeggereEAggiornare() {
        when(resource.jwt.getGroups()).thenReturn(Set.of("Venditore"));
        when(resource.jwt.getClaim("permissions")).thenReturn(Json.createArrayBuilder().add("prezzi_articoli.view").build());
        when(resource.service.cerca("414", null, false)).thenReturn(List.of());
        AggiornamentoPrezziDto richiesta = new AggiornamentoPrezziDto();
        when(resource.service.aggiorna(richiesta, "TEST")).thenReturn(3);
        when(resource.service.ricaricoImpostato("414")).thenReturn(new BigDecimal("60"));
        var risposta = resource.cerca("414", null, false);
        assertEquals(200, risposta.getStatus());
        assertEquals(new BigDecimal("60"), ((PrezziArticoliDto) risposta.getEntity()).getRicaricoImpostato());
        assertEquals(List.of(), ((PrezziArticoliDto) risposta.getEntity()).getArticoli());
        assertEquals(200, resource.aggiorna(richiesta).getStatus());
        verify(resource.service).cerca("414", null, false);
        verify(resource.service).aggiorna(richiesta, "TEST");
        when(resource.classiService.getClassi()).thenReturn(List.of());
        assertEquals(200, resource.classiFornitore().getStatus());
        verify(resource.classiService).getClassi();
        SogliaRicaricoDto soglia = new SogliaRicaricoDto();
        assertEquals(200, resource.salvaSoglia(soglia).getStatus());
        verify(resource.service).salvaSoglia(soglia, "TEST");
        when(resource.service.storico("414")).thenReturn(List.of());
        assertEquals(200, resource.storico("414").getStatus());
        verify(resource.service).storico("414");
        verify(resource.jwt, never()).getGroups();
    }

    @Test
    void claimMancanteOMalformatoNonConcedeAccesso() {
        assertEquals(403, resource.cerca("414", null, false).getStatus());
        when(resource.jwt.getClaim("permissions")).thenReturn("prezzi_articoli.view");
        assertEquals(403, resource.cerca("414", null, false).getStatus());
        assertEquals(403, resource.classiFornitore().getStatus());
        assertEquals(403, resource.salvaSoglia(new SogliaRicaricoDto()).getStatus());
        assertEquals(403, resource.storico("414").getStatus());
        verifyNoInteractions(resource.service, resource.classiService);
    }
}
