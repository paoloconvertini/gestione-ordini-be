package it.calolenoci.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.EmailDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.File;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Path("api/v1")
public class MailingResource {

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance ordine(String name);
    }

    @POST
    @Path("/mail")
    public Uni<Void> send(EmailDto dto) {
        File f = new File("ordine_" + dto.getAnno() + "_" + dto.getSerie() + "_" + dto.getProgressivo() + ".pdf");
        Mail m = new Mail();
        m.setSubject("Ordine confermato!");
        m.addTo(dto.getTo());
        m.addAttachment(f.getName(), f, "application/pdf");
        return Templates.ordine("Paolo Convertini")
                .mail(m)
                .send();
    }

}
