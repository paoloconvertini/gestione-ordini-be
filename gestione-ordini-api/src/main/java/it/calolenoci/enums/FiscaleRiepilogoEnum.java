package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FiscaleRiepilogoEnum {

    INIZIO_ESERCIZIO("Inizio esercizio"),
    RISERVATO("RISERVATO"),
    NON_DISPONIBILE("NON_DISPONIBILE"),

    CONSEGNATO("CONSEGNATO"),
    QUANTITA("QUANTITA"),
    QTA_RISERVATA("QTA_RISERVATA"),
    QTA_PRONTO_CONSEGNA("QTA_PRONTO_CONSEGNA"),
    TONO("TONO"),
    PRONTO_CONSEGNA("PRONTO_CONSEGNA"),
    ORDINATO("ORDINATO");

    private final String desczrizione;
}
