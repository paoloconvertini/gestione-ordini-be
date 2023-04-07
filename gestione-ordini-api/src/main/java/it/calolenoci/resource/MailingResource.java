package it.calolenoci.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.dto.ResponseDto;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.File;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static it.calolenoci.enums.Ruolo.*;

@Consumes(APPLICATION_JSON)
@Path("api/v1/mail")
public class MailingResource {

    @Inject
    @Claim(standard = Claims.full_name)
    String fullName;

    @ConfigProperty(name = "ordini.path")
    String pathReport;

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance ordine(String venditore, Integer anno, String serie, Integer progressivo);
    }

    @POST
    @Produces(APPLICATION_JSON)
    @RolesAllowed({ADMIN, VENDITORE})
    public Uni<Response> send(EmailDto dto) {
        File f = new File(pathReport + "/ordine_" + dto.getAnno() + "_" + dto.getSerie() + "_" + dto.getProgressivo() + ".pdf");
        Mail m = new Mail();
        m.addInlineAttachment("logo.jpg", new File("src/main/resources/logo.jpg"),
                "image/jpg", "<logo@calolenoci>");
        m.setSubject("Ordine confermato!");
        m.addTo(dto.getTo());
        //m.setFrom("Venditore 1");
        m.addAttachment(f.getName(), f, "application/pdf");
        return Templates.ordine(fullName, dto.getAnno(), dto.getSerie(), dto.getProgressivo())
                .mail(m)
                .send()
                .map(a -> Response.ok(new ResponseDto("Mail inviata correttamente",false)).build())
                .onFailure()
                .recoverWithItem(Response.serverError().build());
    }

}
