package it.calolenoci.mapper;

import it.calolenoci.dto.OrdineDTO;
import it.calolenoci.dto.OrdineDettaglioDto;
import it.calolenoci.dto.OrdineReportDto;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrdineClienteReportMapper {

    public OrdineReportDto fromEntityToDto(OrdineDTO ordineDTO, OrdineDettaglioDto a, String filename) {
        OrdineReportDto dto = new OrdineReportDto();
        dto.setTIPORIGO(a.getTipoRigo());
        if (!"C".equals(a.getTipoRigo())) {
            dto.setFCOLLI(a.getFColli());
            dto.setQUANTITA(a.getQuantita());
            dto.setPREZZO(a.getPrezzo());
            dto.setFUNITAMISURA(a.getFUnitaMisura());
            if (dto.getQUANTITA() != null && dto.getPREZZO() != null) {
                dto.setValoreTotale(calcolaValoreTotale(a));
            } else {
                dto.setValoreTotale(0D);
            }
            if(a.getScontoArticolo() != null && a.getScontoArticolo() != 0) {
                dto.setSCONTOARTICOLO(a.getScontoArticolo());
            }
            if(a.getScontoC1() != null && a.getScontoC1() != 0) {
                dto.setSCONTOC1(a.getScontoC1());
            }
            if(a.getScontoC2() != null && a.getScontoC2() != 0) {
                dto.setSCONTOC2(a.getScontoC2());
            }
            if(a.getScontoP() != null && a.getScontoP() != 0) {
                dto.setSCONTOP(a.getScontoP());
            }
            dto.setFCODICEIVA(a.getFCodiceIva());
        }

        dto.setANNO(a.getAnno());
        dto.setSERIE(a.getSerie());
        dto.setPROGRESSIVO(a.getProgressivo());
        dto.setRIGO(a.getRigo());
        dto.setFARTICOLO(a.getFArticolo());
        dto.setCODARTFORNITORE(a.getCodArtFornitore());
        dto.setFDESCRARTICOLO(a.getFDescrArticolo());
        dto.setINTESTAZIONE(ordineDTO.getIntestazione());
        dto.setINDIRIZZO(ordineDTO.getIndirizzo());
        dto.setLOCALITA(ordineDTO.getLocalita());
        dto.setCAP(ordineDTO.getCap());
        dto.setPROVINCIA(ordineDTO.getProvincia());
        dto.setTELEFONO(ordineDTO.getTelefono());
        dto.setFILENAME(filename);
        dto.setDATAORDINE(ordineDTO.getDataOrdine());
        dto.setDATACONFERMA(ordineDTO.getDataConferma());
        dto.setNUMEROCONFERMA(ordineDTO.getNumeroConferma());
        return dto;
    }

    private Double calcolaValoreTotale(OrdineDettaglioDto dto) {
        Double valoreTotale;
        valoreTotale = dto.getQuantita() * dto.getPrezzo();
        if (dto.getScontoArticolo() != null && dto.getScontoArticolo() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoArticolo() / 100);
        }
        if (dto.getScontoC1() != null && dto.getScontoC1() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoC1() / 100);
        }
        if (dto.getScontoC2() != null && dto.getScontoC2() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoC2() / 100);
        }
        if (dto.getScontoP() != null && dto.getScontoP() != 0) {
            valoreTotale -= (valoreTotale * dto.getScontoP() / 100);
        }

        return valoreTotale;
    }
}
