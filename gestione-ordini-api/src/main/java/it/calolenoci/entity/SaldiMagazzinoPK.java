package it.calolenoci.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

public class SaldiMagazzinoPK implements Serializable {
    @Column(name = "MARTICOLO")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String marticolo;
    @Column(name = "VAR1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String var1;
    @Column(name = "VAR2")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String var2;
    @Column(name = "VAR3")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String var3;
    @Column(name = "VAR4")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String var4;
    @Column(name = "VAR5")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String var5;
    @Column(name = "MMAGAZZINO")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String mmagazzino;
    @Column(name = "QTY")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String qty;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SaldiMagazzinoPK that = (SaldiMagazzinoPK) o;

        if (marticolo != null ? !marticolo.equals(that.marticolo) : that.marticolo != null) return false;
        if (var1 != null ? !var1.equals(that.var1) : that.var1 != null) return false;
        if (var2 != null ? !var2.equals(that.var2) : that.var2 != null) return false;
        if (var3 != null ? !var3.equals(that.var3) : that.var3 != null) return false;
        if (var4 != null ? !var4.equals(that.var4) : that.var4 != null) return false;
        if (var5 != null ? !var5.equals(that.var5) : that.var5 != null) return false;
        if (mmagazzino != null ? !mmagazzino.equals(that.mmagazzino) : that.mmagazzino != null) return false;
        if (qty != null ? !qty.equals(that.qty) : that.qty != null) return false;

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
        return result;
    }
}
