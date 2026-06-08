package it.calolenoci.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutlookEventDto {

    private String subject;

    private String description;

    private String location;

    private LocalDateTime start;

    private LocalDateTime end;
}