package it.calolenoci.resource;

import io.quarkus.security.Authenticated;
import it.calolenoci.dto.DashboardDto;
import it.calolenoci.dto.FiltroOrdini;
import it.calolenoci.service.OrdineService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ForbiddenException;
import it.calolenoci.dto.OrdineNotaDaOrdinareDto;
import java.util.List;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.text.ParseException;

@Path("api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@RequestScoped
public class DashboardResource {
    @Inject
    OrdineService service;

    @Inject
    JsonWebToken jwt;

    @GET
    public DashboardDto get() throws ParseException {
        DashboardDto dto = new DashboardDto();
        if (hasPerm("ordini_clienti.view")) {
            dto.setOrdiniAttivi(service.countDashboard(new FiltroOrdini()));
            FiltroOrdini daOrdinare = new FiltroOrdini();
            daOrdinare.setFiltroStatus("DA_ORDINARE");
            dto.setDaOrdinare(service.countDashboard(daOrdinare));
            FiltroOrdini modificati = new FiltroOrdini();
            modificati.setDaOrdinareModificati(true);
            modificati.setFiltroStatus("DA_ORDINARE");
            dto.setDaOrdinareModificati(service.countDashboard(modificati));
            dto.setDaOrdinareConNote(service.countOrdiniDaOrdinareConNote());
            FiltroOrdini carichi = new FiltroOrdini();
            carichi.setCarichiDaProcessare(true);
            dto.setCarichiDaProcessare(service.countDashboard(carichi));
            dto.setCarichiPiuVecchi(service.carichiPiuVecchi(carichi));
            FiltroOrdini senzaBolla = new FiltroOrdini();
            senzaBolla.setSenzaBolla(true);
            dto.setSenzaBolla(service.countDashboard(senzaBolla));
            if (hasPerm("ordini_clienti.warn_non_firmato")) {
                FiltroOrdini senzaFirma = new FiltroOrdini();
                senzaFirma.setFiltroStatus("ARCHIVIATO");
                senzaFirma.setSenzaFirma(true);
                dto.setArchiviatiSenzaFirma(service.countDashboard(senzaFirma));
            }
            if (hasPerm("articoli.codifica_articoli")) {
                FiltroOrdini filtro = new FiltroOrdini();
                filtro.setDaCodificare(true);
                dto.setDaCodificare(service.countDashboard(filtro));
            }
            if (hasPerm("logistica.update_veicolo")) {
                FiltroOrdini filtro = new FiltroOrdini();
                filtro.setConsegneDaProgrammare(true);
                dto.setConsegneDaProgrammare(service.countDashboard(filtro));
            }
            if (hasPerm("ordini_clienti.pronto_consegna")) {
                FiltroOrdini filtro = new FiltroOrdini();
                filtro.setProntoConsegna(true);
                dto.setProntoConsegna(service.countDashboard(filtro));
            }
        }
        return dto;
    }

    @GET
    @Path("/note-da-ordinare")
    public List<OrdineNotaDaOrdinareDto> noteDaOrdinare(@QueryParam("venditore") String venditore) throws ParseException {
        if (!hasPerm("ordini_clienti.view")) throw new ForbiddenException();
        return service.noteArticoliDaOrdinare(venditore);
    }

    @GET
    @Path("/articoli/{tipo}")
    public it.calolenoci.dto.DashboardArticoliDto articoli(@jakarta.ws.rs.PathParam("tipo") String tipo,
            @QueryParam("venditore") String venditore, @QueryParam("pagina") int pagina) throws ParseException {
        if (!hasPerm("ordini_clienti.view")) throw new ForbiddenException();
        String permission = switch (tipo) {
            case "codifica" -> "articoli.codifica_articoli";
            case "pronto" -> "ordini_clienti.pronto_consegna";
            case "programmare" -> "logistica.update_veicolo";
            case "firme" -> "ordini_clienti.warn_non_firmato";
            default -> "ordini_clienti.view";
        };
        if (!hasPerm(permission)) throw new ForbiddenException();
        if (pagina < 0 || pagina > 100000) throw new jakarta.ws.rs.BadRequestException();
        return service.articoliDashboard(tipo, venditore, pagina);
    }

    private boolean hasPerm(String permission) {
        Object permissions = jwt.getClaim("permissions");
        return permissions instanceof JsonArray array && array.stream()
                .filter(JsonString.class::isInstance).map(JsonString.class::cast)
                .anyMatch(value -> permission.equals(value.getString()));
    }
}
