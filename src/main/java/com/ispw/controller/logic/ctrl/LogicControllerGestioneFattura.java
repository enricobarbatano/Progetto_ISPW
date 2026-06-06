package com.ispw.controller.logic.ctrl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiFatturaBean;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaRimborso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Fattura;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Prenotazione;

public class LogicControllerGestioneFattura
        implements GestioneFatturaPrenotazione, GestioneFatturaPenalita, GestioneFatturaRimborso {

    /** DAO on-demand (nessun campo, nessuna dipendenza da concreti). */
    private FatturaDAO fatturaDAO() {
        return DAOFactory.getInstance().getFatturaDAO();
    }

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    private PrenotazioneDAO prenotazioneDAO() {
        return DAOFactory.getInstance().getPrenotazioneDAO();
    }

    /** Logger on-demand per evitare campi (stateless). S1312 soppressa localmente. */
    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    // =========================
    // FATTURA PRENOTAZIONE
    // =========================

    @Override
    public Fattura generaFatturaPrenotazione(DatiFatturaBean dati, int idPrenotazione) {
        if (!isValidDati(dati)) {
            log().warning("[FATTURA][WARN] DatiFatturaBean non validi per prenotazione (email mancante)");
            return null;
        }
        if (idPrenotazione <= 0) {
            log().log(Level.WARNING, "[FATTURA][WARN] idPrenotazione non valido: {0}", idPrenotazione);
            return null;
        }

        final LocalDate emissione = (dati.getDataOperazione() != null) ? dati.getDataOperazione() : LocalDate.now(ZoneId.systemDefault());

        final int idUtente = resolveIdUtente(dati);
        if (idUtente <= 0) {
            log().warning("[FATTURA][WARN] Utente non trovato per email");
            return null;
        }

        Fattura f = new Fattura();
        f.setIdPrenotazione(idPrenotazione);
        f.setIdUtente(idUtente);

        // Campo CF: lo salviamo se presente (ma NON lo usiamo per la risoluzione utente)
        f.setCodiceFiscaleCliente(trimOrNull(dati.getCodiceFiscaleCliente()));

        f.setDataEmissione(emissione);
        f.setLinkPdf(buildPdfLink("FATT", idPrenotazione, emissione));

        fatturaDAO().store(f);
        log().log(Level.FINE, "[FATTURA] Emessa fattura prenotazione #{0} -> {1}",
                new Object[]{idPrenotazione, f.getLinkPdf()});
        return f;
    }

    // =========================
    // FATTURA PENALITA'
    // =========================

    @Override
    public Fattura generaFatturaPenalita(DatiFatturaBean dati, int idPenalita) {
        if (!isValidDati(dati)) {
            log().warning("[FATTURA][WARN] DatiFatturaBean non validi per penalita (email mancante)");
            return null;
        }
        if (idPenalita <= 0) {
            log().log(Level.WARNING, "[FATTURA][WARN] idPenalita non valido: {0}", idPenalita);
            return null;
        }

        final int idPrenotazioneFittizio = -Math.abs(idPenalita);
        final LocalDate emissione = (dati.getDataOperazione() != null) ? dati.getDataOperazione() : LocalDate.now(ZoneId.systemDefault());

        final int idUtente = resolveIdUtente(dati);
        if (idUtente <= 0) {
            log().warning("[FATTURA][WARN] Utente non trovato per email (penalita)");
            return null;
        }

        Fattura f = new Fattura();
        f.setIdPrenotazione(idPrenotazioneFittizio);
        f.setIdUtente(idUtente);
        f.setCodiceFiscaleCliente(trimOrNull(dati.getCodiceFiscaleCliente()));
        f.setDataEmissione(emissione);
        f.setLinkPdf(buildPdfLink("PEN", idPrenotazioneFittizio, emissione));

        fatturaDAO().store(f);
        log().log(Level.FINE, "[FATTURA] Emessa fattura penalita #{0} (ref {1}) -> {2}",
                new Object[]{idPenalita, idPrenotazioneFittizio, f.getLinkPdf()});
        return f;
    }

    // =========================
    // NOTA DI CREDITO (RIMBORSO)
    // =========================

    @Override
    public void emettiNotaDiCredito(int idPrenotazione) {
        if (idPrenotazione <= 0) {
            log().log(Level.WARNING, "[FATTURA][WARN] idPrenotazione non valido per NC: {0}", idPrenotazione);
            return;
        }

        final LocalDate oggi = LocalDate.now(ZoneId.systemDefault());

        // Uso API standard del DAO (evita dipendere da metodi non garantiti)
        Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (p == null) {
            log().log(Level.WARNING, "[FATTURA][WARN] Prenotazione non trovata per NC: {0}", idPrenotazione);
            return;
        }

        Fattura nc = new Fattura();
        nc.setIdPrenotazione(idPrenotazione);
        nc.setIdUtente(p.getIdUtente());
        nc.setDataEmissione(oggi);
        nc.setLinkPdf(buildPdfLink("NC", idPrenotazione, oggi));

        fatturaDAO().store(nc);
        log().log(Level.FINE, "[FATTURA] Emetti NC prenotazione #{0} -> {1}",
                new Object[]{idPrenotazione, nc.getLinkPdf()});
    }

    // =========================
    // HELPERS
    // =========================

    /** Validazione semplificata (progetto universitario): serve solo l'email. */
    private boolean isValidDati(DatiFatturaBean dati) {
        return dati != null && hasText(dati.getEmail());
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Link PDF fittizio e deterministico. */
    private String buildPdfLink(String prefix, int ref, LocalDate date) {
        final String d = date.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        return prefix + "-" + Math.abs(ref) + "-" + d + ".pdf";
    }

    /** Risolve id utente SOLO da email (semplificazione progetto). */
    private int resolveIdUtente(DatiFatturaBean dati) {
        if (dati == null || !hasText(dati.getEmail())) return 0;

        final String email = dati.getEmail().trim().toLowerCase();
        GeneralUser user = userDAO().findByEmail(email);
        return user != null ? user.getIdUtente() : 0;
    }
}
