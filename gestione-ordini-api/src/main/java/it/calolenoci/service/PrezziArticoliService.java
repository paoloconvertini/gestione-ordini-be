package it.calolenoci.service;

import it.calolenoci.dto.AggiornamentoPrezziDto;
import it.calolenoci.dto.PrezzoArticoloDto;
import it.calolenoci.dto.SogliaRicaricoDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.quarkus.hibernate.orm.panache.Panache.getEntityManager;

@ApplicationScoped
public class PrezziArticoliService {
    private static final LocalDate DATA_COSTI_IMPORTATI = LocalDate.of(2021, 12, 31);
    private static final BigDecimal FATTORE_COSTI_IMPORTATI = new BigDecimal("1.40");
    private static final BigDecimal CENTO = new BigDecimal("100");

    public List<PrezzoArticoloDto> cerca(String classeA1, BigDecimal percentuale, boolean includiBloccati) {
        validaClasseA1(classeA1);
        if (percentuale != null) validaPercentuale(percentuale);
        return leggi(classeA1.trim(), percentuale, includiBloccati, false);
    }

    public BigDecimal ricaricoImpostato(String classeA1) {
        validaClasseA1(classeA1);
        return leggiRicarico(classeA1.trim(), false);
    }

    private BigDecimal leggiRicarico(String classeA1, boolean lock) {
        String hint = lock ? " WITH (UPDLOCK, HOLDLOCK)" : "";
        List<?> righe = getEntityManager().createNativeQuery(
                "SELECT PERCENTUALE FROM dbo.GO_RICARICO_FORNITORE" + hint + " WHERE CLASSEA1=:classe")
                .setParameter("classe", classeA1).getResultList();
        return righe.isEmpty() ? null : new BigDecimal(righe.get(0).toString());
    }

    @Transactional
    public void salvaSoglia(SogliaRicaricoDto richiesta, String user) {
        if (richiesta == null) throw new BadRequestException("Richiesta mancante");
        validaClasseA1(richiesta.getClasseA1());
        validaPercentuale(richiesta.getPercentuale());
        String classe = richiesta.getClasseA1().trim();
        BigDecimal corrente = leggiRicarico(classe, true);
        if (!uguali(corrente, richiesta.getRicaricoImpostato())) {
            throw new WebApplicationException("Soglia del fornitore cambiata. Ripetere la ricerca prima di salvare.", 409);
        }
        salvaRicarico(classe, richiesta.getPercentuale(), user);
    }

    private void salvaRicarico(String classeA1, BigDecimal percentuale, String user) {
        int modificati = getEntityManager().createNativeQuery("""
                UPDATE dbo.GO_RICARICO_FORNITORE
                SET PERCENTUALE=:percentuale, UPDATED_AT=GETDATE(), UPDATED_BY=:user
                WHERE CLASSEA1=:classe
                """).setParameter("percentuale", percentuale).setParameter("user", user)
                .setParameter("classe", classeA1).executeUpdate();
        if (modificati == 0) {
            getEntityManager().createNativeQuery("""
                    INSERT INTO dbo.GO_RICARICO_FORNITORE (CLASSEA1,PERCENTUALE,CREATED_BY,UPDATED_BY)
                    VALUES (:classe,:percentuale,:user,:user)
                    """).setParameter("classe", classeA1).setParameter("percentuale", percentuale)
                    .setParameter("user", user).executeUpdate();
        }
    }

