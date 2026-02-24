package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.converter.TrueFalseConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "GO_ORD_VEICOLO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoOrdVeicolo extends PanacheEntityBase {

    @EmbeddedId
    private GoOrdVeicoloPK id;

    @Column(name = "idVeicolo")
    private int idVeicolo;

    @Column(name = "DATA_CONSEGNA")
    private LocalDate dataConsegna;

    @Column(name = "FL_VENDITORE", nullable = false, columnDefinition = "CHAR(1)")
    @Convert(converter = TrueFalseConverter.class)
    private Boolean venditore;

    @Column(name = "ora_consegna")
    private Character oraConsegna;

    @Column(name = "ordine")
    private Long ordine;
}
