package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import lombok.*;

import java.util.List;

@Entity
@Table(name = "GO_SHOWROOM_MOTIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomMotivo extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESCRIZIONE", nullable = false, length = 150)
    private String descrizione;

    @Column(name = "ATTIVO", nullable = false)
    private Boolean attivo = true;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private ShowroomMotivo parent;
}