package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "TFTC")
@Getter
@Setter
public class Vettore extends PanacheEntityBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "VETTORE")
    private String vettore;
    @Basic
    @Column(name = "INTESTVETTORE")
    private String intestvettore;
    @Basic
    @Column(name = "INDIRVETTORE")
    private String indirvettore;
    @Basic
    @Column(name = "LOCVETTORE")
    private String locvettore;
    @Basic
    @Column(name = "NOTEVETTORE")
    private String notevettore;
    @Basic
    @Column(name = "PARTITAIVA")
    private String partitaiva;
    @Basic
    @Column(name = "NUMALBO")
    private String numalbo;
    @Basic
    @Column(name = "TARGA")
    private String targa;
    @Basic
    @Column(name = "TARGARIMORCHIO")
    private String targarimorchio;
    @Basic
    @Column(name = "GRUPPOVETTORE")
    private Integer gruppovettore;
    @Basic
    @Column(name = "CONTOVETTORE")
    private String contovettore;
    @Basic
    @Column(name = "CURATRASPORTO")
    private String curatrasporto;
    @Basic
    @Column(name = "FLMODOCONSEGNA")
    private String flmodoconsegna;
    @Basic
    @Column(name = "NCOPIEBOLLA")
    private Integer ncopiebolla;
    @Basic
    @Column(name = "MODULODOC")
    private String modulodoc;
    @Basic
    @Column(name = "FLETICHETTACOLLI")
    private String fletichettacolli;
    @Basic
    @Column(name = "PRETICHETTACOLLI")
    private Integer pretichettacolli;
    @Basic
    @Column(name = "FORMULATRASP")
    private String formulatrasp;
    @Basic
    @Column(name = "MODULOETK")
    private String moduloetk;
    @Basic
    @Column(name = "GRUPPO")
    private String gruppo;
    @Basic
    @Column(name = "VALUSER1")
    private Double valuser1;
    @Basic
    @Column(name = "VALUSER2")
    private Double valuser2;
    @Basic
    @Column(name = "CAMPOUSER1")
    private String campouser1;
    @Basic
    @Column(name = "CAMPOUSER2")
    private String campouser2;
    @Basic
    @Column(name = "SYS_CREATEDATE")
    private Date sysCreatedate;
    @Basic
    @Column(name = "SYS_CREATEUSER")
    private String sysCreateuser;
    @Basic
    @Column(name = "SYS_UPDATEDATE")
    private Date sysUpdatedate;
    @Basic
    @Column(name = "SYS_UPDATEUSER")
    private String sysUpdateuser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vettore vettore1 = (Vettore) o;

        if (vettore != null ? !vettore.equals(vettore1.vettore) : vettore1.vettore != null) return false;
        if (intestvettore != null ? !intestvettore.equals(vettore1.intestvettore) : vettore1.intestvettore != null)
            return false;
        if (indirvettore != null ? !indirvettore.equals(vettore1.indirvettore) : vettore1.indirvettore != null)
            return false;
        if (locvettore != null ? !locvettore.equals(vettore1.locvettore) : vettore1.locvettore != null) return false;
        if (notevettore != null ? !notevettore.equals(vettore1.notevettore) : vettore1.notevettore != null)
            return false;
        if (partitaiva != null ? !partitaiva.equals(vettore1.partitaiva) : vettore1.partitaiva != null) return false;
        if (numalbo != null ? !numalbo.equals(vettore1.numalbo) : vettore1.numalbo != null) return false;
        if (targa != null ? !targa.equals(vettore1.targa) : vettore1.targa != null) return false;
        if (targarimorchio != null ? !targarimorchio.equals(vettore1.targarimorchio) : vettore1.targarimorchio != null)
            return false;
        if (gruppovettore != null ? !gruppovettore.equals(vettore1.gruppovettore) : vettore1.gruppovettore != null)
            return false;
        if (contovettore != null ? !contovettore.equals(vettore1.contovettore) : vettore1.contovettore != null)
            return false;
        if (curatrasporto != null ? !curatrasporto.equals(vettore1.curatrasporto) : vettore1.curatrasporto != null)
            return false;
        if (flmodoconsegna != null ? !flmodoconsegna.equals(vettore1.flmodoconsegna) : vettore1.flmodoconsegna != null)
            return false;
        if (ncopiebolla != null ? !ncopiebolla.equals(vettore1.ncopiebolla) : vettore1.ncopiebolla != null)
            return false;
        if (modulodoc != null ? !modulodoc.equals(vettore1.modulodoc) : vettore1.modulodoc != null) return false;
        if (fletichettacolli != null ? !fletichettacolli.equals(vettore1.fletichettacolli) : vettore1.fletichettacolli != null)
            return false;
        if (pretichettacolli != null ? !pretichettacolli.equals(vettore1.pretichettacolli) : vettore1.pretichettacolli != null)
            return false;
        if (formulatrasp != null ? !formulatrasp.equals(vettore1.formulatrasp) : vettore1.formulatrasp != null)
            return false;
        if (moduloetk != null ? !moduloetk.equals(vettore1.moduloetk) : vettore1.moduloetk != null) return false;
        if (gruppo != null ? !gruppo.equals(vettore1.gruppo) : vettore1.gruppo != null) return false;
        if (valuser1 != null ? !valuser1.equals(vettore1.valuser1) : vettore1.valuser1 != null) return false;
        if (valuser2 != null ? !valuser2.equals(vettore1.valuser2) : vettore1.valuser2 != null) return false;
        if (campouser1 != null ? !campouser1.equals(vettore1.campouser1) : vettore1.campouser1 != null) return false;
        if (campouser2 != null ? !campouser2.equals(vettore1.campouser2) : vettore1.campouser2 != null) return false;
        if (sysCreatedate != null ? !sysCreatedate.equals(vettore1.sysCreatedate) : vettore1.sysCreatedate != null)
            return false;
        if (sysCreateuser != null ? !sysCreateuser.equals(vettore1.sysCreateuser) : vettore1.sysCreateuser != null)
            return false;
        if (sysUpdatedate != null ? !sysUpdatedate.equals(vettore1.sysUpdatedate) : vettore1.sysUpdatedate != null)
            return false;
        if (sysUpdateuser != null ? !sysUpdateuser.equals(vettore1.sysUpdateuser) : vettore1.sysUpdateuser != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = vettore != null ? vettore.hashCode() : 0;
        result = 31 * result + (intestvettore != null ? intestvettore.hashCode() : 0);
        result = 31 * result + (indirvettore != null ? indirvettore.hashCode() : 0);
        result = 31 * result + (locvettore != null ? locvettore.hashCode() : 0);
        result = 31 * result + (notevettore != null ? notevettore.hashCode() : 0);
        result = 31 * result + (partitaiva != null ? partitaiva.hashCode() : 0);
        result = 31 * result + (numalbo != null ? numalbo.hashCode() : 0);
        result = 31 * result + (targa != null ? targa.hashCode() : 0);
        result = 31 * result + (targarimorchio != null ? targarimorchio.hashCode() : 0);
        result = 31 * result + (gruppovettore != null ? gruppovettore.hashCode() : 0);
        result = 31 * result + (contovettore != null ? contovettore.hashCode() : 0);
        result = 31 * result + (curatrasporto != null ? curatrasporto.hashCode() : 0);
        result = 31 * result + (flmodoconsegna != null ? flmodoconsegna.hashCode() : 0);
        result = 31 * result + (ncopiebolla != null ? ncopiebolla.hashCode() : 0);
        result = 31 * result + (modulodoc != null ? modulodoc.hashCode() : 0);
        result = 31 * result + (fletichettacolli != null ? fletichettacolli.hashCode() : 0);
        result = 31 * result + (pretichettacolli != null ? pretichettacolli.hashCode() : 0);
        result = 31 * result + (formulatrasp != null ? formulatrasp.hashCode() : 0);
        result = 31 * result + (moduloetk != null ? moduloetk.hashCode() : 0);
        result = 31 * result + (gruppo != null ? gruppo.hashCode() : 0);
        result = 31 * result + (valuser1 != null ? valuser1.hashCode() : 0);
        result = 31 * result + (valuser2 != null ? valuser2.hashCode() : 0);
        result = 31 * result + (campouser1 != null ? campouser1.hashCode() : 0);
        result = 31 * result + (campouser2 != null ? campouser2.hashCode() : 0);
        result = 31 * result + (sysCreatedate != null ? sysCreatedate.hashCode() : 0);
        result = 31 * result + (sysCreateuser != null ? sysCreateuser.hashCode() : 0);
        result = 31 * result + (sysUpdatedate != null ? sysUpdatedate.hashCode() : 0);
        result = 31 * result + (sysUpdateuser != null ? sysUpdateuser.hashCode() : 0);
        return result;
    }
}
