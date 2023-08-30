package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.Mailer;
import it.calolenoci.dto.*;
import it.calolenoci.enums.StatoOrdineEnum;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class MailService {


    @Inject
    Mailer mailer;

    @Inject
    OrdineService ordineService;

    @RestClient
    @Inject
    UserService userService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public Response send(MailTemplate.MailTemplateInstance template, MailAttachment attachment, String subject, InlineAttachment inlineAttachment, String... to) {
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
            template
                    .mail(m)
                    .send()
                    .map(a -> Response.ok(new ResponseDto("Mail inviata correttamente", false)).build())
                    .onFailure()
                    .recoverWithItem(a -> {
                        Log.error("Errore invio mail: " + a);
                        return Response.serverError().build();
                    });
            Log.debug("Invio corretto!");
            return Response.ok(new ResponseDto("Mail inviata correttamente", false)).build();
        } catch (Exception e) {
            Log.error("Errore invio mail", e);
            return Response.serverError().build();
        }
    }

    public void sendMailOrdineCompleto(File f, OrdineDTO dto, String email) {
        try {
            String body = "<div style=\"font-family:Arial,sans-serif\">" +
                    " <span>L'ordine n. " + dto.getAnno() + "/" + dto.getSerie() + "/" + dto.getProgressivo() + " è completo e quindi è pronto per essere consegnato.</span> " +
                    " <ul>Di seugito i riferimenti del cliente: " +
                    "    <li>Nome: " + (StringUtils.isNotBlank(dto.getIntestazione()) ? dto.getIntestazione() : "") + "</li>" +
                    "    <li>Luogo: " + (StringUtils.isNotBlank(dto.getLocalita()) ? dto.getLocalita() : "") + (StringUtils.isNotBlank(dto.getProvincia()) ? "(" + dto.getProvincia() + ")" : "") + "</li>" +
                    "    <li>Telefono: " + (StringUtils.isNotBlank(dto.getTelefono()) ? dto.getTelefono() : "") + "</li>" +
                    "    <li>Email: " + (StringUtils.isNotBlank(dto.getEmail()) ? dto.getEmail() : "") + "</li>" +
                    " </ul>" +
                    "</div>";
            MailAttachment attachment = new MailAttachment(f.getName(), f, "application/pdf");
            mailer.send(Mail.withHtml(email, "Ordine completo!", body)
                    .addAttachment(attachment.getName(), attachment.getFile(), attachment.getContentType()));
        } catch (Exception e) {
            Log.error("Errore invio mail", e);
        }
    }


    public void invioMailOrdini() {
        try {
            final List<UserResponseDTO> venditori = userService.getVenditori();
            for (UserResponseDTO v : venditori) {
                StringBuilder body;
                FiltroOrdini filtro = new FiltroOrdini();
                filtro.setCodVenditore(v.getCodVenditore());
                List<String> stati = new ArrayList<>();
                stati.add(StatoOrdineEnum.COMPLETO.getDescrizione());
                filtro.setStati(stati);
                List<OrdineDTO> ordini = ordineService.findAllByStati(filtro);
                if (ordini.isEmpty()) {
                    continue;
                }
                body = new StringBuilder("<table style=\"font-family:Arial,sans-serif\"> " +
                        "        <thead>" +
                        "         <tr>" +
                        "          <th>Ordine n.</th>" +
                        "          <th>Cliente</th>" +
                        "          <th>Data</th>" +
                        "          <th>Luogo</th>" +
                        "          <th>Stato Ord.</th>" +
                        "         </tr>" +
                        "        </thead>" +
                        "        <tbody>");
                for (OrdineDTO dto : ordini) {
                    body.append("<tr>").append("<td>").append(dto.getAnno()).append("/").append(dto.getSerie()).append("/").append(dto.getProgressivo()).append("</td>");
                    body.append("<td>").append(StringUtils.isNotBlank(dto.getIntestazione()) ? dto.getIntestazione() : "").append("</td>");
                    body.append("<td>").append(sdf.format(dto.getDataConferma())).append("</td>");
                    body.append("<td>").append(StringUtils.isNotBlank(dto.getLocalita()) ? dto.getLocalita() : "").append(StringUtils.isNotBlank(dto.getProvincia()) ? " (" + dto.getProvincia() + ")" : "").append("</td>");
                    body.append("<td>").append(dto.getStatus()).append("</td>").append("</tr>");
                }

                body.append("</tbody></table>");
                mailer.send(Mail.withHtml(v.getEmail(), "Lista Ordini completi!", body.toString()));
            }

        } catch (Exception e) {
            Log.error("Errore invio mail", e);
        }

    }
}
