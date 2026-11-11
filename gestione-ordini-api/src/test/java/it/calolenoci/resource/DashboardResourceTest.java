package it.calolenoci.resource;

import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.service.OrdineService;
import jakarta.json.Json;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DashboardResourceTest {
    private DashboardResource resource(String... permissions) {
        DashboardResource resource = new DashboardResource();
        resource.service = mock(OrdineService.class);
        resource.jwt = mock(JsonWebToken.class);
        var array = Json.createArrayBuilder();
        for (String permission : permissions) array.add(permission);
        when(resource.jwt.getClaim("permissions")).thenReturn(array.build());
        return resource;
    }

    @Test
    void senzaPermessoNonLeggeGliOrdini() throws Exception {
        DashboardResource resource = resource("articoli.codifica_articoli", "ordini_clienti.pronto_consegna");
        var result = resource.get();
        assertNull(result.getOrdiniAttivi());
        assertNull(result.getDaCodificare());
        assertNull(result.getProntoConsegna());
        verifyNoInteractions(resource.service);
    }

    @Test
    void mostraSoloIContatoriConsentiti() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view", "articoli.codifica_articoli");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            return Boolean.TRUE.equals(filtro.getDaCodificare()) ? 3L : 10L;
        });
        var result = resource.get();
        assertEquals(10L, result.getOrdiniAttivi());
        assertEquals(3L, result.getDaCodificare());
        assertNull(result.getProntoConsegna());
        verify(resource.service, times(6)).countDashboard(any(FiltroOrdini.class));
    }

    @Test
    void prontoConsegnaUsaLoStessoFiltroDellaLista() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view", "ordini_clienti.pronto_consegna");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            return Boolean.TRUE.equals(filtro.getProntoConsegna()) ? 4L : 10L;
        });
        var result = resource.get();
        assertEquals(4L, result.getProntoConsegna());
        assertNull(result.getDaCodificare());
    }
    @Test
    void esponeLAlertCarichiESenzaBollaComeLaLista() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            if (Boolean.TRUE.equals(filtro.getCarichiDaProcessare())) return 7L;
            if (Boolean.TRUE.equals(filtro.getSenzaBolla())) return 2L;
            return 10L;
        });
        var result = resource.get();
        assertEquals(7L, result.getCarichiDaProcessare());
        assertEquals(2L, result.getSenzaBolla());
        assertNull(result.getArchiviatiSenzaFirma());
        verify(resource.service).carichiPiuVecchi(argThat(FiltroOrdini::getCarichiDaProcessare));
    }

    @Test
    void ilContatoreFirmeRichiedeIlPermessoEUsaGliArchiviati() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view", "ordini_clienti.warn_non_firmato");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            if (Boolean.TRUE.equals(filtro.getSenzaFirma())) {
                assertEquals("ARCHIVIATO", filtro.getFiltroStatus());
                return 6L;
            }
            return 10L;
        });
        assertEquals(6L, resource.get().getArchiviatiSenzaFirma());
    }
    @Test
    void noteDaOrdinareRichiedonoIlPermessoOrdiniERiusanoIlConteggioDedicato() throws Exception {
        DashboardResource negata = resource();
        assertThrows(jakarta.ws.rs.ForbiddenException.class, () -> negata.noteDaOrdinare("A"));
        verifyNoInteractions(negata.service);
        DashboardResource consentita = resource("ordini_clienti.view");
        when(consentita.service.countOrdiniDaOrdinareConNote()).thenReturn(3L);
        when(consentita.service.noteArticoliDaOrdinare("A")).thenReturn(java.util.List.of());
        assertEquals(3L, consentita.get().getDaOrdinareConNote());
        assertTrue(consentita.noteDaOrdinare("A").isEmpty());
        verify(consentita.service).noteArticoliDaOrdinare("A");
    }

    @Test
    void modificatiUsaIlFiltroDellaListaConStatoDaOrdinare() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            if (Boolean.TRUE.equals(filtro.getDaOrdinareModificati())) {
                assertEquals("DA_ORDINARE", filtro.getFiltroStatus());
                return 5L;
            }
            return 10L;
        });
        assertEquals(5L, resource.get().getDaOrdinareModificati());
        assertNull(resource().get().getDaOrdinareModificati());
    }

    @Test
    void daOrdinareContaLoStatoEsatto() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            return "DA_ORDINARE".equals(filtro.getFiltroStatus()) ? 8L : 10L;
        });
        assertEquals(8L, resource.get().getDaOrdinare());
    }
    @Test
    void consegneDaProgrammareRichiedeIlPermessoDiProgrammazione() throws Exception {
        DashboardResource resource = resource("ordini_clienti.view", "logistica.update_veicolo");
        when(resource.service.countDashboard(any(FiltroOrdini.class))).thenAnswer(call -> {
            FiltroOrdini filtro = call.getArgument(0);
            return Boolean.TRUE.equals(filtro.getConsegneDaProgrammare()) ? 5L : 10L;
        });
        assertEquals(5L, resource.get().getConsegneDaProgrammare());
        assertNull(resource("ordini_clienti.view").get().getConsegneDaProgrammare());
    }
}
