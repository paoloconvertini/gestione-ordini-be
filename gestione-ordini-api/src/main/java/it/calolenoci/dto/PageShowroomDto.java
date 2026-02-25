package it.calolenoci.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageShowroomDto {

    private long count;
    private List<ShowroomVisitDto> list;

}