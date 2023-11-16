package it.calolenoci.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

public class PrimanotaPK implements Serializable {
    @Column(name = "ANNO", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int anno;
    @Column(name = "GIORNALE", nullable = false, length = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String giornale;
    @Column(name = "PROTOCOLLO", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int protocollo;
    @Column(name = "PROGRPRIMANOTA", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int progrprimanota;

    public int getAnno() {
        return anno;
    }

    public void setAnno(int anno) {
        this.anno = anno;
    }

    public String getGiornale() {
        return giornale;
    }

    public void setGiornale(String giornale) {
        this.giornale = giornale;
    }

    public int getProtocollo() {
        return protocollo;
    }

    public void setProtocollo(int protocollo) {
        this.protocollo = protocollo;
    }

    public int getProgrprimanota() {
        return progrprimanota;
    }

    public void setProgrprimanota(int progrprimanota) {
        this.progrprimanota = progrprimanota;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimanotaPK that = (PrimanotaPK) o;

        if (anno != that.anno) return false;
        if (protocollo != that.protocollo) return false;
        if (progrprimanota != that.progrprimanota) return false;
        if (giornale != null ? !giornale.equals(that.giornale) : that.giornale != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = anno;
        result = 31 * result + (giornale != null ? giornale.hashCode() : 0);
        result = 31 * result + protocollo;
        result = 31 * result + progrprimanota;
        return result;
    }
}
