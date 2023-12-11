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
public class FiltroCespite implements Serializable {
    private String tipoCespite;
    private String descrizione;
    private String data;

    public FiltroCespite(String tipoCespite, String descrizione) {
        this.tipoCespite = tipoCespite;
        this.descrizione = descrizione;
    }
}
