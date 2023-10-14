package it.calolenoci.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class GoOrdVeicoloPK implements Serializable {

    @Column(name = "anno", nullable = false)
    private int anno;
    @Column(name = "serie", nullable = false, length = 3)
    private String serie;
    @Column(name = "progressivo", nullable = false)
    private int progressivo;
    @Column(name = "idVeicolo", nullable = false)
    private int idVeicolo;

}
