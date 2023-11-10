package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "GO_T_SUPER_AMMORT")
@Data
public class TipoSuperAmm extends PanacheEntity {

    @Column(name = "DESCRIZIONE", nullable = true, length = 100)
    private String descrizione;

    @Column(name = "PERC", nullable = true)
    private Integer perc;


}
