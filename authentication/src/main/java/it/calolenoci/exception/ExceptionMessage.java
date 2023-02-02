package it.calolenoci.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExceptionMessage {
    private boolean error = true;
    private String type;
    private String msg;

    public ExceptionMessage(String type, String msg){
        this.type = type;
        this.msg = msg;
    }

    public static String getErrorMessage(String type, String msg){
        try{
            return new ObjectMapper().writeValueAsString(new ExceptionMessage(type, msg));
        } catch (JsonProcessingException e){
            return "{error:true}";
        }
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
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
