package it.calolenoci.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuperUserDTO {

    private Long id;

    private String username;

    private String name;

    private String lastname;

}
