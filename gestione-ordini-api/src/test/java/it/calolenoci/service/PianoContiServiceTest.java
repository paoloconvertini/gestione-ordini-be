package it.calolenoci.service;

import io.quarkus.panache.common.Parameters;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.entity.PianoContiId;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PianoContiServiceTest {

    @Inject
    PianoContiService service;

    @Inject
    EntityManager entityManager;

    @Test
    void nonEsegueLaRicercaSeIlTestoEVuoto() {
        assertTrue(service.searchClienti(null).isEmpty());
        assertTrue(service.searchClienti("").isEmpty());
        assertTrue(service.searchClienti("   ").isEmpty());
    }

    @Test
    @TestTransaction
    void ricercaSoloClientiSenzaDistinguereMaiuscoleEMinuscole() {
        List<PianoConti> records = existingClienti(2);
        String marker = "ClienteTest" + UUID.randomUUID().toString().substring(0, 8);
        PianoConti cliente = records.get(0);
        cliente.setIntestazione(marker);
        cliente.setIndirizzo("Via Roma 10");
        cliente.setLocalita("Ceglie Messapica");
        cliente.setCap("72013");
        cliente.setProvincia("BR");
        cliente.setLatitudine(40.65);
        cliente.setLongitudine(17.52);
        cliente.setTelefono("0831000000");
        cliente.setCellulare("3330000000");
        cliente.setEmail("cliente@example.test");

        PianoConti nonCliente = records.get(1);
        nonCliente.setCliFor("F");
        nonCliente.setIntestazione(marker + " escluso");

        List<PianoContiDto> result = service.searchClienti(marker.toLowerCase());

        assertEquals(1, result.size());
        PianoContiDto dto = result.getFirst();
        assertEquals(cliente.getGruppoConto(), dto.getGruppoConto());
        assertEquals(cliente.getSottoConto(), dto.getSottoConto());
        assertEquals(marker, dto.getIntestazione());
        assertEquals("Via Roma 10", dto.getIndirizzo());
        assertEquals("Ceglie Messapica", dto.getLocalita());
        assertEquals("72013", dto.getCap());
        assertEquals("BR", dto.getProvincia());
        assertEquals(40.65, dto.getLatitudine());
        assertEquals(17.52, dto.getLongitudine());
        assertEquals("0831000000", dto.getTelefono());
        assertEquals("3330000000", dto.getCellulare());
        assertEquals("cliente@example.test", dto.getEmail());
    }

    @Test
    @TestTransaction
    void ordinaPerIntestazioneELimitaLaRicercaAVentiRisultati() {
        List<PianoConti> records = existingClienti(21);
        String marker = "TEST" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        for (int index = 0; index < records.size(); index++) {
            records.get(index).setIntestazione(marker + String.format("-%02d", 20 - index));
        }

        List<PianoContiDto> result = service.searchClienti(marker);

        assertEquals(20, result.size());
        assertEquals(IntStream.range(0, 20).mapToObj(index -> marker + String.format("-%02d", index)).toList(),
                result.stream().map(PianoContiDto::getIntestazione).toList());
    }

    @Test
    @TestTransaction
    void aggiornaLEmailDelClienteDelGruppo1231() {
        PianoConti cliente = PianoConti.find("gruppoConto = 1231").firstResult();
        if (cliente == null) {
            throw new AssertionError("Il database di sviluppo non contiene clienti del gruppo 1231");
        }
        EmailDto dto = new EmailDto();
        dto.setSottoConto(cliente.getSottoConto());
        dto.setTo("nuova-email@example.test");

        service.update(dto);

        entityManager.clear();
        PianoConti updated = PianoConti.findById(
                new PianoContiId(cliente.getGruppoConto(), cliente.getSottoConto()));
        assertEquals("nuova-email@example.test", updated.getEmail());
    }

    private List<PianoConti> existingClienti(int count) {
        List<PianoConti> records = PianoConti.find("cliFor = :tipo",
                Parameters.with("tipo", "C")).range(0, count - 1).list();
        if (records.size() < count) {
            throw new AssertionError("Il database di sviluppo non contiene abbastanza clienti nel piano dei conti");
        }
        return records;
    }
}
