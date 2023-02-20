package it.calolenoci.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class OrdineClienteTestataDto implements Serializable {
    private  Integer anno;
    private  String serie;
    private  Integer progressivo;
    private  Integer gruppoCliente;
    private  String contoCliente;
    //private  Integer gruppoFattura;
    //private  String contoFattura;
    private  String tipoFattura;
    private  Date dataOrdine;
    private  Date dataRichiesta;
    private  String numeroConferma;
    private  Date dataConferma;
    private  Date dataConfermaCli;
    private  String intestazione;
    private  String continuaIntest;
    private  String indirizzo;
    private  String localita;
    private  String cap;
    private  String provincia;
    private  String statoResidenza;
    private  String statoEstero;
    private  String telefono;
    private  String cellulare;
    private  String email;
    private  String pec;
}
