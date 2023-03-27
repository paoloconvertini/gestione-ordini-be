package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@IdClass(MagazzinoId.class)
@Getter
@Setter
@Table(name = "MAGAZZINO")
public class Magazzino extends PanacheEntityBase {

    @Column(length = 4, name = "ANNO")
    @Id
    private  Integer anno;

    @Column(length = 3, name = "SERIEMAGAZZINO")
    @Id
    private String serie;

    @Column(name="PROGRESSIVOMAG")
    @Id
    private Integer progressivo;

    @Column
    @Id
    private String flagControMag;

    @Column
    @Id
    private Integer rigo;

    @Column
    private Integer gruppoMag;

    @Column(length = 6)
    private String contoMag;

    @Column(length = 13)
    private String mArticolo;

    @Column(length = 3)
    private String mCausale;

    @Column(length = 60)
    private String mDescrArticolo;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataInserimento;
}
