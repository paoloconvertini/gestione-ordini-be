package it.calolenoci.dto;


import io.quarkus.runtime.annotations.RegisterForReflection;
import it.calolenoci.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@RegisterForReflection
@Data
@NoArgsConstructor
public class UserResponseDTO extends SuperUserDTO{

    private String fullname;
    private String email;
    private String codVenditore;
    private Boolean checked;

    private String password;

    private Date dataNascita;

    private List<Role> roles;

    public UserResponseDTO(String fullname, String email, String codVenditore, Boolean checked) {
        this.fullname = fullname;
        this.email = email;
        this.codVenditore = codVenditore;
        this.checked = checked;
    }
}

