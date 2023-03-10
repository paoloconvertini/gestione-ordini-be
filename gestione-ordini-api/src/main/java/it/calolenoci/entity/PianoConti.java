package it.calolenoci.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "PIANOCONTI")
@Getter
@Setter
@IdClass(PianoContiId.class)
public class PianoConti extends PanacheEntityBase {

    @Column
    @Id
    private Integer gruppoConto;

    @Column(length = 6)
    @Id
    private String sottoConto;

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

    public static PianoConti findByGruppoAndSottoConto(Integer gruppo, String sottoConto ) {
      return find("gruppoConto = : gruppo and sottoConto = :sottoConto",
              Parameters.with("gruppo", gruppo).and("sottoConto", sottoConto)).firstResult();

    }
}
