package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperaioDto implements Serializable {


    private Long id;

    private String nome;

    private Boolean attivo;
}
