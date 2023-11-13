package it.calolenoci.dto;

import it.calolenoci.entity.Articolo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A DTO for the {@link Articolo} entity
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
    private Double prezzoBase;
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

    private Double quantitaV;

    private Integer colli;

    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String tipoRigo;
    private Double fScontoArticolo;
    private Double scontoF1;

    private Double scontoF2;

    private Double fScontoP;


    public ArticoloDto(String articolo, String descrArticolo, String descrArtSuppl, String unitaMisura) {
        this.articolo = articolo;
        this.descrArticolo = descrArticolo;
        this.descrArtSuppl = descrArtSuppl;
        this.unitaMisura = unitaMisura;
    }

    public ArticoloDto(String intestazione, String articolo, String descrArticolo, String descrArtSuppl, String unitaMisura, Double prezzoBase, Float costoBase, Integer gruppoConto, String sottoConto, String codPagamento, String banca, Integer progrGenerale, Integer rigo, Double quantita, Integer colli) {
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
                       Double prezzoBase, Float costoBase, Integer gruppoConto, String sottoConto, String codPagamento,
                       String banca, Integer progrGenerale, Integer rigo, Double quantita, Double quantitaV, Integer colli) {
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
        this.quantitaV = quantitaV;
        this.colli = colli;
    }
}