package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineReportDto;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrdineClienteReportMapper {

    public OrdineReportDto fromEntityToDto (OrdineDTO ordineDTO, OrdineDettaglioDto a, String filename) {
        OrdineReportDto dto = new OrdineReportDto();
        dto.setANNO(a.getAnno());
        dto.setSERIE(a.getSerie());
        dto.setPROGRESSIVO(a.getProgressivo());
        dto.setRIGO(a.getRigo());
        dto.setFARTICOLO(a.getFArticolo());
        dto.setCODARTFORNITORE(a.getCodArtFornitore());
        dto.setFDESCRARTICOLO(a.getFDescrArticolo());
        dto.setQUANTITA(a.getQuantita());
        dto.setPREZZO(a.getPrezzo());
        dto.setFUNITAMISURA(a.getFUnitaMisura());
        dto.setSCONTOARTICOLO(5f);
        dto.setINTESTAZIONE(ordineDTO.getIntestazione());
        dto.setINDIRIZZO(ordineDTO.getIndirizzo());
        dto.setLOCALITA(ordineDTO.getLocalita());
        dto.setCAP(ordineDTO.getCap());
        dto.setPROVINCIA(ordineDTO.getProvincia());
        dto.setTELEFONO(ordineDTO.getTelefono());
        dto.setFCOLLI(a.getFColli());
        dto.setFILENAME(filename);
        dto.setDATAORDINE(ordineDTO.getDataOrdine());
        dto.setDATACONFERMA(ordineDTO.getDataConferma());
        dto.setNUMEROCONFERMA(ordineDTO.getNumeroConferma());
        return dto;
    }
}
