package com.ispw.controller.logic.ctrl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaDisdetta;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaRimborso;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaDisdetta;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoRimborso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * Logica di disdetta prenotazione.
 * - Nessun SQL: tutto passa dai DAO.
 * - Collaboratori secondari (rimborso/nota/notify/slot) passati via parametri.
 * - Calcolo penale tramite RegoleTempistiche/RegolePenalita (DAO singleton).
 * - Rimborso basato su importo effettivamente pagato (PagamentoDAO), non su stime.
 */
public class LogicControllerDisdettaPrenotazione {

    // ========================
    // Costanti messaggi (no magic strings)
    // ========================
    private static final String MSG_INPUT_NON_VALIDO   = "Input non valido";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_PREN_INESISTENTE   = "Prenotazione inesistente";
    private static final String MSG_GIA_ANNULLATA      = "Prenotazione già annullata";
    private static final String MSG_DISDETTA_OK        = "Disdetta eseguita";
    private static final String MSG_DISDETTA_KO        = "Disdetta non consentita";

    // ========================
    // Logger on-demand (niente campo statico) – S1312
    // ========================
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // ========================
    // DAO (factory; nessun accoppiamento a concreti)
    // ========================
    private PrenotazioneDAO      prenotazioneDAO()     { return DAOFactory.getInstance().getPrenotazioneDAO(); }
    private GeneralUserDAO       userDAO()             { return DAOFactory.getInstance().getGeneralUserDAO(); }
    private PagamentoDAO         pagamentoDAO()        { return DAOFactory.getInstance().getPagamentoDAO(); }
    private LogDAO               logDAO()              { return DAOFactory.getInstance().getLogDAO(); }
    private RegoleTempisticheDAO tempisticheDAO()      { return DAOFactory.getInstance().getRegoleTempisticheDAO(); }
    private RegolePenalitaDAO    penalitaDAO()         { return DAOFactory.getInstance().getRegolePenalitaDAO(); }

    // =====================================================================================
    // + ottieniPrenotazioniCancellabili(UtenteBean): List<RiepilogoPrenotazioneBean>
    //   - Restituisce prenotazioni future e non ANNULLATE dell’utente (riepilogo con importo pagato se disponibile).
    // =====================================================================================
    public List<RiepilogoPrenotazioneBean> ottieniPrenotazioniCancellabili(UtenteBean utente) {
        if (utente == null || isBlank(utente.getEmail())) return List.of();

        final var user = userDAO().findByEmail(normalizeEmail(utente.getEmail()));
        if (user == null) return List.of();

        final List<Prenotazione> tutte = prenotazioneDAO().findByUtente(user.getIdUtente());
        final LocalDateTime now = LocalDateTime.now();

        final List<RiepilogoPrenotazioneBean> out = new ArrayList<>();
        for (Prenotazione p : tutte) {
            if (!isCancellablePrenotazione(p, now)) continue;

            // Importo: se esiste pagamento, usa importoFinale; altrimenti 0 (pren. non pagata)
            float importo = 0f;
            var pag = pagamentoDAO().findByPrenotazione(p.getIdPrenotazione());
            if (pag != null && pag.getImportoFinale() != null) {
                importo = pag.getImportoFinale().floatValue();
            }

            RiepilogoPrenotazioneBean r = new RiepilogoPrenotazioneBean();
            r.setIdPrenotazione(p.getIdPrenotazione());
            r.setUtente(utente);
            r.setImportoTotale(importo);
            r.setDatiFiscali(null);
            out.add(r);
        }
        return out;
    }

    /**
     * Verifica se una prenotazione è cancellabile (futura e non annullata).
     */
    private boolean isCancellablePrenotazione(Prenotazione p, LocalDateTime now) {
        if (p == null) return false;
        if (p.getStato() == StatoPrenotazione.ANNULLATA) return false;
        if (p.getData() == null || p.getOraInizio() == null) return false;

        LocalDateTime inizio = LocalDateTime.of(p.getData(), p.getOraInizio());
        return inizio.isAfter(now); // solo future
    }

