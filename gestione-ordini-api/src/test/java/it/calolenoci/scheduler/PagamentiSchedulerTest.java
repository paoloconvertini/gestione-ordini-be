package it.calolenoci.scheduler;

import it.calolenoci.entity.OrdineId;
import it.calolenoci.service.ModalitaPagamentoService;
import it.calolenoci.service.OrdineService;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class PagamentiSchedulerTest {
    private FetchScheduler scheduler(List<OrdineId> nuovi) throws Exception {
        FetchScheduler s=new FetchScheduler();
        s.ordineService=mock(OrdineService.class);
        s.modalitaPagamentoService=mock(ModalitaPagamentoService.class);
        when(s.ordineService.addNuoviOrdini()).thenReturn(nuovi);
        return s;
    }

    @Test void senzaNuoviOrdiniNonParteNessunControlloPagamento() throws Exception {
        var s=scheduler(List.of());
        s.findNuoviOrdini();
        verifyNoInteractions(s.modalitaPagamentoService);
    }

    @Test void acquisizioneControllaSoloLeChiaviRestituiteENonRilanciaIlPregresso() throws Exception {
        var s=scheduler(List.of(new OrdineId(2026,"14",123),new OrdineId(2026,"11",456)));
        s.findNuoviOrdini();
        verify(s.modalitaPagamentoService).riconciliaOrdine(2026,"14",123);
        verify(s.modalitaPagamentoService).riconciliaOrdine(2026,"11",456);
        verifyNoMoreInteractions(s.modalitaPagamentoService);
    }
}
