package it.calolenoci.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GO_SHOWROOM_VISIT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowroomVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOME_CLIENTE", nullable = false, length = 150)
    private String nomeCliente;

    @Column(name = "PROVENIENZA", length = 100)
    private String provenienza;

    @Column(name = "TELEFONO", length = 50)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOTIVO_ID", nullable = false)
    private ShowroomMotivo motivo;

    @Column(name = "VENDITORE_ID", nullable = false)
    private Long venditoreId;

    @Column(name = "DATA_VISITA", nullable = false)
    private LocalDateTime dataVisita;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.dataVisita == null) {
            this.dataVisita = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
