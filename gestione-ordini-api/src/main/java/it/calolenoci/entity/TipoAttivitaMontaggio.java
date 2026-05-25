package it.calolenoci.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;


@Entity
@Table(name = "GO_TIPO_ATTIVITA_MONTAGGIO")
@Getter
@Setter
public class TipoAttivitaMontaggio extends PanacheEntityBase {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESCRIZIONE", nullable = false, length = 50)
    private String descrizione;

    @Column(name = "ORDINE_VISUALIZZAZIONE", nullable = false)
    private Integer ordineVisualizzazione;

    @Column(name = "ATTIVO", nullable = false)
    private Boolean attivo;

}
