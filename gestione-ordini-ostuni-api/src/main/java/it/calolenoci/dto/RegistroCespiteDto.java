package it.calolenoci.dto;

import it.calolenoci.entity.RegistroCespite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for the {@link it.calolenoci.entity.RegistroCespite} entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistroCespiteDto implements Serializable {
    private String id;
    private String tipoCespite;
    private Integer progressivo1;
    private Integer progressivo2;
    private String cespite;
    private LocalDate dataAcq;
    private String numDocAcq;
    private String fornitore;
    private Double importo;
    private Double importoRivalutazione;
    private Boolean attivo;
    private LocalDate dataVendita;
    private String numDocVend;
    private String intestatarioVendita;
    private Double importoVendita;
    private Long superAmm;
    private Integer protocollo;
    private String giornale;
    private Integer anno;
    private LocalDate dtInizioCalcoloAmm;
    private Boolean flPrimoAnno;
    private Double fondoRivalutazione;
    private LocalDate dataAmm;
    private String descrAmm;
    private Double percAmm;
    private Double quota;
    private Double fondo;
    private Double residuo;
    private Integer annoAmm;
    private Double percSuper;
    private Double quotaSuper;
    private Double quotaRivalutazione;
    private Double fondoRivAmm;
    private String idTipoCesp;
    private String descrTipoCesp;
    private Double percAmmortamento;
    private Integer costoGruppo;
    private String costoConto;
    private Integer ammGruppo;
    private String ammConto;
    private Integer fondoGruppo;
    private String fondoConto;
    private Integer plusGruppo;
    private String plusConto;
    private Integer minusGruppo;
    private String minusConto;


    public RegistroCespiteDto(String id, String tipoCespite, Integer progressivo1, Integer progressivo2, String cespite,
                              LocalDate dataAcq, String numDocAcq, String fornitore, Double importo, Double importoRivalutazione,
                              Boolean attivo, LocalDate dataVendita, String numDocVend, String intestatarioVendita, Double importoVendita,
                              Long superAmm, Integer protocollo, String giornale, Integer anno, LocalDate dtInizioCalcoloAmm,
                              Boolean flPrimoAnno, Double fondoRivalutazione, String idTipoCesp, String descrTipoCesp,
                              Double percAmmortamento, Integer costoGruppo, String costoConto, Integer ammGruppo, String ammConto,
                              Integer fondoGruppo, String fondoConto, Integer plusGruppo, String plusConto, Integer minusGruppo, String minusConto) {
        this.id = id;
        this.tipoCespite = tipoCespite;
        this.progressivo1 = progressivo1;
        this.progressivo2 = progressivo2;
        this.cespite = cespite;
        this.dataAcq = dataAcq;
        this.numDocAcq = numDocAcq;
        this.fornitore = fornitore;
        this.importo = importo;
        this.importoRivalutazione = importoRivalutazione;
        this.attivo = attivo;
        this.dataVendita = dataVendita;
        this.numDocVend = numDocVend;
        this.intestatarioVendita = intestatarioVendita;
        this.importoVendita = importoVendita;
        this.superAmm = superAmm;
        this.protocollo = protocollo;
        this.giornale = giornale;
        this.anno = anno;
        this.dtInizioCalcoloAmm = dtInizioCalcoloAmm;
        this.flPrimoAnno = flPrimoAnno;
        this.fondoRivalutazione = fondoRivalutazione;
        this.idTipoCesp = idTipoCesp;
        this.descrTipoCesp = descrTipoCesp;
        this.percAmmortamento = percAmmortamento;
        this.costoGruppo = costoGruppo;
        this.costoConto = costoConto;
        this.ammGruppo = ammGruppo;
        this.ammConto = ammConto;
        this.fondoGruppo = fondoGruppo;
        this.fondoConto = fondoConto;
        this.plusGruppo = plusGruppo;
        this.plusConto = plusConto;
        this.minusGruppo = minusGruppo;
        this.minusConto = minusConto;
    }

}