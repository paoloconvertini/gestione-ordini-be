package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "GO_ATTIVITA_MONTAGGIO_DETT")
@Getter
@Setter
public class AttivitaMontaggioDett extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_ATTIVITA_MONTAGGIO")
    private Long idAttivitaMontaggio;

    @Column(name = "ID_TIPO_ATTIVITA")
    private Long idTipoAttivita;

    @Column(name = "QUANTITA")
    private BigDecimal quantita;

    @Column(name = "COMPLETATO")
    private Boolean completato;

    @Column(name = "NOTE")
    private String note;
}