    // =====================================================================================
    // + anteprimaDisdetta(int, SessioneUtenteBean): EsitoDisdettaBean
    //   - Wrapper della validazione (possibile + penale stimata).
    // =====================================================================================
    public EsitoDisdettaBean anteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        return validaDisdetta(idPrenotazione, sessione);
    }

    // =====================================================================================
    // + eseguiAnnullamento(int, SessioneUtenteBean): EsitoOperazioneBean
    //   - Esegue il flusso: valida → (eventuale rimborso) → update stato → libera slot → notifica → log.
    //   - DIP via parametri per evitare accoppiamenti e favorire i test.
    // =====================================================================================
    public EsitoOperazioneBean eseguiAnnullamento(
            int idPrenotazione,
            SessioneUtenteBean sessione,
            GestionePagamentoRimborso payRimborso,
            GestioneFatturaRimborso   fattRimborso,
            GestioneNotificaDisdetta  noti,
            GestioneDisponibilitaDisdetta disp) {

        if (sessione == null || sessione.getUtente() == null
                || payRimborso == null || fattRimborso == null || noti == null || disp == null) {
            return esito(false, MSG_INPUT_NON_VALIDO);
        }

        final EsitoDisdettaBean preview = validaDisdetta(idPrenotazione, sessione);
        if (preview == null || !preview.isPossibile()) return esito(false, MSG_DISDETTA_KO);

        final var user = userDAO().findByEmail(normalizeEmail(sessione.getUtente().getEmail()));
        final Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (user == null) return esito(false, MSG_UTENTE_INESISTENTE);
        if (p == null)    return esito(false, MSG_PREN_INESISTENTE);
        if (p.getStato() == StatoPrenotazione.ANNULLATA) return esito(false, MSG_GIA_ANNULLATA);

        final float penale = Math.max(0f, preview.getPenale());

        // 1) rimborso (se necessario)
        processRefundIfConfirmed(p, idPrenotazione, penale, payRimborso, fattRimborso);

        // 2) elimina prenotazione e rilascia slot
        prenotazioneDAO().delete(idPrenotazione);
        liberaRisorsa(idPrenotazione, disp);

        // 3) notifica e log
        notifyAndLog(sessione, noti, user.getIdUtente(), idPrenotazione, penale);

        return esito(true, MSG_DISDETTA_OK);
    }

    /**
     * Overload semplice: crea internamente i controller secondari e li passa al metodo interno.
     */
    public EsitoOperazioneBean eseguiAnnullamento(int idPrenotazione, SessioneUtenteBean sessione) {
        return eseguiAnnullamento(
                idPrenotazione,
                sessione,
                new LogicControllerGestionePagamento(),
                new LogicControllerGestioneFattura(),
                new LogicControllerGestioneNotifica(),
                new LogicControllerGestoreDisponibilita());
    }

    // =====================================================================================
    // - validaDisdetta(int, SessioneUtenteBean): EsitoDisdettaBean
    //   - Titolarità, stato e finestra temporale; calcolo penale da regole singleton.
    // =====================================================================================
    EsitoDisdettaBean validaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        EsitoDisdettaBean out = new EsitoDisdettaBean();

        if (sessione == null || sessione.getUtente() == null || isBlank(sessione.getUtente().getEmail())) {
            return invalidoEsito();
        }
        final var user = userDAO().findByEmail(normalizeEmail(sessione.getUtente().getEmail()));
        if (user == null) {
            return invalidoEsito();
        }

        final Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (p == null) {
            return invalidoEsito();
        }
        if (p.getIdUtente() != user.getIdUtente()) {
            return invalidoEsito();
        }
        if (p.getStato() == StatoPrenotazione.ANNULLATA) {
            return invalidoEsito();
        }

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime start = LocalDateTime.of(p.getData(), p.getOraInizio());
        if (!start.isAfter(now)) {
            return invalidoEsito(); // nel passato
        }

        final RegoleTempistiche rt = tempisticheDAO().get(); // preavvisoMinimo (minuti)
        final RegolePenalita   rp = penalitaDAO().get();     // valorePenalita (BigDecimal) + preavvisoMinimo (minuti)
        final float penale = calcolaPenale(p, rt, rp);

        out.setPossibile(true);
        out.setPenale(penale);
        return out;
    }

    /** Helper per validazione fallita: ritorna EsitoDisdettaBean con possibile=false, penale=0f */
    private EsitoDisdettaBean invalidoEsito() {
        EsitoDisdettaBean out = new EsitoDisdettaBean();
        out.setPossibile(false);
        out.setPenale(0f);
        return out;
    }

    // =====================================================================================
    // - calcolaPenale(Prenotazione, RegoleTempistiche, RegolePenalita): float
    //   - Se il preavviso residuo >= minimo, penale 0; altrimenti penale fissa da regole.
    // =====================================================================================
    float calcolaPenale(Prenotazione p, RegoleTempistiche rt, RegolePenalita rp) {
        if (p == null) return 0f;

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime start = LocalDateTime.of(p.getData(), p.getOraInizio());
        final long minResidui = Duration.between(now, start).toMinutes();

        int preavvisoMin = 0;
        if (rt != null) preavvisoMin = Math.max(preavvisoMin, rt.getPreavvisoMinimo());
        if (rp != null && preavvisoMin <= 0) preavvisoMin = Math.max(preavvisoMin, rp.getPreavvisoMinimo());

        if (minResidui >= preavvisoMin) return 0f;

        final float penale = (rp != null && rp.getValorePenalita() != null)
                ? rp.getValorePenalita().floatValue()
                : 0f;
        return Math.max(0f, penale);
    }

    // =====================================================================================
    // - liberaRisorsa(int): void   (DIP: riceve il collaboratore in input)
    // =====================================================================================
    void liberaRisorsa(int idPrenotazione, GestioneDisponibilitaDisdetta dispCtrl) {
        if (dispCtrl == null) return;
        try {
            dispCtrl.liberaSlot(idPrenotazione);
            log().fine(() -> "[DISDETTA] Slot liberato per prenotazione #" + idPrenotazione);
        } catch (RuntimeException ex) {
            // Best effort: il rilascio slot non deve bloccare la disdetta
            log().log(Level.FINE, "Rilascio slot fallito: {0}", new Object[]{ex.getMessage()});
        }
    }

    private void processRefundIfConfirmed(Prenotazione p, int idPrenotazione, float penale,
                                           GestionePagamentoRimborso payRimborso,
                                           GestioneFatturaRimborso fattRimborro) {
        if (p == null) return;
        if (p.getStato() != StatoPrenotazione.CONFERMATA) return;

        final var pag = pagamentoDAO().findByPrenotazione(idPrenotazione);
        final float importoPagato = (pag != null && pag.getImportoFinale() != null)
                ? pag.getImportoFinale().floatValue() : 0f;
        final float rimborso = Math.max(0f, importoPagato - penale);
        if (rimborso <= 0f) return;

        try {
            payRimborso.eseguiRimborso(idPrenotazione, rimborso);
            fattRimborro.emettiNotaDiCredito(idPrenotazione);
        } catch (RuntimeException ex) {
            // Best-effort: errori refund non bloccano la disdetta
            log().log(Level.FINE, "Rimborso fallito: {0}", new Object[]{ex.getMessage()});
        }
    }

    private void notifyAndLog(SessioneUtenteBean sessione,
                              GestioneNotificaDisdetta noti,
                              int idUtente,
                              int idPrenotazione,
                              float penale) {
        try {
            noti.inviaConfermaCancellazione(sessione.getUtente(),
                    "Prenotazione #" + idPrenotazione + " annullata (penale " + penale + "€)");
        } catch (RuntimeException ex) {
            // best-effort
            log().log(Level.FINE, "Notifica disdetta fallita: {0}", new Object[]{ex.getMessage()});
        }

        appendLogSafe(idUtente, "DISDETTA_PRENOTAZIONE #" + idPrenotazione + " - penale " + penale + "€");
    }

    // ========================
    // Helper
    // ========================
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        final String t = email.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }

    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }

    private void appendLogSafe(int idUtente, String descr) {
        try {
            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setIdUtenteCoinvolto(idUtente);
            log.setDescrizione(descr);
            logDAO().append(log); // append-only
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log disdetta fallito: {0}", new Object[]{ex.getMessage()});
        }
    }
}
