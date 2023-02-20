package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


@Entity
@Table(name = "ORDCLI2")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrdineCliente implements Serializable {

    @EmbeddedId
    OrdineDettaglioId ordineDettaglioId;

    @Column(length = 2)
    private String tipoRigo;

    @Column(length = 13)
    private String fArticolo;

    @Column(length = 25)
    private String codArtFornitore;

    @Column(length = 50)
    private String fDescrArticolo;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConfConsegna;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRichConsegna;

    @Column
    private Float quantita;

    @Column
    private Float quantitaV;

    @Column
    private Float quantita2;

    @Column
    private Float prezzo;

    @Column(length = 2)
    private String fUnitaMisura;

    @Column(name="GE_UPDATEDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date geUpdateDate;

    @Column(length = 500, name = "GE_UPDATEUSER")
    private String geUpdateUser;

    @Column(length = 1, name = "GE_FLAG_RISERVATO")
    private Character geFlagRiservato;

    @Column(length = 1, name = "GE_FLAG_NON_DISPONIBILE")
    private Character geFlagNonDisponibile;

    @Column(length = 1, name = "GE_FLAG_ORDINATO")
    private Character geFlagOrdinato;

    @Column(length = 20, name="GE_TONO")
    private String geTono;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdineCliente that = (OrdineCliente) o;
        return Objects.equals(ordineDettaglioId, that.ordineDettaglioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordineDettaglioId);
    }
}
