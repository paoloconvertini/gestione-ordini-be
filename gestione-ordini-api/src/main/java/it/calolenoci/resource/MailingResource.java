package it.calolenoci.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.EmailDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.File;
import it.calolenoci.dto.ResponseDto;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Consumes(APPLICATION_JSON)
@Path("api/v1/mail")
public class MailingResource {

    @CheckedTemplate
    static class Templates {
        public static native MailTemplate.MailTemplateInstance ordine(String name);
    }

    @POST
    public Response send(EmailDto dto) {
        File f = new File("ordine_" + dto.getAnno() + "_" + dto.getSerie() + "_" + dto.getProgressivo() + ".pdf");
        Mail m = new Mail();
        m.setSubject("Ordine confermato!");
        m.addTo(dto.getTo());
        m.addAttachment(f.getName(), f, "application/pdf");
        Uni<Void> uni = Templates.ordine("Paolo Convertini")
                .mail(m)
                .send();
        uni.subscribe().with(result -> {
            //return Response.ok(new ResponseDto("errore invio mail", true)).build();
        }, failure -> {}
        );
        return Response.ok(new ResponseDto("errore invio mail", true)).build();
    }

}
