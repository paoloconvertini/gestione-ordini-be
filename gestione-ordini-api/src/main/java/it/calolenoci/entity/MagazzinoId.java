 package it.calolenoci.entity;

import lombok.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.conversions.IntegerConversion;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MagazzinoId implements Serializable {

    @Column(name = "ANNO")
    private Integer anno;

    @Column(length = 3, name = "SERIEMAGAZZINO")
    private String serie;

    @Column(name = "PROGRESSIVOMAG")
    private Integer progressivo;

    @Column(length = 255, name = "FLAGCONTROMAG")
    private String flagControMag;

    @Column(name = "RIGO")
    private Integer rigo;



}
