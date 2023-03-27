package it.calolenoci.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
public class MagazzinoId implements Serializable {

    @Column(length = 4, name = "ANNO")
    private  Integer anno;

    @Column(length = 3, name = "SERIEMAGAZZINO")
    private String serie;

    @Column(name="PROGRESSIVOMAG")
    private Integer progressivo;

    @Column
    private String flagControMag;

    @Column
    private Integer rigo;

}
