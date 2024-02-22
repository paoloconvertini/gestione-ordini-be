package it.calolenoci.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@RegisterForReflection
@NoArgsConstructor
@AllArgsConstructor
public class OrdineclienteMonitorDto implements Serializable {

    private Long totOrdNonProcessati;

    private Long totOrdNonDisp;

}
