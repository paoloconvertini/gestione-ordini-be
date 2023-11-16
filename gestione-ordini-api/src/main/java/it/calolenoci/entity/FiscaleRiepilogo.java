package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "GO_FISCALE_RIEPILOGO")
public class FiscaleRiepilogo extends PanacheEntityBase {

    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;

    @Column(name="TIPO_CESPITE", length = 3)
    private String tipoCespite;

    @Column(name="DESCRIZIONE")
    String desc;

    @Column(name="VALORE_AGGIORNATO")
    Double valoreAggiornato = 0D;

    @Column(name="AMMORTAMENTO_ORDINARIO")
    Double ammortamentoOrdinario = 0D;

    @Column(name="AMMORTAMENTO_ANTICIPATO")
    Double ammortamentoAnticipato = 0D;

    @Column(name="TOTALE_AMMORTAMENTO")
    Double totaleAmmortamento = 0D;

    @Column(name="NON_AMMORTABILE")
    Double nonAmmortabile = 0D;

    @Column(name="FONDO_AMMORTAMENTI")
    Double fondoAmmortamenti = 0D;

    @Column(name="RESIDUO")
    Double residuo = 0D;

}
