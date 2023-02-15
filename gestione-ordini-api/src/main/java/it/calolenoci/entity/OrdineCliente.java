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
@IdClass(OrdineDettaglioId.class)
@Getter
@Setter
public class OrdineCliente implements Serializable {

    @Column(length = 4)
    @Id
    private Integer anno;

    @Column(length = 3)
    @Id
    private String serie;

    @Column
    @Id
    private Integer progressivo;

    @Column
    @Id
    private Integer rigo;

    @Column(length = 2)
    private String tipoRigo;

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

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date geUpdateDate;

    @Column(length = 500)
    private String geUpdateUser;

    @Column(length = 1)
    private Character geFlagRiservato;

    @Column(length = 1)
    private Character geFlagNonDisponibile;

    @Column(length = 1)
    private Character geFlagOrdinato;

    @Column(length = 20)
    private String geTono;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdineCliente that = (OrdineCliente) o;
        return Objects.equals(anno, that.anno) && Objects.equals(serie, that.serie) && Objects.equals(progressivo, that.progressivo) && Objects.equals(rigo, that.rigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anno, serie, progressivo, rigo);
    }
}
