 package it.calolenoci.entity;

 import lombok.*;

 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import java.io.Serializable;

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
