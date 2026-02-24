package it.calolenoci.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GO_SHOWROOM_MOTIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomMotivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESCRIZIONE", nullable = false, length = 150)
    private String descrizione;

    @Column(name = "ATTIVO", nullable = false)
    private Boolean attivo = true;
}
