package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FiscaleRiepilogoEnum {

    INIZIO_ESERCIZIO("Inizio esercizio"),
    ACQUISTO("Acquisti"),
    VENDITE("Vendite"),
    AMMORTAMENTI_DEDUCIBILI("Ammortamenti deducibili"),
    AMMORTAMENTI_NON_DEDUCIBILI("Ammortamenti non deducibili"),
    SUPER_AMM1("40% L.208/2015"),
    SUPER_AMM2("40% L.232/2015"),
    SUPER_AMM3("30% L.205/2017"),
    SUPER_AMM4("30% L.58/2019"),
    FINE_ESERCIZIO("Fine esercizio");

    private final String descrizione;
}
