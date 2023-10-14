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
public class GoTmpScaricoPK implements Serializable {

    @Column(name = "MARTICOLO", nullable = false, length = 13)
    private String marticolo;

    @Column(name = "MMAGAZZINO", nullable = false, length = 3)
    private String mmagazzino;


}
