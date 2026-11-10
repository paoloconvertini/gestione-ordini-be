package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "GO_CONSEGNA_LIBERA")
@Getter
@Setter
public class ConsegnaLibera extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String intestazione;
    @Column(nullable = false, length = 255)
    private String indirizzo;
    @Column(nullable = false, length = 255)
    private String localita;
    @Column(length = 50)
    private String telefono;
    private Double latitudine;
    private Double longitudine;
    @Column(nullable = false)
    private LocalDate dataConsegna;
    @Column(nullable = false)
    private Character oraConsegna;
    @Column(nullable = false)
    private Integer idVeicolo;
    @Column(nullable = false)
    private Long ordine;
}
