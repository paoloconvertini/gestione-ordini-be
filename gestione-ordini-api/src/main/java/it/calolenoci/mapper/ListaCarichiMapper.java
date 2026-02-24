package it.calolenoci.mapper;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.calolenoci.dto.ListaCarichiDto;
import it.calolenoci.entity.Deposito;
import it.calolenoci.entity.ListaCarichi;
import it.calolenoci.entity.RegistroAzioni;
import it.calolenoci.entity.Trasportatore;
import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Comparator;
import java.util.Date;


@ApplicationScoped
public class ListaCarichiMapper {

    public ListaCarichi fromDtoToEntity(ListaCarichiDto dto) {
        ListaCarichi listaCarichi = new ListaCarichi();

        Long id = ListaCarichi.find("select isnull(max(id), 0) from ListaCarichi")
                .project(Long.class).firstResult();
        listaCarichi.setId(id+1);
        if(StringUtils.isNotBlank(dto.getAzienda())){
            listaCarichi.setAzienda(dto.getAzienda());
        }
        if(StringUtils.isNotBlank(dto.getNumeroOrdine())){
            listaCarichi.setNumeroOrdine(dto.getNumeroOrdine());
        }
        if(dto.getPeso() != null){
            listaCarichi.setPeso(dto.getPeso());
        }
        if(dto.getDataDisponibile() != null){
            listaCarichi.setDataDisponibile(dto.getDataDisponibile());
        }
        if(dto.getIdDeposito() != null) {
            listaCarichi.setDeposito(dto.getIdDeposito());
        }
        if(dto.getIdTrasportatore() != null) {
            listaCarichi.setTrasportatore(dto.getIdTrasportatore());
        }
        return listaCarichi;
    }
}
