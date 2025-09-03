package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class FatturaAccontoIvaView implements Serializable {
    private Integer anno;
    private String serie;
    private Integer progressivo;
    private String fCodiceIva;
    private Double importo;
    private Double importoIvato;
    private List<AccontoDto> acconti = new ArrayList<>();
    private List<DdtNettoDto> ddtList = new ArrayList<>();
    private Double importoResiduo;
    private Double importoResiduoIvato;
    private Double residuoAcconti;
    private Double residuoAccontiIvato;
    private Double residuoFatturabile;
    private Double residuoFatturabileIvato;
    private Double nuovoAcconto;
    private Double nuovoAccontoIvato;


    public FatturaAccontoIvaView(Integer anno, String serie, Integer progressivo, String fCodiceIva, Double importo) {
        this.anno = anno;
        this.serie = serie;
        this.progressivo = progressivo;
        this.fCodiceIva = fCodiceIva;
        this.importo = importo;
    }


}
