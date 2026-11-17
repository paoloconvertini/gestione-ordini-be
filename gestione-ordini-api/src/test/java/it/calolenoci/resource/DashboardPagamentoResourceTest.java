package it.calolenoci.resource;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.entity.GoPagamentoOrdine;
import it.calolenoci.enums.Ruolo;
import it.calolenoci.service.OrdineService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class DashboardPagamentoResourceTest {
    @Inject EntityManager em;
    @Test @TestTransaction void soloAdminRiceveContatoriEContaLeAnomaliePerOrdine() throws Exception {
        DashboardResource r=new DashboardResource();
        r.em=em; r.service=mock(OrdineService.class); r.jwt=mock(JsonWebToken.class);
        when(r.jwt.getGroups()).thenReturn(Set.of(Ruolo.ADMIN));
        when(r.jwt.getClaim("permissions")).thenReturn(Json.createArrayBuilder().build());
        var prima=r.get();
        aggiungi(-1_999_999_991,"DA_FATTURARE",1);
        aggiungi(-1_999_999_990,"DA_FATTURARE",1);
        aggiungi(-1_999_999_992,"ANOMALIA",2);
        aggiungi(-1_999_999_993,"ANOMALIA",2);
        em.flush();
        var dopo=r.get();
        assertEquals(prima.getFattureDaEmettere()+1,dopo.getFattureDaEmettere());
        assertEquals(prima.getAnomaliePagamento()+1,dopo.getAnomaliePagamento());
        assertNotNull(dopo.getControlloPagamenti());
        when(r.jwt.getGroups()).thenReturn(Set.of(Ruolo.VENDITORE));
        assertNull(r.get().getFattureDaEmettere());
        assertNull(r.get().getAnomaliePagamento());
        verifyNoInteractions(r.service);
    }
    private void aggiungi(int id,String stato,int ordine) {
        GoPagamentoOrdine p=new GoPagamentoOrdine(); p.setProgrGenerale(id); p.setAnno(2039);
        p.setSerie("MP1"); p.setProgressivo(ordine); p.setRigo(-id); p.setArticolo("*ACCONTO");
        p.setStato(stato); p.setControllatoIl(LocalDateTime.now()); p.persist();
    }
}
