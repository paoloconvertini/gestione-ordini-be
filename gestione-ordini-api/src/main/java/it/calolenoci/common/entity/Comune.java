package it.calolenoci.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TTCO")
@Getter
@Setter
public class Comune {

    @Id
    @Column(name = "codiceIstat")
    private String codiceIstat;

    @Column(name = "nomeComune")
    private String nomeComune;

    @Column(name = "siglaProvincia")
    private String siglaProvincia;
}