package it.calolenoci.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

@RegisterForReflection
@Data
@NoArgsConstructor
public class UserResponseDTO {

    private String fullname;
    private String email;
    private String codVenditore;


    public UserResponseDTO(String fullname, String email, String codVenditore) {
        this.fullname = fullname;
        this.email = email;
        this.codVenditore = codVenditore;
    }
}

