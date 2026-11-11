package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrdineNotaDaOrdinareDto {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String cliente;
    private String status;
    private Integer progrGenerale;
    private Integer rigo;
    private String articolo;
    private String descrizione;
    private String nota;
    private LocalDateTime dataNota;
}
