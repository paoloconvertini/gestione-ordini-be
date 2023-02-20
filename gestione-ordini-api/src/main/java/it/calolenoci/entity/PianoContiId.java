package it.calolenoci.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class PianoContiId implements Serializable {

    @Column
    private Integer gruppoConto;

    @Column(length = 6)
    private String sottoConto;

}
