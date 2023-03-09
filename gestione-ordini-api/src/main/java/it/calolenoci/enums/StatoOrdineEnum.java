package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatoOrdineEnum {

    DA_PROCESSARE("DA_PROCESSARE"),

    DA_ORDINARE("DA_ORDINARE"),
    INCOMPLETO("INCOMPLETO"),
    COMPLETO("COMPLETO"),

    ARCHIVIATO("ARCHIVIATO");

    private final String descrizione;
}
