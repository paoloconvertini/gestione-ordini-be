package it.calolenoci.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ConsegnaLiberaDto {
    private String intestazione;
    private String indirizzo;
    private String localita;
    private String telefono;
    private Double latitudine;
    private Double longitudine;
    private LocalDate dataConsegna;
    private Character oraConsegna;
    private Integer veicolo;
}
