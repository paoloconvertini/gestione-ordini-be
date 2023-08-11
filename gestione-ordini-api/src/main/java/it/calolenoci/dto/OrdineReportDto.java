package it.calolenoci.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrdineReportDto implements Serializable {

    private Integer ANNO;
	private String SERIE;
	private Integer PROGRESSIVO;
	private Integer RIGO;
	private Integer PROGRGENERALE;
	private String TIPORIGO;
	private String FARTICOLO;
	private String CODARTFORNITORE;
	private String FDESCRARTICOLO;
	private Double QUANTITA;
	private Double PREZZO;
	private String FUNITAMISURA;
	private Double SCONTOARTICOLO;
	private Double SCONTOC1;
	private Double SCONTOC2;
	private Double SCONTOP;
	private String FCODICEIVA;
	private String INTESTAZIONE;
	private String INDIRIZZO;
	private String LOCALITA;
	private String CAP;
	private String PROVINCIA;
	private String TELEFONO;
	private String EMAIL;
	private String PARTITAIVA;
	private String CODICEFISCALE;
	private String COGNOME;
	private String NOME;
	private Integer FCOLLI;
	private String FILENAME;
	private String firmaVenditore;
	private String NUMEROCONFERMA;
	private Date DATAORDINE;
	private Date DATACONFERMA;
	private Double valoreTotale;
}
