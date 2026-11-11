package it.calolenoci.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class DashboardDto {
    private Long ordiniAttivi;
    private Long daCodificare;
    private Long daOrdinare;
    private Long daOrdinareModificati;
    private Long daOrdinareConNote;
    private Long prontoConsegna;
    private Long consegneDaProgrammare;
    private Long carichiDaProcessare;
    private Long senzaBolla;
    private Long archiviatiSenzaFirma;
    private List<DashboardCaricoDto> carichiPiuVecchi;
}
