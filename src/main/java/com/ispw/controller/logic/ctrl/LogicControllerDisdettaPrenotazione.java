package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoDisdettaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RichiestaDisdettaBean;
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
import com.ispw.dao.interfaces.RichiestaDisdettaDAO;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.RichiestaDisdettaRimborso;
import com.ispw.model.entity.SystemLog;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoPrenotazione;
import com.ispw.model.enums.StatoRichiestaDisdetta;

/**
 * Logica di disdetta prenotazione.
 * - Nessun SQL: tutto passa dai DAO.
 * - Collaboratori secondari (rimborso/nota/notify/slot) passati via parametri.
 * - Calcolo penale tramite RegoleTempistiche/RegolePenalita (DAO singleton).
 * - Rimborso basato su importo effettivamente pagato (PagamentoDAO), non su stime.
 *
 * ESTENSIONE per UC complesso (2 attori):
 * - UTENTE crea richiesta disdetta (PENDING) con penale/rimborso stimati
 * - GESTORE approva/rifiuta; se approva esegue annullamento e marca ESEGUITA
 *
 * Vincolo responsabilità:
 * - il layer graphic deve chiamare SOLO metodi di questo logic controller
 * - quindi esiste un overload "pulito" che incapsula i secondari (default) per il gestore.
 */
public class LogicControllerDisdettaPrenotazione {

    // Costanti messaggi (no magic strings)
    private static final String MSG_INPUT_NON_VALIDO   = "Input non valido";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_PREN_INESISTENTE   = "Prenotazione inesistente";
    private static final String MSG_GIA_ANNULLATA      = "Prenotazione gia annullata";
    private static final String MSG_DISDETTA_OK        = "Disdetta eseguita";
    private static final String MSG_DISDETTA_KO        = "Disdetta non consentita";

    // Messaggi UC complesso
    private static final String MSG_RICHIESTA_OK         = "Richiesta disdetta inviata";
    private static final String MSG_RICHIESTA_GIA_ESISTE = "Richiesta già presente per la prenotazione";
    private static final String MSG_RICHIESTA_ASSENTE    = "Richiesta inesistente";
    private static final String MSG_RICHIESTA_GIA_GESTITA= "Richiesta già gestita";
    private static final String MSG_SOLO_GESTORE         = "Operazione riservata al gestore";
    private static final String MSG_SOLO_UTENTE          = "Operazione riservata all'utente";

    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // DAO (factory; nessun accoppiamento a concreti)
    private PrenotazioneDAO      prenotazioneDAO() { return DAOFactory.getInstance().getPrenotazioneDAO(); }
    private GeneralUserDAO       userDAO()         { return DAOFactory.getInstance().getGeneralUserDAO(); }
    private PagamentoDAO         pagamentoDAO()    { return DAOFactory.getInstance().getPagamentoDAO(); }
    private LogDAO               logDAO()          { return DAOFactory.getInstance().getLogDAO(); }
    private RegoleTempisticheDAO tempisticheDAO()  { return DAOFactory.getInstance().getRegoleTempisticheDAO(); }
    private RegolePenalitaDAO    penalitaDAO()     { return DAOFactory.getInstance().getRegolePenalitaDAO(); }
    private RichiestaDisdettaDAO richiestaDAO()    { return DAOFactory.getInstance().getRichiestaDisdettaDAO(); }

    // =====================================================================
    // UTENTE: prenotazioni cancellabili + anteprima
    // =====================================================================

