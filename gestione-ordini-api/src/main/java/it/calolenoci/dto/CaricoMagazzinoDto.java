package it.calolenoci.dto;

import it.calolenoci.entity.OrdineDettaglio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaricoMagazzinoDto implements Serializable {

    String numDoc;

    LocalDate dataOperazione;

    String causale;

    String vettore;

    List<OrdineDettaglio> articoli;
}
