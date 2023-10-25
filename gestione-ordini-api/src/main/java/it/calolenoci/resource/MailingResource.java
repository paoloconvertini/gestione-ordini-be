package it.calolenoci.resource;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.dto.InlineAttachment;
import it.calolenoci.dto.MailAttachment;
import it.calolenoci.scheduler.FetchScheduler;
import it.calolenoci.service.MailService;
import it.calolenoci.service.PianoContiService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jfree.util.Log;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static it.calolenoci.enums.Ruolo.ADMIN;
import static it.calolenoci.enums.Ruolo.VENDITORE;
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

    @GET
    @Produces(APPLICATION_JSON)
    @PermitAll
    @Path("/test")
    public Response test() throws IOException, ParseException, InterruptedException, org.jose4j.json.internal.json_simple.parser.ParseException {
        scheduler.calcolaAmmortamentoCespiti();
        return Response.ok().build();
    }

    @POST
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE})
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
