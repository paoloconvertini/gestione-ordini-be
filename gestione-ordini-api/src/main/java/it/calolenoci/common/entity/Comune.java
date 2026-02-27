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
    @Column(name = "CODICECOMUNE")
    private String codiceIstat;

    @Column(name = "NOMECOMUNE")
    private String nomeComune;

    @Column(name = "PROVCOMUNE")
    private String siglaProvincia;
}