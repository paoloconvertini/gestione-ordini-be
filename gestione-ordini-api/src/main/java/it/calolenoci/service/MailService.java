package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.Mailer;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.*;
import it.calolenoci.resource.MailingResource;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class MailService {


    @ConfigProperty(name = "email.to")
    String to;

    @Inject
    Mailer mailer;

    public Uni<Response> send(MailTemplate.MailTemplateInstance template, MailAttachment attachment, String subject, InlineAttachment inlineAttachment, String... to) {
        try {
            Mail m = new Mail();
            if (inlineAttachment != null) {
                m.addInlineAttachment(inlineAttachment.getName(), inlineAttachment.getFile(),
                        inlineAttachment.getContentType(), inlineAttachment.getContentId());
            }
            m.setSubject(subject);
            m.addTo(to);
            if (attachment != null) {
                m.addAttachment(attachment.getName(), attachment.getFile(), attachment.getContentType());
            }
            Uni<Response> uni = template
                    .mail(m)
                    .send()
                    .map(a -> Response.ok(new ResponseDto("Mail inviata correttamente", false)).build())
                    .onFailure()
                    .recoverWithItem(a -> {
                        Log.error("Errore invio mail: " + a);
                        return Response.serverError().build();
                    });
            Log.debug("Invio corretto!");
            return uni;
        } catch (Exception e) {
            Log.error("Errore invio mail", e);
            return null;
        }
    }

    public void sendMailOrdineCompleto(File f, OrdineDTO dto) {
        try {
            String body = "<div style=\"font-family:Arial,sans-serif\">" +
                          " <span>L'ordine n. " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo() + " è completo e quindi è pronto per essere consegnato.</span> " +
                          " <ul>Di seugito i riferimenti del cliente: " +
                          "    <li>Nome: " + (StringUtils.isNotBlank(dto.getIntestazione())?dto.getIntestazione():"") + "</li>" +
                          "    <li>Luogo: " + (StringUtils.isNotBlank(dto.getLocalita())?dto.getLocalita():"") + (StringUtils.isNotBlank(dto.getProvincia())?"("+dto.getProvincia()+")":"") + "</li>" +
                          "    <li>Telefono: " + (StringUtils.isNotBlank(dto.getTelefono())?dto.getTelefono(): "") + "</li>" +
                          "    <li>Email: " + (StringUtils.isNotBlank(dto.getEmail())?dto.getEmail(): "") + "</li>"     +
                          " </ul>" +
                          "</div>";
            MailAttachment attachment = new MailAttachment(f.getName(), f, "application/pdf");
            String[] split = to.split(",");
            List<String> strings = new ArrayList<>(Arrays.stream(split).toList());
            strings.remove(0);
            String [] result = strings.toArray(new String[0]);
            mailer.send(Mail.withHtml(split[0], "Ordine completo!", body)
                    .addTo(result)
                    .addAttachment(attachment.getName(), attachment.getFile(), attachment.getContentType()));
        } catch (Exception e) {
            Log.error("Errore invio mail", e);
        }
    }


}
