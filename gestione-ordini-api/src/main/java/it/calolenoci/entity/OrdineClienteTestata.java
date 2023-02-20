package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


@Entity
@Table(name = "ORDCLI")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrdineClienteTestata implements Serializable {


    @EmbeddedId
    OrdineId ordineId;

    @Column
    private Integer gruppoCliente;

    @Column(length = 6)
    private String contoCliente;

    @Column
    private Integer gruppoFattura;

    @Column(length = 6)
    private String contoFattura;

    @Column(length = 1)
    private String tipoFattura;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataOrdine;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRichiesta;

    @Column(length = 15)
    private String numeroConferma;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConferma;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConfermaCli;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdineClienteTestata that = (OrdineClienteTestata) o;
        return Objects.equals(ordineId, that.ordineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordineId);
    }
}
