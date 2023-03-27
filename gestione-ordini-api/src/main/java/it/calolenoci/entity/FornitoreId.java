package it.calolenoci.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class FornitoreId implements Serializable {

    @Column(length = 4, name = "ANNOOAF")
    private  Integer anno;

    @Column(length = 3, name = "SERIEOAF")
    private String serie;

    @Column(name="PROGRESSIVOOAF")
    private Integer progressivo;


}