    public List<RiepilogoPrenotazioneBean> ottieniPrenotazioniCancellabili(UtenteBean utente) {
        if (utente == null || isBlank(utente.getEmail())) return List.of();

        final var user = userDAO().findByEmail(normalizeEmail(utente.getEmail()));
        if (user == null) return List.of();

        if (user.getRuolo() != Ruolo.UTENTE) return List.of();

        final List<Prenotazione> tutte = prenotazioneDAO().findByUtente(user.getIdUtente());
        final LocalDateTime now = LocalDateTime.now();

        final List<RiepilogoPrenotazioneBean> out = new ArrayList<>();
        for (Prenotazione p : tutte) {
            if (!isCancellablePrenotazione(p, now)) continue;

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

    public EsitoDisdettaBean anteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        return validaDisdetta(idPrenotazione, sessione);
    }

    // =====================================================================
    // UC complesso STEP 1: UTENTE crea richiesta (PENDING)
    // =====================================================================

    public EsitoOperazioneBean richiediDisdetta(int idPrenotazione, String notaUtente, SessioneUtenteBean sessione) {

        if (sessione == null || sessione.getUtente() == null || isBlank(sessione.getUtente().getEmail())) {
            return esito(false, MSG_INPUT_NON_VALIDO);
        }

        final var user = resolveUserFromSession(sessione);
        if (user == null) return esito(false, MSG_UTENTE_INESISTENTE);

        if (user.getRuolo() != Ruolo.UTENTE) return esito(false, MSG_SOLO_UTENTE);

        final EsitoDisdettaBean preview = validaDisdetta(idPrenotazione, sessione);
        if (preview == null || !preview.isPossibile()) return esito(false, MSG_DISDETTA_KO);

        // evita doppie richieste pendenti sulla stessa prenotazione
        RichiestaDisdettaRimborso existing = richiestaDAO().findByPrenotazione(idPrenotazione);
        if (existing != null && existing.getStato() == StatoRichiestaDisdetta.PENDING) {
            return esito(false, MSG_RICHIESTA_GIA_ESISTE);
        }

        final Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (p == null) return esito(false, MSG_PREN_INESISTENTE);

        final float penale = Math.max(0f, preview.getPenale());

        float importoPagato = 0f;
        Pagamento pag = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pag != null && pag.getImportoFinale() != null) {
            importoPagato = pag.getImportoFinale().floatValue();
        }
        float rimborsoStimato = Math.max(0f, importoPagato - penale);

        RichiestaDisdettaRimborso req = new RichiestaDisdettaRimborso();
        req.setIdPrenotazione(idPrenotazione);
        req.setIdUtente(user.getIdUtente());
        req.setTimestampRichiesta(LocalDateTime.now());
        req.setPenaleStimata(BigDecimal.valueOf(penale));
        req.setRimborsoStimato(BigDecimal.valueOf(rimborsoStimato));
        req.setStato(StatoRichiestaDisdetta.PENDING);
        req.setNotaUtente(notaUtente);

        richiestaDAO().store(req);

        appendLogSafe(user.getIdUtente(),
                "RICHIESTA_DISDETTA #" + req.getIdRichiesta() +
                " pren=" + idPrenotazione +
                " penaleStimata=" + penale +
                " rimborsoStimato=" + rimborsoStimato);

        return esito(true, MSG_RICHIESTA_OK);
    }

    // =====================================================================
    // UC complesso STEP 2: GESTORE lista pending (ritorna BEAN, non entity)
    // =====================================================================

    public List<RichiestaDisdettaBean> listaRichiestePending(SessioneUtenteBean sessioneGestore) {
        if (!isGestore(sessioneGestore)) return List.of();

        return richiestaDAO().findByStato(StatoRichiestaDisdetta.PENDING).stream()
                .filter(r -> r != null)
                .map(this::toBean)
                .toList();
    }

    // =====================================================================
    // UC complesso STEP 2: overload "pulito" (NO secondari nel layer graphic)
    // =====================================================================

    public EsitoOperazioneBean valutaRichiestaDisdetta(
            int idRichiesta,
            boolean approva,
            String notaGestore,
            SessioneUtenteBean sessioneGestore) {

        return valutaRichiestaDisdetta(
                idRichiesta,
                approva,
                notaGestore,
                sessioneGestore,
                new LogicControllerGestionePagamento(),
                new LogicControllerGestioneFattura(),
                new LogicControllerGestioneNotifica(),
                new LogicControllerGestoreDisponibilita()
        );
    }

    // =====================================================================
    // UC complesso STEP 2: metodo completo (secondari per test/mocking)
    // =====================================================================

