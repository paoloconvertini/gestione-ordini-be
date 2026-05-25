package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GO_ATTIVITA_MONTAGGIO_OPERAIO")
@Getter
@Setter
public class AttivitaMontaggioOperaio extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_ATTIVITA_MONTAGGIO")
    private Long idAttivitaMontaggio;

    @Column(name = "ID_OPERAIO")
    private Long idOperaio;

    @Column(name = "PRINCIPALE")
    private Boolean principale;
}