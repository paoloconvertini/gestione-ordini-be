package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FiltroPrimanota implements Serializable {

    String giornale;
    Integer anno;
    Integer protocollo;
}