    private List<PrezzoArticoloDto> leggi(String classeA1, BigDecimal percentuale, boolean includiBloccati, boolean lock) {
        // In aggiornamento i lock durano fino al commit, anche sui movimenti
        // usati per determinare il costo ultimo e sulle relative causali.
        String articoloLock = lock ? " WITH (UPDLOCK, HOLDLOCK)" : "";
        String letturaLock = lock ? " WITH (HOLDLOCK)" : "";
        String sql = """
                SELECT a.ARTICOLO, a.DESCRARTICOLO, a.UNITAMISURA,
                       CAST(a.BLOCCATO AS varchar(10)), CAST(a.PREZZOBASE AS decimal(28,8)),
                       u.PROGRGENERALE, u.DATAOPMAGAZZINO, u.MUNITA, u.CostoUltimo
                FROM dbo.ARTICOLI_TAB a%s
                OUTER APPLY (
                    SELECT TOP (1) m.PROGRGENERALE, m.DATAOPMAGAZZINO, m.MUNITA,
                           CAST(m.VALORE AS decimal(28,8)) /
                           NULLIF(CAST(m.MQUANTITA AS decimal(28,8)), 0) AS CostoUltimo
                    FROM dbo.MAGAZZINO m%s
                    JOIN dbo.TMMC t%s ON t.MCAUSALE = m.MCAUSALE
                    WHERE m.MARTICOLO = a.ARTICOLO
                      AND t.AGGIORNAULTIMO = 'S'
                      AND m.MQUANTITA > 0 AND m.VALORE > 0 AND m.VALOREUNITARIO > 0
                      AND m.DATAOPMAGAZZINO <= GETDATE()
                    ORDER BY m.DATAOPMAGAZZINO DESC, m.PROGRGENERALE DESC
                ) u
                WHERE LTRIM(RTRIM(a.CLASSEA1)) = :classeA1
                ORDER BY a.ARTICOLO
                """.formatted(articoloLock, letturaLock, letturaLock);
        @SuppressWarnings("unchecked")
        List<Object[]> righe = getEntityManager().createNativeQuery(sql)
                .setParameter("classeA1", classeA1.trim()).getResultList();
        List<PrezzoArticoloDto> risultato = new ArrayList<>();
        for (Object[] r : righe) {
            PrezzoArticoloDto dto = new PrezzoArticoloDto();
            dto.setArticolo((String) r[0]);
            dto.setDescrizione((String) r[1]);
            dto.setUnitaMisura((String) r[2]);
            dto.setBloccato((String) r[3]);
            dto.setPrezzoBase(decimale(r[4]));
            dto.setMovimento(r[5] == null ? null : ((Number) r[5]).intValue());
            dto.setDataUltimoAcquisto((LocalDateTime) r[6]);
            dto.setUnitaAcquisto((String) r[7]);
            dto.setCostoUltimo(decimale(r[8]));
            prepara(dto, percentuale, includiBloccati);
            risultato.add(dto);
        }
        return risultato;
    }

    private void prepara(PrezzoArticoloDto dto, BigDecimal percentuale, boolean includiBloccati) {
        // Rettifica solo il costo di riferimento: il movimento originale non viene modificato.
        dto.setCostoRettificato(dto.getCostoUltimo() != null && dto.getDataUltimoAcquisto() != null
                && DATA_COSTI_IMPORTATI.equals(dto.getDataUltimoAcquisto().toLocalDate()));
        dto.setCostoRiferimento(dto.isCostoRettificato()
                ? dto.getCostoUltimo().multiply(FATTORE_COSTI_IMPORTATI) : dto.getCostoUltimo());
        if (dto.getCostoUltimo() != null && dto.getCostoUltimo().signum() > 0
                && dto.getPrezzoBase() != null && stessaUnita(dto)) {
            dto.setRicaricoAttuale(dto.getPrezzoBase().divide(dto.getCostoRiferimento(), 10, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE).multiply(CENTO).setScale(2, RoundingMode.HALF_UP));
        }
        if (dto.getCostoUltimo() == null || dto.getCostoUltimo().signum() <= 0) {
            dto.setMotivoEsclusione("Costo ultimo non disponibile");
        } else if (!stessaUnita(dto)) {
            dto.setMotivoEsclusione("Unità di misura da verificare");
        } else if (!includiBloccati && ("S".equalsIgnoreCase(trim(dto.getBloccato()))
                || "N".equalsIgnoreCase(trim(dto.getBloccato())))) {
            dto.setMotivoEsclusione("Articolo bloccato");
        }
        dto.setAggiornabile(dto.getMotivoEsclusione() == null);
        if (dto.isAggiornabile() && percentuale != null) {
            dto.setNuovoPrezzo(calcolaPrezzo(dto.getCostoRiferimento(), percentuale));
        }
    }

