package it.calolenoci.dto;

import it.calolenoci.entity.AmmortamentoCespite;
import it.calolenoci.entity.Cespite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CespiteDBDto implements Serializable {

    private Cespite cespite;

    private AmmortamentoCespite ammortamentoCespite;

    private String categoria;
}
