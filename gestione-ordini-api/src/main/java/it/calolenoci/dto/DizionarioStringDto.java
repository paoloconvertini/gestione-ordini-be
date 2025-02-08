package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@RegisterForReflection
public class DizionarioStringDto implements Serializable {
    private String id;

    private String nome;

    public DizionarioStringDto(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public DizionarioStringDto() {
    }
}
