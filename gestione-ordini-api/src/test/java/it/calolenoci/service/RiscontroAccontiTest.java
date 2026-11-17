package it.calolenoci.service;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiscontroAccontiTest {
    private RiscontroAcconti.Previsto p(int id, String soglia) {
        return new RiscontroAcconti.Previsto(id, new BigDecimal(soglia));
    }
    private RiscontroAcconti.Emesso e(String numero, String importo) {
        return new RiscontroAcconti.Emesso(numero, new BigDecimal(importo));
    }
    private List<RiscontroAcconti.Previsto> quattroTranche() {
        return List.of(p(1,"250"),p(2,"500"),p(3,"750"),p(4,"1000"));
    }
    @Test void fatturatoTrentacinquePercentoCopreSoloLaPrimaTrancheDaVenticinque() {
        var esiti=RiscontroAcconti.verifica(quattroTranche(),List.of(e("2026/A/1","350")));
        assertTrue(esiti.get(1).coperto());
        assertFalse(esiti.get(2).coperto());
        assertFalse(esiti.get(3).coperto());
        assertFalse(esiti.get(4).coperto());
    }
    @Test void cumulaDocumentiDiversiEUnaPercentualeMaggioreCoprePiuTranche() {
        var esiti=RiscontroAcconti.verifica(quattroTranche(),List.of(e("2026/A/1","350"),e("2026/A/2","250")));
        assertTrue(esiti.get(1).coperto());
        assertTrue(esiti.get(2).coperto());
        assertFalse(esiti.get(3).coperto());
        assertFalse(esiti.get(4).coperto());
    }
    @Test void centoPercentoCopreTuttoENonGeneraAmbiguitaSuPercentualiUguali() {
        var esiti=RiscontroAcconti.verifica(quattroTranche(),List.of(e("2026/A/1","1000")));
        assertTrue(esiti.values().stream().allMatch(RiscontroAcconti.Esito::coperto));
    }
    @Test void unaQuotaParzialeNonEscludeUnaTrancheNonInteramenteCoperta() {
        var esiti=RiscontroAcconti.verifica(quattroTranche(),List.of(e("2026/A/1","200")));
        assertTrue(esiti.values().stream().noneMatch(RiscontroAcconti.Esito::coperto));
    }
    @Test void sogliaEsattaArrotondataAiCentesimi() {
        assertTrue(RiscontroAcconti.verifica(List.of(p(1,"300.30")),List.of(e("2026/A/1","300.297"))).get(1).coperto());
        assertFalse(RiscontroAcconti.verifica(List.of(p(1,"300.30")),List.of(e("2026/A/1","300.29"))).get(1).coperto());
    }
}
