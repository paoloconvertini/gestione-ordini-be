package it.calolenoci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoAppuntamentoEnum {

    APPUNTAMENTO("APPUNTAMENTO"),
    FORMAZIONE("FORMAZIONE"),
    VISITA("VISITA");

    private final String descrizione;
}
