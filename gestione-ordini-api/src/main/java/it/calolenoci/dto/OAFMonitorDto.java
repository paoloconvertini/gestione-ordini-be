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
public class OAFMonitorDto implements Serializable {

    private String user;

    private Long totale;


}
