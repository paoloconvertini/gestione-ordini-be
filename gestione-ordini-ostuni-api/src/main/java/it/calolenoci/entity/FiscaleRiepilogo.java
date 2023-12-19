package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "GO_FISCALE_RIEPILOGO")
@Getter
@Setter
public class FiscaleRiepilogo extends PanacheEntityBase {

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
    
    @Column(name = "DESCRIZIONE", nullable = false, length = 100)
    private String descrizione;
    
    @Column(name = "VALORE_AGGIORNATO", nullable = true, precision = 0)
    private Double valoreAggiornato;
    
    @Column(name = "AMMORTAMENTO_ORDINARIO", nullable = true, precision = 0)
    private Double ammortamentoOrdinario;
    
    @Column(name = "AMMORTAMENTO_RIVALUTATO", nullable = true, precision = 0)
    private Double ammortamentoRivalutato;
    
    @Column(name = "TOTALE_AMMORTAMENTO", nullable = true, precision = 0)
    private Double totaleAmmortamento;
    
    @Column(name = "NON_AMMORTABILE", nullable = true, precision = 0)
    private Double nonAmmortabile;
    
    @Column(name = "FONDO_AMMORTAMENTI", nullable = true, precision = 0)
    private Double fondoAmmortamenti;
    
    @Column(name = "FONDO_AMMORTAMENTI_RIV", nullable = true, precision = 0)
    private Double fondoAmmortamentiRiv;
    
    @Column(name = "SUPER_AMM1", nullable = true, precision = 0)
    private Double superAmm1;
    
    @Column(name = "SUPER_AMM2", nullable = true, precision = 0)
    private Double superAmm2;
    
    @Column(name = "SUPER_AMM3", nullable = true, precision = 0)
    private Double superAmm3;

    @Column(name = "SUPER_AMM4", nullable = true, precision = 0)
    private Double superAmm4;
    
    @Column(name = "PLUS", nullable = true, precision = 0)
    private Double plus;
    
    @Column(name = "MINUS", nullable = true, precision = 0)
    private Double minus;
    
    @Column(name = "RESIDUO", nullable = true, precision = 0)
    private Double residuo;

}
