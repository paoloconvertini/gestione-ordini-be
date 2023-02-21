package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@RegisterForReflection
@Getter
@Setter
public class OrdineDTO implements Serializable {

    private  Integer anno;
    private  String serie;
    private  Integer progressivo;
    private Date dataOrdine;
    private  String numeroConferma;
    private  String tipoFattura;
    private  Date dataConferma;
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
