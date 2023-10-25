package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "GO_AMMORT_CESPITE")
@Getter
@Setter
public class AmmortamentoCespite extends PanacheEntityBase {

    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;
    
    @Column(name = "ID_AMMORTAMENTO", nullable = false, length = 36)
    private String idAmmortamento;
    
    @Column(name = "DATA_AMM", nullable = true)
    private Date dataAmm;
    
    @Column(name = "DESCRIZIONE", nullable = true, length = 200)
    private String descrizione;
    
    @Column(name = "PERC_AMM", nullable = true, precision = 0)
    private Double percAmm;
    
    @Column(name = "QUOTA", nullable = true, precision = 2)
    private Double quota;
    
    @Column(name = "FONDO", nullable = true, precision = 0)
    private Double fondo;
    
    @Column(name = "RESIDUO", nullable = true, precision = 0)
    private Double residuo;

    @Column(name = "ANNO")
    private Integer anno;
}
