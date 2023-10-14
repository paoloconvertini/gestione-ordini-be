package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.FattureDettaglio;
import it.calolenoci.entity.GoTmpScarico;
import it.calolenoci.entity.SaldiMagazzino;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class SaldiMagazzinoDTO {

    private GoTmpScarico goTmpScarico;
    private SaldiMagazzino saldiMagazzino;
    private FattureDettaglio fattureDettaglio;
}
