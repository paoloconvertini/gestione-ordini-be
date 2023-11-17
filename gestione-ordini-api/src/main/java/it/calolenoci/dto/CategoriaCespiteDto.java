package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link it.calolenoci.entity.CategoriaCespite} entity
 */
@Data
@RegisterForReflection
public class CategoriaCespiteDto implements Serializable {

    private String tipoCespite;
    private String descrizione;
    private Integer costoGruppo;
    private String costoConto;

    public CategoriaCespiteDto(String tipoCespite, String descrizione, Integer costoGruppo, String costoConto) {
        this.tipoCespite = tipoCespite;
        this.descrizione = descrizione;
        this.costoGruppo = costoGruppo;
        this.costoConto = costoConto;
    }

    public CategoriaCespiteDto() {
    }

    public CategoriaCespiteDto(String tipoCespite, String descrizione) {
        this.tipoCespite = tipoCespite;
        this.descrizione = descrizione;
    }
}