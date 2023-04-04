package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link it.calolenoci.entity.OrdineFornitoreDettaglio} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdineFornitoreDettaglioDto implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private Integer progrGenerale;
    private Integer rigo;
    private Integer pid;
    private String nota;
    private String oArticolo;
    private String oDescrArticolo;
    private Float oQuantita;
    private Float oPrezzo;
    private String oUnitaMisura;
    private Integer oColli;
    private String oCodiceIva;

    public OrdineFornitoreDettaglioDto(Integer anno, String serie, Integer progressivo, Integer rigo, String nota, String oArticolo, String oDescrArticolo, Float oQuantita, Float oPrezzo, String oUnitaMisura) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.rigo = rigo;
        this.nota = nota;
        this.oArticolo = oArticolo;
        this.oDescrArticolo = oDescrArticolo;
        this.oQuantita = oQuantita;
        this.oPrezzo = oPrezzo;
        this.oUnitaMisura = oUnitaMisura;
    }
}