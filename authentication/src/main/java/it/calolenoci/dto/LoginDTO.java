package it.calolenoci.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String idToken;
    private long expireIn;
    private Boolean error;
}
