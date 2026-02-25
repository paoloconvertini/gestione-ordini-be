package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "GO_NOTA_CONSEGNA")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotaConsegna extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, updatable = false)
    private String id;
    @Column(name = "dataNota", nullable = false)
    private LocalDate dataNota;
    @Column(name = "nota", length = 2000)
    private String nota;


}
