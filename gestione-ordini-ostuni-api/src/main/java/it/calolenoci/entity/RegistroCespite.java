package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "registro_cespite")
@Getter
@Setter
public class RegistroCespite extends PanacheEntityBase {
    
    @Column(name = "id", nullable = false, length = 36)
    @Id
    private String id;
    
    @Column(name = "TIPO_CESPITE", nullable = false, length = 3)
    private String tipoCespite;
    
    @Column(name = "PROGRESSIVO1", nullable = true)
    private Integer progressivo1;
    
    @Column(name = "PROGRESSIVO2", nullable = true)
    private Integer progressivo2;
    
    @Column(name = "CESPITE", nullable = true, length = 500)
    private String cespite;
    
    @Column(name = "DATA_ACQ", nullable = true)
    private LocalDate dataAcq;
    
    @Column(name = "NUM_DOC_ACQ", nullable = true, length = 20)
    private String numDocAcq;
    
    @Column(name = "FORNITORE", nullable = true, length = 100)
    private String fornitore;
    
    @Column(name = "IMPORTO", nullable = true, precision = 0)
    private Double importo;
    
    @Column(name = "IMPORTO_RIVALUTAZIONE", nullable = true, precision = 0)
    private Double importoRivalutazione;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(name="ATTIVO", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean attivo;
    
    @Column(name = "DATA_VEND", nullable = true)
    private LocalDate dataVendita;
    
    @Column(name = "NUM_DOC_VEND", nullable = true, length = 20)
    private String numDocVend;
    
    @Column(name = "INTESTATARIO_VEND", nullable = true, length = 250)
    private String intestatarioVendita;
    
    @Column(name = "IMPORTO_VEND", nullable = true, precision = 0)
    private Double importoVendita;
    
    @Column(name = "SUPER_AMMORTAMENTO", nullable = true)
    private Integer superAmm;
    
    @Column(name = "PROTOCOLLO", nullable = true)
    private Integer protocollo;
    
    @Column(name = "GIORNALE", nullable = true, length = 1)
    private String giornale;
    
    @Column(name = "ANNO", nullable = true)
    private Integer anno;
    
    @Column(name = "DT_INIZIO_CALCOLO_AMM", nullable = true)
    private LocalDate dtInizioCalcoloAmm;
    
    @Column(name = "FL_PRIMO_ANNO", nullable = true, length = 1)
    private String flPrimoAnno;
    
    @Column(name = "FONDO_RIVALUTAZIONE", nullable = true, precision = 0)
    private Double fondoRivalutazione;
    
    @Column(name = "DATA_AMM", nullable = true)
    private LocalDate dataAmm;
    
    @Column(name = "descrAmm", nullable = true, length = 200)
    private String descrAmm;
    
    @Column(name = "PERC_AMM", nullable = true, precision = 0)
    private Double percAmm;
    
    @Column(name = "QUOTA", nullable = true, precision = 0)
    private Double quota;
    
    @Column(name = "FONDO", nullable = true, precision = 0)
    private Double fondo;
    
    @Column(name = "RESIDUO", nullable = true, precision = 0)
    private Double residuo;
    
    @Column(name = "annoAmm", nullable = true)
    private Integer annoAmm;
    
    @Column(name = "PERC_SUPER", nullable = true, precision = 0)
    private Double percSuper;
    
    @Column(name = "QUOTA_SUPER", nullable = true, precision = 0)
    private Double quotaSuper;
    
    @Column(name = "QUOTA_RIVALUTAZIONE", nullable = true, precision = 0)
    private Double quotaRivalutazione;
    
    @Column(name = "fondoRivAmm", nullable = true, precision = 0)
    private Double fondoRivAmm;
    
    @Column(name = "idTipoCesp", nullable = true, length = 36)
    private String idTipoCesp;
    
    @Column(name = "CODICE", nullable = true, length = 20)
    private String codice;
    
    @Column(name = "descrTipoCesp", nullable = true, length = 100)
    private String descrTipoCesp;
    
    @Column(name = "PERC_AMMORTAMENTO", nullable = true, precision = 0)
    private Double percAmmortamento;
    
    @Column(name = "COSTO_GRUPPO", nullable = true)
    private Integer costoGruppo;
    
    @Column(name = "COSTO_CONTO", nullable = true, length = 6)
    private String costoConto;
    
    @Column(name = "AMM_GRUPPO", nullable = true)
    private Integer ammGruppo;
    
    @Column(name = "AMM_CONTO", nullable = true, length = 6)
    private String ammConto;
    
    @Column(name = "FONDO_GRUPPO", nullable = true)
    private Integer fondoGruppo;
    
    @Column(name = "FONDO_CONTO", nullable = true, length = 6)
    private String fondoConto;
    
    @Column(name = "PLUS_GRUPPO", nullable = true)
    private Integer plusGruppo;
    
    @Column(name = "PLUS_CONTO", nullable = true, length = 6)
    private String plusConto;
    
    @Column(name = "MINUS_GRUPPO", nullable = true)
    private Integer minusGruppo;
    
    @Column(name = "MINUS_CONTO", nullable = true, length = 6)
    private String minusConto;

}
