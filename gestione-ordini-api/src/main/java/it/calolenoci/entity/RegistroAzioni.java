package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "GO_REGISTROAZIONI")
@Getter
@Setter
public class RegistroAzioni extends PanacheEntityBase {

    @Column(length = 36)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Id
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
