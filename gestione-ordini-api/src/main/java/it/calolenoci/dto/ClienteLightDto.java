package it.calolenoci.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteLightDto {

    private String codiceCliente;
    private String nome;
}