    public EsitoOperazioneBean valutaRichiestaDisdetta(
            int idRichiesta,
            boolean approva,
            String notaGestore,
            SessioneUtenteBean sessioneGestore,
            GestionePagamentoRimborso payRimborso,
            GestioneFatturaRimborso   fattRimborso,
            GestioneNotificaDisdetta  noti,
            GestioneDisponibilitaDisdetta disp) {

        if (!isGestore(sessioneGestore)) return esito(false, MSG_SOLO_GESTORE);
        if (idRichiesta <= 0) return esito(false, MSG_INPUT_NON_VALIDO);
        if (payRimborso == null || fattRimborso == null || noti == null || disp == null) return esito(false, MSG_INPUT_NON_VALIDO);

        RichiestaDisdettaRimborso req = richiestaDAO().load(idRichiesta);
        if (req == null) return esito(false, MSG_RICHIESTA_ASSENTE);
        if (req.getStato() != StatoRichiestaDisdetta.PENDING) return esito(false, MSG_RICHIESTA_GIA_GESTITA);

        // best-effort: idGestore dalla sessione
        Integer idGestore = null;
        try {
            var g = resolveUserFromSession(sessioneGestore);
            if (g != null) idGestore = g.getIdUtente();
        } catch (RuntimeException ignored) { }

        if (!approva) {
            richiestaDAO().updateStato(idRichiesta, StatoRichiestaDisdetta.RIFIUTATA, idGestore, notaGestore);
            appendLogSafe(req.getIdUtente(), "RICHIESTA_DISDETTA RIFIUTATA #" + idRichiesta);
            return esito(true, "Richiesta rifiutata");
        }

        richiestaDAO().updateStato(idRichiesta, StatoRichiestaDisdetta.APPROVATA, idGestore, notaGestore);

        var user = userDAO().findById(req.getIdUtente());
        if (user == null) return esito(false, MSG_UTENTE_INESISTENTE);

        SessioneUtenteBean sessioneUtente = new SessioneUtenteBean();
        sessioneUtente.setUtente(new UtenteBean(user.getNome(), user.getCognome(), user.getEmail(), user.getRuolo()));
        sessioneUtente.setIdSessione("AUTO-" + System.currentTimeMillis());

        EsitoOperazioneBean exec = eseguiAnnullamento(
                req.getIdPrenotazione(),
                sessioneUtente,
                payRimborso,
                fattRimborso,
                noti,
                disp
        );

        if (exec != null && exec.isSuccesso()) {
            richiestaDAO().updateStato(idRichiesta, StatoRichiestaDisdetta.ESEGUITA, idGestore, notaGestore);
            appendLogSafe(req.getIdUtente(), "RICHIESTA_DISDETTA ESEGUITA #" + idRichiesta);
            return esito(true, "Richiesta approvata ed eseguita");
        }

        return esito(false, "Approvata ma esecuzione fallita");
    }

    // =====================================================================
    // Flusso CORE già esistente (immutato)
    // =====================================================================

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

        final var user = resolveUserFromSession(sessione);
        if (user == null) return esito(false, MSG_UTENTE_INESISTENTE);

        if (user.getRuolo() != Ruolo.UTENTE) return esito(false, MSG_DISDETTA_KO);

        final Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (p == null) return esito(false, MSG_PREN_INESISTENTE);
        if (p.getStato() == StatoPrenotazione.ANNULLATA) return esito(false, MSG_GIA_ANNULLATA);

        final float penale = Math.max(0f, preview.getPenale());

        processRefundIfConfirmed(p, idPrenotazione, penale, payRimborso, fattRimborso);

        prenotazioneDAO().delete(idPrenotazione);
        liberaRisorsa(idPrenotazione, disp);

        notifyAndLog(sessione, noti, user.getIdUtente(), idPrenotazione, penale);

