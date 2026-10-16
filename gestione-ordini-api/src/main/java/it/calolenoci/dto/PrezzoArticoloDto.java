package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@RegisterForReflection
public class PrezzoArticoloDto {
    private String articolo;
    private String descrizione;
    private BigDecimal giacenzaTotale;
    private List<GiacenzaMagazzinoDto> giacenze = new ArrayList<>();
    private String unitaMisura;
    private String unitaAcquisto;
    private String bloccato;
    private BigDecimal costoUltimo;
    private BigDecimal costoRiferimento;
    private boolean costoRettificato;
    private BigDecimal prezzoBase;
    private BigDecimal ricaricoAttuale;
    private BigDecimal nuovoPrezzo;
    private Integer movimento;
    private LocalDateTime dataUltimoAcquisto;
    private String motivoEsclusione;
    private boolean aggiornabile;
}
