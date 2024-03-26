package it.calolenoci.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.panache.common.Parameters;
import io.quarkus.qute.CheckedTemplate;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.dto.InlineAttachment;
import it.calolenoci.dto.MailAttachment;
import it.calolenoci.dto.PianoContiDto;
import it.calolenoci.entity.PianoConti;
import it.calolenoci.enums.Ruolo;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.AmmortamentoCespiteService;
import it.calolenoci.service.MailService;
import it.calolenoci.service.PianoContiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Path("api/mail")
@RequestScoped
public class MailingResource {

    @Inject
    @Claim(standard = Claims.full_name)
    String fullName;

    @ConfigProperty(name = "ordini.path")
    String pathReport;

    @ConfigProperty(name = "logo.path")
    String logoPath;

    @Inject
    MailService service;

    @Inject
    PianoContiService pianoContiService;

    @Inject
    FetchScheduler scheduler;

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance ordine(String venditore, Integer anno, String serie, Integer progressivo);
    }

    @POST
    @Produces(APPLICATION_JSON)
    @PermitAll
    @Path("/test/{d}")
    public Response test(String d) {
        Optional<PianoContiDto> optional = PianoConti.find("SELECT gruppoConto, sottoConto, indirizzo, localita, cap  " +
                "FROM PianoConti " +
                "WHERE gruppoConto = 1231 AND sottoConto =:sottoConto", Parameters.with("sottoConto", d)).project(PianoContiDto.class).firstResultOptional();
        optional.ifPresent(pianoContiDto -> scheduler.updateLatLon(pianoContiDto));
        return Response.ok().build();
    }

    @POST
    @Produces(APPLICATION_JSON)
    @RolesAllowed({Ruolo.ADMIN, Ruolo.VENDITORE})
    @Path("/confermato")
    public Response send(EmailDto dto) {
        File f = new File(pathReport + "/" + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getSottoConto() + "_" + dto.getAnno() + "_" + dto.getSerie() + "_" + dto.getProgressivo() + ".pdf");
        MailAttachment attachment = new MailAttachment(f.getName(), f, "application/pdf");
        String subject = "Ordine confermato!";
        InlineAttachment inlineAttach = new InlineAttachment("logo.jpg", new File(logoPath + "/logo.jpg"),
                "image/jpg", "<logo@calolenoci>");
        MailTemplate.MailTemplateInstance ordine = Templates.ordine(fullName, dto.getAnno(), dto.getSerie(), dto.getProgressivo());
        if(dto.isUpdate()){
            pianoContiService.update(dto);
        }
        return service.send(ordine, attachment, subject, inlineAttach, dto.getTo());
    }

}