        return esito(true, MSG_DISDETTA_OK);
    }

    public EsitoOperazioneBean eseguiAnnullamento(int idPrenotazione, SessioneUtenteBean sessione) {
        return eseguiAnnullamento(
                idPrenotazione,
                sessione,
                new LogicControllerGestionePagamento(),
                new LogicControllerGestioneFattura(),
                new LogicControllerGestioneNotifica(),
                new LogicControllerGestoreDisponibilita());
    }

    EsitoDisdettaBean validaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null || isBlank(sessione.getUtente().getEmail())) {
            return invalidoEsito();
        }

        final var user = resolveUserFromSession(sessione);
        if (user == null) return invalidoEsito();

        if (user.getRuolo() != Ruolo.UTENTE) return invalidoEsito();

        final Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (p == null) return invalidoEsito();

        if (p.getIdUtente() != user.getIdUtente()) return invalidoEsito();
        if (p.getStato() == StatoPrenotazione.ANNULLATA) return invalidoEsito();

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime start = LocalDateTime.of(p.getData(), p.getOraInizio());
        if (!start.isAfter(now)) return invalidoEsito();

        RegoleTempistiche rt = null;
        RegolePenalita rp = null;
        try { rt = tempisticheDAO().get(); } catch (RuntimeException ex) { }
        try { rp = penalitaDAO().get(); } catch (RuntimeException ex) { }

        final float penale = calcolaPenale(p, rt, rp);

        EsitoDisdettaBean out = new EsitoDisdettaBean();
        out.setPossibile(true);
        out.setPenale(penale);
        return out;
    }

    private EsitoDisdettaBean invalidoEsito() {
        EsitoDisdettaBean out = new EsitoDisdettaBean();
        out.setPossibile(false);
        out.setPenale(0f);
        return out;
    }

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

    void liberaRisorsa(int idPrenotazione, GestioneDisponibilitaDisdetta dispCtrl) {
        if (dispCtrl == null) return;
        try {
            dispCtrl.liberaSlot(idPrenotazione);
            log().fine(() -> "[DISDETTA] Slot liberato per prenotazione #" + idPrenotazione);
        } catch (RuntimeException ex) {
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
                    "Prenotazione #" + idPrenotazione + " annullata (penale " + penale + " EUR)");
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica disdetta fallita: {0}", new Object[]{ex.getMessage()});
        }

        appendLogSafe(idUtente, "DISDETTA_PRENOTAZIONE #" + idPrenotazione + " - penale " + penale + " EUR");
    }

    private boolean isCancellablePrenotazione(Prenotazione p, LocalDateTime now) {
        if (p == null) return false;
        if (p.getStato() == StatoPrenotazione.ANNULLATA) return false;
        if (p.getData() == null || p.getOraInizio() == null) return false;

        LocalDateTime inizio = LocalDateTime.of(p.getData(), p.getOraInizio());
        return inizio.isAfter(now);
    }

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
            logDAO().append(log);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log disdetta fallito: {0}", new Object[]{ex.getMessage()});
        }
    }

    private com.ispw.model.entity.GeneralUser resolveUserFromSession(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null || isBlank(sessione.getUtente().getEmail())) return null;
        return userDAO().findByEmail(normalizeEmail(sessione.getUtente().getEmail()));
    }

    private boolean isGestore(SessioneUtenteBean sessione) {
        return sessione != null
                && sessione.getUtente() != null
                && sessione.getUtente().getRuolo() == Ruolo.GESTORE;
    }

    // ===================== Mapping entity -> bean =====================

    private RichiestaDisdettaBean toBean(RichiestaDisdettaRimborso r) {
        RichiestaDisdettaBean b = new RichiestaDisdettaBean();
        b.setIdRichiesta(r.getIdRichiesta());
        b.setIdPrenotazione(r.getIdPrenotazione());
        b.setIdUtente(r.getIdUtente());
        b.setTimestampRichiesta(r.getTimestampRichiesta());
        b.setTimestampDecisione(r.getTimestampDecisione());
        b.setPenaleStimata(r.getPenaleStimata());
        b.setRimborsoStimato(r.getRimborsoStimato());
        b.setStato(r.getStato());
        b.setNotaUtente(r.getNotaUtente());
        b.setNotaGestore(r.getNotaGestore());
        b.setIdGestoreDecisione(r.getIdGestoreDecisione());
        return b;
    }
}