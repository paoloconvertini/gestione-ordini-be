package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SommaCategoria implements Serializable {

    private int idSuperAmm;
    private String tipoCespite;

    private String superAmmDescr;
    private Double somma;

    private Double fondo;

    public SommaCategoria(String tipoCespite, Double somma) {
        this.tipoCespite = tipoCespite;
        this.somma = somma;
    }

    public SommaCategoria(String tipoCespite, Double somma, Double fondo) {
        this.tipoCespite = tipoCespite;
        this.somma = somma;
        this.fondo = fondo;
    }
}
