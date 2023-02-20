package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "PIANOCONTI")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PianoConti implements Serializable {

    @EmbeddedId
    PianoContiId pianoContiId;

    @Column(length = 40)
    private String intestazione;

    @Column(length = 40)
    private String continuaIntest;

    @Column(length = 40)
    private String indirizzo;

    @Column(length = 35)
    private String localita;

    @Column(length = 6)
    private String cap;

    @Column(length = 2)
    private String provincia;

    @Column(length = 3)
    private String statoResidenza;

    @Column(length = 3)
    private String statoEstero;

    @Column(length = 30)
    private String telefono;

    @Column(length = 30)
    private String cellulare;

    @Column(length = 800)
    private String email;

    @Column(length = 80)
    private String pec;
}
