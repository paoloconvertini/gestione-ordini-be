package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link it.calolenoci.entity.Articolo} entity
 */
@Data
@NoArgsConstructor
public class ArticoloDto implements Serializable {

    private String intestazioneCliente;
    private String intestazione;

    private String articolo;
    private String descrArticolo;
    private String descrArtSuppl;
    private String unitaMisura;
    private Float prezzoBase;
    private Float costoBase;
    private Integer gruppoConto;
    private String sottoConto;
    private String codPagamento;
    private String banca;

    private Date dataInserimento;
    private Integer progrGenerale;
    private Integer rigo;

    private Integer progrGeneraleFornitore;
    private Double quantita;

    private Integer colli;

    public ArticoloDto(String intestazione, String articolo, String descrArticolo, String descrArtSuppl, String unitaMisura, Float prezzoBase, Float costoBase, Integer gruppoConto, String sottoConto, String codPagamento, String banca, Integer progrGenerale, Integer rigo, Double quantita, Integer colli) {
        this.intestazione = intestazione;
        this.articolo = articolo;
        this.descrArticolo = descrArticolo;
        this.descrArtSuppl = descrArtSuppl;
        this.unitaMisura = unitaMisura;
        this.prezzoBase = prezzoBase;
        this.costoBase = costoBase;
        this.gruppoConto = gruppoConto;
        this.sottoConto = sottoConto;
        this.codPagamento = codPagamento;
        this.banca = banca;
        this.progrGenerale = progrGenerale;
        this.rigo = rigo;
        this.quantita = quantita;
        this.colli = colli;
    }
    public ArticoloDto(String intestazioneCliente, String intestazione, String articolo, String descrArticolo,
                       String descrArtSuppl, String unitaMisura,
                       Float prezzoBase, Float costoBase, Integer gruppoConto, String sottoConto, String codPagamento,
                       String banca, Integer progrGenerale, Integer rigo, Double quantita, Integer colli) {
        this.intestazioneCliente = intestazioneCliente;
        this.intestazione = intestazione;
        this.articolo = articolo;
        this.descrArticolo = descrArticolo;
        this.descrArtSuppl = descrArtSuppl;
        this.unitaMisura = unitaMisura;
        this.prezzoBase = prezzoBase;
        this.costoBase = costoBase;
        this.gruppoConto = gruppoConto;
        this.sottoConto = sottoConto;
        this.codPagamento = codPagamento;
        this.banca = banca;
        this.progrGenerale = progrGenerale;
        this.rigo = rigo;
        this.quantita = quantita;
        this.colli = colli;
    }
}