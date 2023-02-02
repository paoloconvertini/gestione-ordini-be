package it.calolenoci.dto;

import java.util.Objects;

public class ErrorResponseDTO {

    private String error;
    private String type;
    private String msg;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(String error, String type, String msg) {
        this.error = error;
        this.type = type;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ErrorResponseDTO{" +
                "error='" + error + '\'' +
                ", type='" + type + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponseDTO that = (ErrorResponseDTO) o;
        return Objects.equals(error, that.error) && Objects.equals(type, that.type) && Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, type, msg);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

