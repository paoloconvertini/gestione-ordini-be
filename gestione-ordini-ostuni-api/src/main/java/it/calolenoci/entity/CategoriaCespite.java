package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "GO_TIPO_CESPITE")
@Getter
@Setter
public class CategoriaCespite extends PanacheEntityBase {

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
    
    @Column(name = "DESCRIZIONE", nullable = true, length = 100)
    private String descrizione;

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
