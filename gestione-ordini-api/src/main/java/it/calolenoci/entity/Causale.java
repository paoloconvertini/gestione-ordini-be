package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "TMMC")
@Getter
@Setter
public class Causale extends PanacheEntityBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "MCAUSALE")
    private String mcausale;
    @Basic
    @Column(name = "DESCRCAUSALEMAG")
    private String descrcausalemag;
    @Basic
    @Column(name = "GIACENZA")
    private Integer giacenza;
    @Basic
    @Column(name = "CARICHI")
    private Integer carichi;
    @Basic
    @Column(name = "SCARICHI")
    private Integer scarichi;
    @Basic
    @Column(name = "FISCALE")
    private Integer fiscale;
    @Basic
    @Column(name = "STAMPAFISCALE")
    private Integer stampafiscale;
    @Basic
    @Column(name = "PROVENIENZA")
    private String provenienza;
    @Basic
    @Column(name = "DESTINAZIONE")
    private String destinazione;
    @Basic
    @Column(name = "QUANTITAOBBL")
    private String quantitaobbl;
    @Basic
    @Column(name = "VALOREOBBL")
    private String valoreobbl;
    @Basic
    @Column(name = "AGGIORNAULTIMO")
    private String aggiornaultimo;
    @Basic
    @Column(name = "CONTROCAUSALE")
    private String controcausale;
    @Basic
    @Column(name = "MOVFISCALE")
    private String movfiscale;
    @Basic
    @Column(name = "COMMESSA")
    private String commessa;
    @Basic
    @Column(name = "MOVCLAVORO")
    private String movclavoro;
    @Basic
    @Column(name = "MOVINTERNO")
    private String movinterno;
    @Basic
    @Column(name = "TIPOCOSTO")
    private String tipocosto;
    @Basic
    @Column(name = "NATURATRANSAZ")
    private String naturatransaz;
    @Basic
    @Column(name = "REGIME")
    private String regime;
    @Basic
    @Column(name = "CONSUMO")
    private Integer consumo;
    @Basic
    @Column(name = "CAUSALIREGTELE")
    private String causaliregtele;
    @Basic
    @Column(name = "FLAGPROD")
    private String flagprod;
    @Basic
    @Column(name = "SEQUENZA")
    private Integer sequenza;
    @Basic
    @Column(name = "FLARCHIVIABILE")
    private String flarchiviabile;
    @Basic
    @Column(name = "FLETICHETTE")
    private String fletichette;
    @Basic
    @Column(name = "FLATTESAFATTURA")
    private String flattesafattura;
    @Basic
    @Column(name = "PROGRESSIVI1")
    private Integer progressivi1;
    @Basic
    @Column(name = "PROGRESSIVI2")
    private Integer progressivi2;
    @Basic
    @Column(name = "PROGRESSIVI3")
    private Integer progressivi3;
    @Basic
    @Column(name = "PROGRESSIVI4")
    private Integer progressivi4;
    @Basic
    @Column(name = "PROGRESSIVI5")
    private Integer progressivi5;
    @Basic
    @Column(name = "CAMPOUSER1")
    private String campouser1;
    @Basic
    @Column(name = "CAMPOUSER2")
    private String campouser2;
    @Basic
    @Column(name = "CAMPOUSER3")
    private String campouser3;
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

        Causale causale = (Causale) o;

        if (mcausale != null ? !mcausale.equals(causale.mcausale) : causale.mcausale != null) return false;
        if (descrcausalemag != null ? !descrcausalemag.equals(causale.descrcausalemag) : causale.descrcausalemag != null)
            return false;
        if (giacenza != null ? !giacenza.equals(causale.giacenza) : causale.giacenza != null) return false;
        if (carichi != null ? !carichi.equals(causale.carichi) : causale.carichi != null) return false;
        if (scarichi != null ? !scarichi.equals(causale.scarichi) : causale.scarichi != null) return false;
        if (fiscale != null ? !fiscale.equals(causale.fiscale) : causale.fiscale != null) return false;
        if (stampafiscale != null ? !stampafiscale.equals(causale.stampafiscale) : causale.stampafiscale != null)
            return false;
        if (provenienza != null ? !provenienza.equals(causale.provenienza) : causale.provenienza != null) return false;
        if (destinazione != null ? !destinazione.equals(causale.destinazione) : causale.destinazione != null)
            return false;
        if (quantitaobbl != null ? !quantitaobbl.equals(causale.quantitaobbl) : causale.quantitaobbl != null)
            return false;
        if (valoreobbl != null ? !valoreobbl.equals(causale.valoreobbl) : causale.valoreobbl != null) return false;
        if (aggiornaultimo != null ? !aggiornaultimo.equals(causale.aggiornaultimo) : causale.aggiornaultimo != null)
            return false;
        if (controcausale != null ? !controcausale.equals(causale.controcausale) : causale.controcausale != null)
            return false;
        if (movfiscale != null ? !movfiscale.equals(causale.movfiscale) : causale.movfiscale != null) return false;
        if (commessa != null ? !commessa.equals(causale.commessa) : causale.commessa != null) return false;
        if (movclavoro != null ? !movclavoro.equals(causale.movclavoro) : causale.movclavoro != null) return false;
        if (movinterno != null ? !movinterno.equals(causale.movinterno) : causale.movinterno != null) return false;
        if (tipocosto != null ? !tipocosto.equals(causale.tipocosto) : causale.tipocosto != null) return false;
        if (naturatransaz != null ? !naturatransaz.equals(causale.naturatransaz) : causale.naturatransaz != null)
            return false;
        if (regime != null ? !regime.equals(causale.regime) : causale.regime != null) return false;
        if (consumo != null ? !consumo.equals(causale.consumo) : causale.consumo != null) return false;
        if (causaliregtele != null ? !causaliregtele.equals(causale.causaliregtele) : causale.causaliregtele != null)
            return false;
        if (flagprod != null ? !flagprod.equals(causale.flagprod) : causale.flagprod != null) return false;
        if (sequenza != null ? !sequenza.equals(causale.sequenza) : causale.sequenza != null) return false;
        if (flarchiviabile != null ? !flarchiviabile.equals(causale.flarchiviabile) : causale.flarchiviabile != null)
            return false;
        if (fletichette != null ? !fletichette.equals(causale.fletichette) : causale.fletichette != null) return false;
        if (flattesafattura != null ? !flattesafattura.equals(causale.flattesafattura) : causale.flattesafattura != null)
            return false;
        if (progressivi1 != null ? !progressivi1.equals(causale.progressivi1) : causale.progressivi1 != null)
            return false;
        if (progressivi2 != null ? !progressivi2.equals(causale.progressivi2) : causale.progressivi2 != null)
            return false;
        if (progressivi3 != null ? !progressivi3.equals(causale.progressivi3) : causale.progressivi3 != null)
            return false;
        if (progressivi4 != null ? !progressivi4.equals(causale.progressivi4) : causale.progressivi4 != null)
            return false;
        if (progressivi5 != null ? !progressivi5.equals(causale.progressivi5) : causale.progressivi5 != null)
            return false;
        if (campouser1 != null ? !campouser1.equals(causale.campouser1) : causale.campouser1 != null) return false;
        if (campouser2 != null ? !campouser2.equals(causale.campouser2) : causale.campouser2 != null) return false;
        if (campouser3 != null ? !campouser3.equals(causale.campouser3) : causale.campouser3 != null) return false;
        if (sysCreatedate != null ? !sysCreatedate.equals(causale.sysCreatedate) : causale.sysCreatedate != null)
            return false;
        if (sysCreateuser != null ? !sysCreateuser.equals(causale.sysCreateuser) : causale.sysCreateuser != null)
            return false;
        if (sysUpdatedate != null ? !sysUpdatedate.equals(causale.sysUpdatedate) : causale.sysUpdatedate != null)
            return false;
        if (sysUpdateuser != null ? !sysUpdateuser.equals(causale.sysUpdateuser) : causale.sysUpdateuser != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mcausale != null ? mcausale.hashCode() : 0;
        result = 31 * result + (descrcausalemag != null ? descrcausalemag.hashCode() : 0);
        result = 31 * result + (giacenza != null ? giacenza.hashCode() : 0);
        result = 31 * result + (carichi != null ? carichi.hashCode() : 0);
        result = 31 * result + (scarichi != null ? scarichi.hashCode() : 0);
        result = 31 * result + (fiscale != null ? fiscale.hashCode() : 0);
        result = 31 * result + (stampafiscale != null ? stampafiscale.hashCode() : 0);
        result = 31 * result + (provenienza != null ? provenienza.hashCode() : 0);
        result = 31 * result + (destinazione != null ? destinazione.hashCode() : 0);
        result = 31 * result + (quantitaobbl != null ? quantitaobbl.hashCode() : 0);
        result = 31 * result + (valoreobbl != null ? valoreobbl.hashCode() : 0);
        result = 31 * result + (aggiornaultimo != null ? aggiornaultimo.hashCode() : 0);
        result = 31 * result + (controcausale != null ? controcausale.hashCode() : 0);
        result = 31 * result + (movfiscale != null ? movfiscale.hashCode() : 0);
        result = 31 * result + (commessa != null ? commessa.hashCode() : 0);
        result = 31 * result + (movclavoro != null ? movclavoro.hashCode() : 0);
        result = 31 * result + (movinterno != null ? movinterno.hashCode() : 0);
        result = 31 * result + (tipocosto != null ? tipocosto.hashCode() : 0);
        result = 31 * result + (naturatransaz != null ? naturatransaz.hashCode() : 0);
        result = 31 * result + (regime != null ? regime.hashCode() : 0);
        result = 31 * result + (consumo != null ? consumo.hashCode() : 0);
        result = 31 * result + (causaliregtele != null ? causaliregtele.hashCode() : 0);
        result = 31 * result + (flagprod != null ? flagprod.hashCode() : 0);
        result = 31 * result + (sequenza != null ? sequenza.hashCode() : 0);
        result = 31 * result + (flarchiviabile != null ? flarchiviabile.hashCode() : 0);
        result = 31 * result + (fletichette != null ? fletichette.hashCode() : 0);
        result = 31 * result + (flattesafattura != null ? flattesafattura.hashCode() : 0);
        result = 31 * result + (progressivi1 != null ? progressivi1.hashCode() : 0);
        result = 31 * result + (progressivi2 != null ? progressivi2.hashCode() : 0);
        result = 31 * result + (progressivi3 != null ? progressivi3.hashCode() : 0);
        result = 31 * result + (progressivi4 != null ? progressivi4.hashCode() : 0);
        result = 31 * result + (progressivi5 != null ? progressivi5.hashCode() : 0);
        result = 31 * result + (campouser1 != null ? campouser1.hashCode() : 0);
        result = 31 * result + (campouser2 != null ? campouser2.hashCode() : 0);
        result = 31 * result + (campouser3 != null ? campouser3.hashCode() : 0);
        result = 31 * result + (sysCreatedate != null ? sysCreatedate.hashCode() : 0);
        result = 31 * result + (sysCreateuser != null ? sysCreateuser.hashCode() : 0);
        result = 31 * result + (sysUpdatedate != null ? sysUpdatedate.hashCode() : 0);
        result = 31 * result + (sysUpdateuser != null ? sysUpdateuser.hashCode() : 0);
        return result;
    }
}
