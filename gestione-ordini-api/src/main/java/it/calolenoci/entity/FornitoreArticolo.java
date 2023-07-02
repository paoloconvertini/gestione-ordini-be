package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "FORNALTERNATIVI")
@Getter
@Setter
public class FornitoreArticolo extends PanacheEntityBase {

    @EmbeddedId
    private FornitoreArticoloId fornitoreArticoloId;

    @Column(name = "FDEFAULT", length = 1)
    private String fDefault;

    @Column(name = "TEMPOCONSEGNA")
    private Integer tempoConsegna;

    @Column(name = "PREZZO")
    private Float prezzo;

    @Column(name = "COEFFPREZZO")
    private Float coefPrezzo;

    @Column(name = "SYS_CREATEDATE")
    private Date createDate;

    @Column(name = "SYS_UPDATEDATE")
    private Date updateDate;

    @Column(name = "SYS_CREATEUSER")
    private String createUser;

    @Column(name = "SYS_UPDATEUSER")
    private String updateUser;

    @Override
    public String toString() {
        return "FornitoreArticolo{" +
                "fornitoreArticoloId=" + fornitoreArticoloId.getArticolo() + "-" + fornitoreArticoloId.getConto() +
                '}';
    }
}
