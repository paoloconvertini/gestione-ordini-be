package it.calolenoci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.ws.rs.core.Response;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private String msg;
    private Boolean error;
    private Response.Status code;

    public ResponseDto(String msg, Boolean error) {
        this.msg = msg;
        this.error = error;
    }
}
