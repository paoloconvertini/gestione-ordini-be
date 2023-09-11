package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Date;

@Entity
@IdClass(SaldiMagazzinoPK.class)
@Getter
@Setter
@Table(name = "SALDIMAGAZZINO")
public class SaldiMagazzino extends PanacheEntityBase {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "MARTICOLO")
    private String marticolo;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "VAR1")
    private String var1;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "VAR2")
    private String var2;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "VAR3")
    private String var3;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "VAR4")
    private String var4;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "VAR5")
    private String var5;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "MMAGAZZINO")
    private String mmagazzino;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "QTY")
    private String qty;
    @Basic
    @Column(name = "QCARICHI")
    private Double qcarichi;
    @Basic
    @Column(name = "VCARICHI")
    private Double vcarichi;
    @Basic
    @Column(name = "QSCARICHI")
    private Double qscarichi;
    @Basic
    @Column(name = "VSCARICHI")
    private Double vscarichi;
    @Basic
    @Column(name = "QFISCALE")
    private Double qfiscale;
    @Basic
    @Column(name = "VFISCALE")
    private Double vfiscale;
    @Basic
    @Column(name = "QGIACENZA")
    private Double qgiacenza;
    @Basic
    @Column(name = "SCORTAMIN")
    private Double scortamin;
    @Basic
    @Column(name = "QCARICHIU")
    private Double qcarichiu;
    @Basic
    @Column(name = "VCARICHIU")
    private Double vcarichiu;
    @Basic
    @Column(name = "QSCARICHIU")
    private Double qscarichiu;
    @Basic
    @Column(name = "VSCARICHIU")
    private Double vscarichiu;
    @Basic
    @Column(name = "QFISCALEU")
    private Double qfiscaleu;
    @Basic
    @Column(name = "VFISCALEU")
    private Double vfiscaleu;
    @Basic
    @Column(name = "QGIACENZAU")
    private Double qgiacenzau;
    @Basic
    @Column(name = "VGIACENZAU")
    private Double vgiacenzau;
    @Basic
    @Column(name = "COSTOULTACQUU")
    private Double costoultacquu;
    @Basic
    @Column(name = "DATAMODIFICA")
    private Date datamodifica;
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

    public String getMarticolo() {
        return marticolo;
    }

    public void setMarticolo(String marticolo) {
        this.marticolo = marticolo;
    }

    public String getVar1() {
        return var1;
    }

    public void setVar1(String var1) {
        this.var1 = var1;
    }

    public String getVar2() {
        return var2;
    }

    public void setVar2(String var2) {
        this.var2 = var2;
    }

    public String getVar3() {
        return var3;
    }

    public void setVar3(String var3) {
        this.var3 = var3;
    }

    public String getVar4() {
        return var4;
    }

    public void setVar4(String var4) {
        this.var4 = var4;
    }

    public String getVar5() {
        return var5;
    }

    public void setVar5(String var5) {
        this.var5 = var5;
    }

    public String getMmagazzino() {
        return mmagazzino;
    }

    public void setMmagazzino(String mmagazzino) {
        this.mmagazzino = mmagazzino;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public Double getQcarichi() {
        return qcarichi;
    }

    public void setQcarichi(Double qcarichi) {
        this.qcarichi = qcarichi;
    }

    public Double getVcarichi() {
        return vcarichi;
    }

    public void setVcarichi(Double vcarichi) {
        this.vcarichi = vcarichi;
    }

    public Double getQscarichi() {
        return qscarichi;
    }

    public void setQscarichi(Double qscarichi) {
        this.qscarichi = qscarichi;
    }

    public Double getVscarichi() {
        return vscarichi;
    }

    public void setVscarichi(Double vscarichi) {
        this.vscarichi = vscarichi;
    }

    public Double getQfiscale() {
        return qfiscale;
    }

    public void setQfiscale(Double qfiscale) {
        this.qfiscale = qfiscale;
    }

    public Double getVfiscale() {
        return vfiscale;
    }

    public void setVfiscale(Double vfiscale) {
        this.vfiscale = vfiscale;
    }

    public Double getQgiacenza() {
        return qgiacenza;
    }

    public void setQgiacenza(Double qgiacenza) {
        this.qgiacenza = qgiacenza;
    }

    public Double getScortamin() {
        return scortamin;
    }

    public void setScortamin(Double scortamin) {
        this.scortamin = scortamin;
    }

    public Double getQcarichiu() {
        return qcarichiu;
    }

    public void setQcarichiu(Double qcarichiu) {
        this.qcarichiu = qcarichiu;
    }

    public Double getVcarichiu() {
        return vcarichiu;
    }

    public void setVcarichiu(Double vcarichiu) {
        this.vcarichiu = vcarichiu;
    }

    public Double getQscarichiu() {
        return qscarichiu;
    }

    public void setQscarichiu(Double qscarichiu) {
        this.qscarichiu = qscarichiu;
    }

    public Double getVscarichiu() {
        return vscarichiu;
    }

    public void setVscarichiu(Double vscarichiu) {
        this.vscarichiu = vscarichiu;
    }

    public Double getQfiscaleu() {
        return qfiscaleu;
    }

    public void setQfiscaleu(Double qfiscaleu) {
        this.qfiscaleu = qfiscaleu;
    }

    public Double getVfiscaleu() {
        return vfiscaleu;
    }

    public void setVfiscaleu(Double vfiscaleu) {
        this.vfiscaleu = vfiscaleu;
    }

    public Double getQgiacenzau() {
        return qgiacenzau;
    }

    public void setQgiacenzau(Double qgiacenzau) {
        this.qgiacenzau = qgiacenzau;
    }

    public Double getVgiacenzau() {
        return vgiacenzau;
    }

    public void setVgiacenzau(Double vgiacenzau) {
        this.vgiacenzau = vgiacenzau;
    }

    public Double getCostoultacquu() {
        return costoultacquu;
    }

    public void setCostoultacquu(Double costoultacquu) {
        this.costoultacquu = costoultacquu;
    }

    public Date getDatamodifica() {
        return datamodifica;
    }

    public void setDatamodifica(Date datamodifica) {
        this.datamodifica = datamodifica;
    }

    public Date getSysCreatedate() {
        return sysCreatedate;
    }

    public void setSysCreatedate(Date sysCreatedate) {
        this.sysCreatedate = sysCreatedate;
    }

    public String getSysCreateuser() {
        return sysCreateuser;
    }

    public void setSysCreateuser(String sysCreateuser) {
        this.sysCreateuser = sysCreateuser;
    }

    public Date getSysUpdatedate() {
        return sysUpdatedate;
    }

    public void setSysUpdatedate(Date sysUpdatedate) {
        this.sysUpdatedate = sysUpdatedate;
    }

    public String getSysUpdateuser() {
        return sysUpdateuser;
    }

    public void setSysUpdateuser(String sysUpdateuser) {
        this.sysUpdateuser = sysUpdateuser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SaldiMagazzino that = (SaldiMagazzino) o;

        if (marticolo != null ? !marticolo.equals(that.marticolo) : that.marticolo != null) return false;
        if (var1 != null ? !var1.equals(that.var1) : that.var1 != null) return false;
        if (var2 != null ? !var2.equals(that.var2) : that.var2 != null) return false;
        if (var3 != null ? !var3.equals(that.var3) : that.var3 != null) return false;
        if (var4 != null ? !var4.equals(that.var4) : that.var4 != null) return false;
        if (var5 != null ? !var5.equals(that.var5) : that.var5 != null) return false;
        if (mmagazzino != null ? !mmagazzino.equals(that.mmagazzino) : that.mmagazzino != null) return false;
        if (qty != null ? !qty.equals(that.qty) : that.qty != null) return false;
        if (qcarichi != null ? !qcarichi.equals(that.qcarichi) : that.qcarichi != null) return false;
        if (vcarichi != null ? !vcarichi.equals(that.vcarichi) : that.vcarichi != null) return false;
        if (qscarichi != null ? !qscarichi.equals(that.qscarichi) : that.qscarichi != null) return false;
        if (vscarichi != null ? !vscarichi.equals(that.vscarichi) : that.vscarichi != null) return false;
        if (qfiscale != null ? !qfiscale.equals(that.qfiscale) : that.qfiscale != null) return false;
        if (vfiscale != null ? !vfiscale.equals(that.vfiscale) : that.vfiscale != null) return false;
        if (qgiacenza != null ? !qgiacenza.equals(that.qgiacenza) : that.qgiacenza != null) return false;
        if (scortamin != null ? !scortamin.equals(that.scortamin) : that.scortamin != null) return false;
        if (qcarichiu != null ? !qcarichiu.equals(that.qcarichiu) : that.qcarichiu != null) return false;
        if (vcarichiu != null ? !vcarichiu.equals(that.vcarichiu) : that.vcarichiu != null) return false;
        if (qscarichiu != null ? !qscarichiu.equals(that.qscarichiu) : that.qscarichiu != null) return false;
        if (vscarichiu != null ? !vscarichiu.equals(that.vscarichiu) : that.vscarichiu != null) return false;
        if (qfiscaleu != null ? !qfiscaleu.equals(that.qfiscaleu) : that.qfiscaleu != null) return false;
        if (vfiscaleu != null ? !vfiscaleu.equals(that.vfiscaleu) : that.vfiscaleu != null) return false;
        if (qgiacenzau != null ? !qgiacenzau.equals(that.qgiacenzau) : that.qgiacenzau != null) return false;
        if (vgiacenzau != null ? !vgiacenzau.equals(that.vgiacenzau) : that.vgiacenzau != null) return false;
        if (costoultacquu != null ? !costoultacquu.equals(that.costoultacquu) : that.costoultacquu != null)
            return false;
        if (datamodifica != null ? !datamodifica.equals(that.datamodifica) : that.datamodifica != null) return false;
        if (sysCreatedate != null ? !sysCreatedate.equals(that.sysCreatedate) : that.sysCreatedate != null)
            return false;
        if (sysCreateuser != null ? !sysCreateuser.equals(that.sysCreateuser) : that.sysCreateuser != null)
            return false;
        if (sysUpdatedate != null ? !sysUpdatedate.equals(that.sysUpdatedate) : that.sysUpdatedate != null)
            return false;
        if (sysUpdateuser != null ? !sysUpdateuser.equals(that.sysUpdateuser) : that.sysUpdateuser != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = marticolo != null ? marticolo.hashCode() : 0;
        result = 31 * result + (var1 != null ? var1.hashCode() : 0);
        result = 31 * result + (var2 != null ? var2.hashCode() : 0);
        result = 31 * result + (var3 != null ? var3.hashCode() : 0);
        result = 31 * result + (var4 != null ? var4.hashCode() : 0);
        result = 31 * result + (var5 != null ? var5.hashCode() : 0);
        result = 31 * result + (mmagazzino != null ? mmagazzino.hashCode() : 0);
        result = 31 * result + (qty != null ? qty.hashCode() : 0);
        result = 31 * result + (qcarichi != null ? qcarichi.hashCode() : 0);
        result = 31 * result + (vcarichi != null ? vcarichi.hashCode() : 0);
        result = 31 * result + (qscarichi != null ? qscarichi.hashCode() : 0);
        result = 31 * result + (vscarichi != null ? vscarichi.hashCode() : 0);
        result = 31 * result + (qfiscale != null ? qfiscale.hashCode() : 0);
        result = 31 * result + (vfiscale != null ? vfiscale.hashCode() : 0);
        result = 31 * result + (qgiacenza != null ? qgiacenza.hashCode() : 0);
        result = 31 * result + (scortamin != null ? scortamin.hashCode() : 0);
        result = 31 * result + (qcarichiu != null ? qcarichiu.hashCode() : 0);
        result = 31 * result + (vcarichiu != null ? vcarichiu.hashCode() : 0);
        result = 31 * result + (qscarichiu != null ? qscarichiu.hashCode() : 0);
        result = 31 * result + (vscarichiu != null ? vscarichiu.hashCode() : 0);
        result = 31 * result + (qfiscaleu != null ? qfiscaleu.hashCode() : 0);
        result = 31 * result + (vfiscaleu != null ? vfiscaleu.hashCode() : 0);
        result = 31 * result + (qgiacenzau != null ? qgiacenzau.hashCode() : 0);
        result = 31 * result + (vgiacenzau != null ? vgiacenzau.hashCode() : 0);
        result = 31 * result + (costoultacquu != null ? costoultacquu.hashCode() : 0);
        result = 31 * result + (datamodifica != null ? datamodifica.hashCode() : 0);
        result = 31 * result + (sysCreatedate != null ? sysCreatedate.hashCode() : 0);
        result = 31 * result + (sysCreateuser != null ? sysCreateuser.hashCode() : 0);
        result = 31 * result + (sysUpdatedate != null ? sysUpdatedate.hashCode() : 0);
        result = 31 * result + (sysUpdateuser != null ? sysUpdateuser.hashCode() : 0);
        return result;
    }
}
