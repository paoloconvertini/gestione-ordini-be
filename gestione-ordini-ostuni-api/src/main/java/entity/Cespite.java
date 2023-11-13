package entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "GO_CESPITE")
@Getter
@Setter
public class Cespite extends PanacheEntityBase {

    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;
    
    @Column(name = "TIPO_CESPITE", nullable = false, length = 3)
    private String tipoCespite;
    
    @Column(name = "CODICE", nullable = true, length = 20)
    private String codice;
    
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
    
    @Column(name = "AMMORTAMENTO", nullable = true, precision = 0)
    private Double ammortamento;

    @Type(type = "org.hibernate.type.TrueFalseType")
    @Column(name="ATTIVO", nullable = false, columnDefinition = "CHAR(1)")
    private Boolean attivo;

    @Column(name = "DATA_VEND", nullable = true)
    private LocalDate dataVendita;

    @Column(name = "NUM_DOC_VEND", nullable = true, length = 20)
    private String numDocVendita;

    @Column(name = "INTESTATARIO_VEND", nullable = true, length = 250)
    private String intestatarioVendita;

    @Column(name = "IMPORTO_VEND", nullable = true, precision = 0)
    private Double importoVendita;

    @Column(name = "SUPER_AMMORTAMENTO")
    private Long superAmm;

}
