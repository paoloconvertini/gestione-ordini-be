package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
@Getter
@Setter
@ToString
public class ArticoloClasseFornitoreDto implements Serializable {

    private String codice;

    private String descrizione;

    private String descrUser;

    private String descrUser2;

    private String descrUser3;

}
