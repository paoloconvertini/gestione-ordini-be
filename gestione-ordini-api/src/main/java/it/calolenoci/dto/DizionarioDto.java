package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@RegisterForReflection
public class DizionarioDto implements Serializable {
    private Long id;

    private String nome;

    public DizionarioDto(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public DizionarioDto() {
    }
}
