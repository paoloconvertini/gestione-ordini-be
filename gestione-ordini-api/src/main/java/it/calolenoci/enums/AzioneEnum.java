package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AzioneEnum {

    FIRMA("FIRMA"),
    RISERVATO("RISERVATO"),
    NON_DISPONIBILE("NON_DISPONIBILE"),

    CONSEGNATO("CONSEGNATO"),
    QUANTITA("QUANTITA"),
    TONO("TONO"),
    ORDINATO("ORDINATO");

    private final String desczrizione;
}
