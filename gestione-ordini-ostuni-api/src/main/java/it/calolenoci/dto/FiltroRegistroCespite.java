package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FiltroRegistroCespite implements Serializable {
    private String cespite;
    private String descrizione;
    private String data;

    public FiltroRegistroCespite(String cespite, String descrizione) {
        this.cespite = cespite;
        this.descrizione = descrizione;
    }
}
