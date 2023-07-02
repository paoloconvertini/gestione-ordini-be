package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MAGAZZINO")
@Getter
@Setter
public class Magazzino extends PanacheEntityBase {

    @EmbeddedId
    private MagazzinoId magazzinoId;

    @Column(length = 13)
    private String mArticolo;

    @Column(name="VALOREUNITARIO")
    private Double valoreUnitario;

    @Column(name = "DATAOPMAGAZZINO")
    private Date dataMagazzino;

}
