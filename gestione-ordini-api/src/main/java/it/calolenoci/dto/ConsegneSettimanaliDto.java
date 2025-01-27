package it.calolenoci.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ConsegneSettimanaliDto implements Serializable {
    private ConsegnaGiornalieraDto lunedi;
    private ConsegnaGiornalieraDto martedi;
    private ConsegnaGiornalieraDto mercoledi;
    private ConsegnaGiornalieraDto giovedi;
    private ConsegnaGiornalieraDto venerdi;
    private ConsegnaGiornalieraDto sabato;
}
