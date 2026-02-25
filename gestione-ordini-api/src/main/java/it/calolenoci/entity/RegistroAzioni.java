package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Date;


@Entity
@Table(name = "GO_REGISTROAZIONI")
@Getter
@Setter
public class RegistroAzioni extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, updatable = false)
    private String id;

    @Column
    private Integer anno;

    @Column(length = 3)
    private String serie;

    @Column
    private Integer progressivo;

    @Column
    private Integer rigo;

    @Column(length = 100)
    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(length = 30)
    private String azione;

    @Column(name = "quantita")
    private Double quantita;

    @Column(length = 20, name= "tono")
    private String tono;

    @Column(name = "qtaRiservata")
    private Double qtaRiservata;

    @Column(name = "qtaProntoConsegna")
    private Double qtaProntoConsegna;



}
