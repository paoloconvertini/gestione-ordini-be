package it.calolenoci.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.EmailDto;
import it.calolenoci.dto.ResponseDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.File;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Path("api/v1/mail")
public class MailingResource {

    private static void accept(Throwable failure) {
        Response.ok(new ResponseDto("errore invio mail", true)).build();
    }

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance ordine(String name);
    }

    @POST
    @Produces(APPLICATION_JSON)
    public Uni<Response> send(EmailDto dto) {
        File f = new File("ordine_" + dto.getAnno() + "_" + dto.getSerie() + "_" + dto.getProgressivo() + ".pdf");
        Mail m = new Mail();
        m.setSubject("Ordine confermato!");
        m.addTo(dto.getTo());
        m.addAttachment(f.getName(), f, "application/pdf");
        return Templates.ordine("Paolo Convertini")
                .mail(m)
                .send()
                .map(a -> Response.ok(new ResponseDto("Mail inviata correttamente",false)).build())
                .onFailure()
                .recoverWithItem(Response.serverError().build());
    }

}
