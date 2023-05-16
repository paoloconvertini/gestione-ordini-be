package it.calolenoci.service;

import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.Mailer;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import it.calolenoci.dto.*;
import it.calolenoci.entity.PianoConti;
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
public class PianoContiService {

    @Transactional
    public void update(EmailDto dto) {
        PianoConti.update("email = :email WHERE gruppoConto = 1231 AND sottoConto = :sottoConto"
                , Parameters.with("email", dto.getTo()).and("sottoConto", dto.getSottoConto()));
    }
}
