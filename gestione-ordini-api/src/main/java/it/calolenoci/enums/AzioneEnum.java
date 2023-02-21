package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AzioneEnum {

    FIRMA("FIRMA"),
    RISERVATO("RISERVATO"),
    NON_DISPONIBILE("NON_DISPONIBILE"),
    ORDINATO("ORDINATO");

    private final String desczrizione;
}
