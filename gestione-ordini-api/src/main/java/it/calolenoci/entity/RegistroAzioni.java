package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


@Entity
@Table(name = "REGISTROAZIONI")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegistroAzioni extends PanacheEntity {

    @Column
    public Integer anno;

    @Column(length = 3)
    private String serie;

    @Column
    private Integer progressivo;

    @Column
    private Integer rigo;

    @Column(length = 100)
    private String user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Column(length = 30)
    private String azione;


}
