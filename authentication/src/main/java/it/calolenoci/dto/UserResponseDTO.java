package it.calolenoci.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private String fullname;
    private String email;
    private String codVenditore;
    private Boolean checked;
}

