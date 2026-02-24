package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "TCA1")
@Getter
@Setter
public class ArticoloClasseFornitore extends PanacheEntityBase {

    @Id
    @Column(length = 3)
    private String codice;

    @Column(length = 40)
    private String descrizione;

    @Column(length = 90, name = "DESCRUSER")
    private String descrUser;

    @Column(length = 90, name = "DESCRUSER2")
    private String descrUser2;

    @Column(length = 90, name = "DESCRUSER3")
    private String descrUser3;

    @Column(name = "SYS_CREATEDATE")
    private Date createDate;

    @Column(name = "SYS_UPDATEDATE")
    private Date updateDate;

    @Column(length = 20, name = "SYS_CREATEUSER")
    private String createUser;

    @Column(length = 20, name = "SYS_UPDATEUSER")
    private String updateUser;
}
