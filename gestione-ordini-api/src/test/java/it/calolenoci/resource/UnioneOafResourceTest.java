package it.calolenoci.resource;

import it.calolenoci.service.OrdineFornitoreService;
import it.calolenoci.exception.BusinessException;
import it.calolenoci.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnioneOafResourceTest {
    @Test void erroreTecnicoRestituisce500ENonSuccesso() {
        var r = new OrdineFornitoreResource(); r.service = mock(OrdineFornitoreService.class);
        doThrow(new IllegalStateException("Errore test")).when(r.service).unisciOrdini(List.of(), false, null);
        var response = r.unisciOrdini(List.of(), false, null);
        assertEquals(500, response.getStatus()); assertTrue(((ResponseDto) response.getEntity()).getError());
    }
    @Test void erroreValidazioneRestituisce400ConMessaggio() {
        var r = new OrdineFornitoreResource(); r.service = mock(OrdineFornitoreService.class);
        doThrow(new BusinessException("Fornitori diversi")).when(r.service).unisciOrdini(List.of(), false, null);
        var response = r.unisciOrdini(List.of(), false, null);
        assertEquals(400, response.getStatus()); assertEquals("Fornitori diversi", ((ResponseDto) response.getEntity()).getMsg());
    }
}