    @Transactional
    public int aggiorna(AggiornamentoPrezziDto richiesta, String user) {
        if (richiesta == null) throw new BadRequestException("Richiesta mancante");
        validaClasseA1(richiesta.getClasseA1());
        validaPercentuale(richiesta.getPercentuale());
        if (richiesta.getArticoli() == null || richiesta.getArticoli().isEmpty()) {
            throw new BadRequestException("Nessun articolo da aggiornare");
        }
        List<PrezzoArticoloDto> deselezionati = richiesta.getArticoliDeselezionati() == null
                ? List.of() : richiesta.getArticoliDeselezionati();
        if (!deselezionati.isEmpty()) {
            validaPercentuale(richiesta.getPercentualeDeselezionati());
        } else if (richiesta.getPercentualeDeselezionati() != null) {
            throw new BadRequestException("Nessun articolo per la seconda percentuale");
        }
        // Lock anche sulla chiave assente: un solo aggiornamento per fornitore alla volta.
        BigDecimal soglia = leggiRicarico(richiesta.getClasseA1().trim(), true);
        if (!uguali(soglia, richiesta.getRicaricoImpostato())) {
            throw new WebApplicationException("Ricarico del fornitore cambiato. Ripetere la ricerca prima di aggiornare.", 409);
        }
        if (richiesta.isPortaASoglia() && (soglia == null
                || !uguali(soglia, richiesta.getPercentuale()) || !deselezionati.isEmpty())) {
            throw new BadRequestException("Porta a soglia richiede la soglia salvata e un solo gruppo di articoli");
        }
        List<PrezzoArticoloDto> correnti = leggi(richiesta.getClasseA1().trim(), null, richiesta.isIncludiBloccati(), true);
        Map<String, PrezzoArticoloDto> perCodice = correnti.stream()
                .collect(Collectors.toMap(PrezzoArticoloDto::getArticolo, Function.identity()));
        Set<String> codici = new HashSet<>();
        validaArticoli(richiesta.getArticoli(), perCodice, codici);
        validaArticoli(deselezionati, perCodice, codici);
        if (richiesta.isPortaASoglia()) {
            for (PrezzoArticoloDto articolo : richiesta.getArticoli()) {
                BigDecimal ricarico = perCodice.get(articolo.getArticolo()).getRicaricoAttuale();
                if (ricarico == null || ricarico.subtract(soglia).setScale(0, RoundingMode.HALF_UP).signum() >= 0) {
                    throw new BadRequestException("Articolo non sotto soglia: " + articolo.getArticolo());
                }
            }
        }
        // Entrambi i gruppi sono validati prima di qualsiasi modifica.
        // Prima i deselezionati con il secondo ricarico, poi i selezionati
        // con quello principale, nella stessa transazione.
        Map<String, BigDecimal> percentuali = new LinkedHashMap<>();
        deselezionati.forEach(a -> percentuali.put(a.getArticolo(), richiesta.getPercentualeDeselezionati()));
        richiesta.getArticoli().forEach(a -> percentuali.put(a.getArticolo(), richiesta.getPercentuale()));
        for (Map.Entry<String, BigDecimal> voce : percentuali.entrySet()) {
            PrezzoArticoloDto corrente = perCodice.get(voce.getKey());
            int modificati = getEntityManager().createNativeQuery("""
                    UPDATE dbo.ARTICOLI_TAB
                    SET PREZZOBASE = :prezzo, SYS_UPDATEDATE = GETDATE(), SYS_UPDATEUSER = :user
                    WHERE ARTICOLO = :articolo
                    """).setParameter("prezzo", calcolaPrezzo(corrente.getCostoRiferimento(), voce.getValue()))
                    .setParameter("user", user)
                    .setParameter("articolo", voce.getKey()).executeUpdate();
            if (modificati != 1) throw new WebApplicationException("Articolo modificato durante l'aggiornamento", 409);
        }
        return codici.size();
    }

    private void validaArticoli(List<PrezzoArticoloDto> articoli,
                                Map<String, PrezzoArticoloDto> perCodice, Set<String> codici) {
        for (PrezzoArticoloDto visto : articoli) {
            if (visto == null || visto.getArticolo() == null || !codici.add(visto.getArticolo())) {
                throw new BadRequestException("Elenco articoli non valido o duplicato");
            }
            PrezzoArticoloDto corrente = perCodice.get(visto.getArticolo());
            if (corrente == null || !corrente.isAggiornabile()
                    || !uguali(corrente.getPrezzoBase(), visto.getPrezzoBase())
                    || !uguali(corrente.getCostoUltimo(), visto.getCostoUltimo())
                    || !Objects.equals(corrente.getMovimento(), visto.getMovimento())
                    || !Objects.equals(corrente.getDataUltimoAcquisto(), visto.getDataUltimoAcquisto())) {
                throw new WebApplicationException("Dati cambiati per " + visto.getArticolo()
                        + ". Ripetere la ricerca prima di aggiornare.", 409);
            }
        }
    }

    static BigDecimal calcolaPrezzo(BigDecimal costo, BigDecimal percentuale) {
        return costo.multiply(BigDecimal.ONE.add(percentuale.divide(CENTO)))
                .setScale(3, RoundingMode.HALF_UP);
    }

    private static void validaClasseA1(String classeA1) {
        if (classeA1 == null || !classeA1.trim().matches("[A-Za-z0-9]{1,3}")) {
            throw new BadRequestException("Selezionare una classificazione fornitore A1 valida");
        }
    }

    private static void validaPercentuale(BigDecimal percentuale) {
        if (percentuale == null || percentuale.signum() < 0 || percentuale.compareTo(new BigDecimal("10000")) > 0
                || percentuale.stripTrailingZeros().scale() > 4) {
            throw new BadRequestException("Inserire una percentuale tra 0 e 10000, con massimo quattro decimali");
        }
    }

    private static boolean stessaUnita(PrezzoArticoloDto dto) {
        return !trim(dto.getUnitaMisura()).isEmpty()
                && trim(dto.getUnitaMisura()).equalsIgnoreCase(trim(dto.getUnitaAcquisto()));
    }

    private static String trim(String valore) {
        return valore == null ? "" : valore.trim();
    }

    private static BigDecimal decimale(Object valore) {
        return valore == null ? null : new BigDecimal(valore.toString()).setScale(6, RoundingMode.HALF_UP);
    }

    private static boolean uguali(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }
}
