package it.calolenoci.resource;

import it.calolenoci.dto.FatturaAccontoDto;
import it.calolenoci.dto.ResponseDto;
import it.calolenoci.service.ModalitaPagamentoService;
import it.calolenoci.service.OrdineService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FatturaAccontoAggiornamentoTest {
    private OrdineResource resource() {
        var r=new OrdineResource();
        r.user="test";
        r.ordineService=mock(OrdineService.class);
        r.modalitaPagamentoService=mock(ModalitaPagamentoService.class);
        return r;
    }
    private FatturaAccontoDto dto(int numero) {
        var d=new FatturaAccontoDto(); d.setAnno(2026); d.setSerie("11"); d.setProgressivo(numero); return d;
    }
    @Test void dopoCreazioneAggiornaSoloGliOrdiniFatturatiUnaVoltaPerOrdine() {
        var r=resource(); var dati=List.of(dto(1),dto(1),dto(2));
        when(r.ordineService.creaFatturaAcconto(dati,"test")).thenReturn("Creata fattura acconto n. 2026/A/42");
        var risposta=r.creaFatturaAcconto(dati);
        assertEquals(200,risposta.getStatus());
        assertFalse(((ResponseDto)risposta.getEntity()).getError());
        var sequenza=inOrder(r.ordineService,r.modalitaPagamentoService);
        sequenza.verify(r.ordineService).creaFatturaAcconto(dati,"test");
        sequenza.verify(r.modalitaPagamentoService).riconciliaOrdine(2026,"11",1);
        sequenza.verify(r.modalitaPagamentoService).riconciliaOrdine(2026,"11",2);
        verifyNoMoreInteractions(r.modalitaPagamentoService);
    }
    @Test void erroreCreazioneNonRiconciliaENonRispondeSuccesso() {
        var r=resource(); var dati=List.of(dto(1));
        when(r.ordineService.creaFatturaAcconto(dati,"test")).thenReturn("Errore nella creazione della fattura di acconto");
        var risposta=r.creaFatturaAcconto(dati);
        assertEquals(500,risposta.getStatus());
        assertTrue(((ResponseDto)risposta.getEntity()).getError());
        verifyNoInteractions(r.modalitaPagamentoService);
    }
    @Test void erroreSegnalazioniNonRipeteLaFatturaENonNascondeIlSalvataggioRiuscito() {
        var r=resource(); var dati=List.of(dto(1),dto(2));
        when(r.ordineService.creaFatturaAcconto(dati,"test")).thenReturn("Creata fattura acconto n. 2026/A/42");
        doThrow(new IllegalStateException("errore controllo")).when(r.modalitaPagamentoService).riconciliaOrdine(2026,"11",1);
        var risposta=r.creaFatturaAcconto(dati);
        var esito=(ResponseDto)risposta.getEntity();
        assertEquals(200,risposta.getStatus());
        assertFalse(esito.getError());
        assertTrue(esito.getMsg().contains("La fattura è stata salvata"));
        verify(r.ordineService,times(1)).creaFatturaAcconto(dati,"test");
        verify(r.modalitaPagamentoService).riconciliaOrdine(2026,"11",2);
    }
}
