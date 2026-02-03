package com.ispw.controller.logic.ctrl;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPrenotazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPrenotazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPrenotazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPagamento;
import com.ispw.model.enums.StatoPrenotazione;

public class LogicControllerPrenotazioneCampo {
    private static final String pagato= "PAGATO";
    // ========================
    // Costanti di servizio
    // ========================
    private static final int DEFAULT_DURATA_MIN = 60;
    private static final String MSG_SLOT_NON_DISP    = "[PRENOT] Slot non disponibile: campo={0} {1} {2}-{3}";
    private static final String MSG_NO_PREN_DA_PAG   = "Nessuna prenotazione da pagare";
    private static final String MSG_INPUT_NON_VALIDO = "Input non valido";

    // ========================
    // Logger on-demand (no static field) – S1312
    // ========================
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // ========================
    // DAO on-demand (no SQL nei controller)
    // ========================
    private PrenotazioneDAO prenotazioneDAO() { return DAOFactory.getInstance().getPrenotazioneDAO(); }
    private CampoDAO        campoDAO()        { return DAOFactory.getInstance().getCampoDAO(); }
    private GeneralUserDAO  userDAO()         { return DAOFactory.getInstance().getGeneralUserDAO(); }
    private PagamentoDAO    pagamentoDAO()    { return DAOFactory.getInstance().getPagamentoDAO(); }

    // =============================================================================
    // 1) TROVA SLOT DISPONIBILI
    // =============================================================================
    public List<DatiDisponibilitaBean> trovaSlotDisponibili(ParametriVerificaBean param,
                                                            GestioneDisponibilitaPrenotazione dispCtrl) {
        if (param == null || dispCtrl == null) return List.of();
        return dispCtrl.verificaDisponibilita(param);
    }

    // =============================================================================
    // 2) NUOVA PRENOTAZIONE (crea DA_PAGARE + blocca slot) → Riepilogo
    // =============================================================================
    public RiepilogoPrenotazioneBean nuovaPrenotazione(DatiInputPrenotazioneBean input,
                                                       SessioneUtenteBean sessione,
                                                       GestioneDisponibilitaPrenotazione dispCtrl) {
        if (input == null || sessione == null || sessione.getUtente() == null || dispCtrl == null) return null;

        final int idCampo  = input.getIdCampo();
        final String sData = input.getData();
        final String sIniz = input.getOraInizio();
        final String sFine = input.getOraFine();

        if (idCampo <= 0 || isBlank(sData) || isBlank(sIniz)) return null;

        final LocalDate data;
        final LocalTime inizio;
        final LocalTime fine;
        try {
            data   = LocalDate.parse(sData.trim()); // yyyy-MM-dd
            inizio = LocalTime.parse(sIniz.trim()); // HH:mm
            fine   = !isBlank(sFine) ? LocalTime.parse(sFine.trim()) : inizio.plusMinutes(DEFAULT_DURATA_MIN);
        } catch (DateTimeParseException ex) {
            log().log(Level.WARNING, "[PRENOT] Formato data/ora non valido", ex);
            return null;
        }

        final int durataMin = (int) Duration.between(inizio, fine).toMinutes();
        if (durataMin <= 0) return null;

        final String email = normalizeEmail(sessione.getUtente().getEmail());
        if (email == null) return null;

        final var user = userDAO().findByEmail(email);
        if (user == null) return null;

        // 1) (ri)verifica disponibilità
        ParametriVerificaBean pv = new ParametriVerificaBean();
        pv.setIdCampo(idCampo);
        pv.setData(data.toString());
        pv.setOraInizio(inizio.toString());
        pv.setDurataMin(durataMin);

        var slots = dispCtrl.verificaDisponibilita(pv);
        boolean disponibile = slots.stream().anyMatch(d ->
                Objects.equals(d.getData(),     data.toString())
             && Objects.equals(d.getOraInizio(), inizio.toString())
             && Objects.equals(d.getOraFine(),   inizio.plusMinutes(durataMin).toString())
        );
        if (!disponibile) {
            log().log(Level.WARNING, MSG_SLOT_NON_DISP, new Object[]{idCampo, data, inizio, fine});
            return null;
        }

        // 2) blocca slot su Campo
        Campo c = campoDAO().findById(idCampo);
        if (c == null) return null;
        c.bloccoSlot(Date.valueOf(data), Time.valueOf(inizio), Time.valueOf(fine));
        campoDAO().store(c);

        // 3) crea Prenotazione (DA_PAGARE) e persiste
        Prenotazione p = new Prenotazione();
        p.setIdCampo(idCampo);
        p.setIdUtente(user.getIdUtente());
        p.setData(data);
        p.setOraInizio(inizio);
        p.setOraFine(fine);
        p.setStato(StatoPrenotazione.DA_PAGARE);
        prenotazioneDAO().store(p);

        log().fine(() -> "[PRENOT] Creata prenotazione id=" + p.getIdPrenotazione() + " per utente=" + email);

        // 4) riepilogo
        return buildRiepilogo(p, c, sessione.getUtente(), durataMin);
    }

    // =============================================================================
    // 3) COMPLETA PRENOTAZIONE (pagamento → conferma → fattura → notifica)
    //    Rifattorizzato per ridurre la complessità cognitiva, senza cambiare la logica.
    // =============================================================================
    public StatoPagamentoBean completaPrenotazione(DatiPagamentoBean dati,
                                                   SessioneUtenteBean sessione,
                                                   GestionePagamentoPrenotazione payCtrl,
                                                   GestioneFatturaPrenotazione   fattCtrl,
                                                   GestioneNotificaPrenotazione  notiCtrl) {
        // 1) Validazioni iniziali (early return)
        if (!isCheckoutInputValid(dati, sessione, payCtrl, fattCtrl, notiCtrl)) {
            return esitoPagamento(false, "KO", MSG_INPUT_NON_VALIDO);
        }

        // 2) Identificazione utente da sessione
        final String email = normalizeEmail(sessione.getUtente().getEmail());
        if (email == null) {
            return esitoPagamento(false, "KO", "Sessione senza email");
        }
        final var user = userDAO().findByEmail(email);
        if (user == null) {
            return esitoPagamento(false, "KO", "Utente inesistente");
        }

        // 3) Prenotazione da pagare
        final Prenotazione p = getFirstDaPagare(user.getIdUtente());
        if (p == null) {
            return esitoPagamento(false, "KO", MSG_NO_PREN_DA_PAG);
        }

        // 4) Pagamento
        final StatoPagamento statoEnum = payCtrl.richiediPagamentoPrenotazione(dati, p.getIdPrenotazione());
        final boolean success = isPagamentoOk(statoEnum);

        // 5) Post-pagamento (solo se successo): conferma → fattura → notifica
        if (success) {
            postPagamentoActions(p, email, fattCtrl, notiCtrl, sessione);
        }

        // 6) Esito: preferisci quanto persistito dal PagamentoDAO; altrimenti fallback
        return composeEsito(p, success, statoEnum);
    }

    /* ========================= Helper estratti (nessuna logica cambiata) ========================= */

    private boolean isCheckoutInputValid(DatiPagamentoBean dati,
                                         SessioneUtenteBean sessione,
                                         GestionePagamentoPrenotazione payCtrl,
                                         GestioneFatturaPrenotazione   fattCtrl,
                                         GestioneNotificaPrenotazione  notiCtrl) {
        return dati != null
            && sessione != null
            && sessione.getUtente() != null
            && payCtrl != null
            && fattCtrl != null
            && notiCtrl != null;
    }

    private Prenotazione getFirstDaPagare(int idUtente) {
        var daPagare = prenotazioneDAO().findByUtenteAndStato(idUtente, StatoPrenotazione.DA_PAGARE);
        return daPagare.isEmpty() ? null : daPagare.get(0);
    }

    /**
     * Conferma prenotazione + emette fattura + invia notifica (solo in caso di successo).
     * Non cambia i messaggi né le modalità di invocazione.
     */
    private void postPagamentoActions(Prenotazione p,
                                      String email,
                                      GestioneFatturaPrenotazione fattCtrl,
                                      GestioneNotificaPrenotazione notiCtrl,
                                      SessioneUtenteBean sessione) {
        prenotazioneDAO().updateStato(p.getIdPrenotazione(), StatoPrenotazione.CONFERMATA);

        DatiFatturaBean fatt = new DatiFatturaBean();
        // NB: rimane invariato il placeholder già presente
        fatt.setCodiceFiscaleCliente(email);
        fattCtrl.generaFatturaPrenotazione(fatt, p.getIdPrenotazione());

        notiCtrl.inviaConfermaPrenotazione(sessione.getUtente(),
                "Prenotazione #" + p.getIdPrenotazione() + " confermata");
    }

    /**
     * Ricompone lo stato di pagamento preferendo quanto scritto dal DAO;
     * se assente, usa il fallback identico all’implementazione originale.
     */
    private StatoPagamentoBean composeEsito(Prenotazione p, boolean success, StatoPagamento statoEnum) {
        Pagamento pag = pagamentoDAO().findByPrenotazione(p.getIdPrenotazione());
        if (pag != null) {
            StatoPagamentoBean bean = new StatoPagamentoBean();
            bean.setSuccesso(success);
            bean.setStato(pag.getStato() != null ? pag.getStato().name() : (success ? pagato : "KO"));
            bean.setIdTransazione(newTxId("PX")); // invariato
            bean.setDataPagamento(pag.getDataPagamento());
            bean.setMessaggio(success ? "Pagamento eseguito" : "Pagamento rifiutato");
            return bean;
        }
        // Fallback minimale (invariato)
        return esitoPagamento(success,
                (statoEnum != null ? statoEnum.name() : (success ? pagato: "KO")),
                success ? "Pagamento eseguito" : "Pagamento rifiutato");
    }

    // =========================
    // Helper generali
    // =========================
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private RiepilogoPrenotazioneBean buildRiepilogo(Prenotazione p, Campo c, UtenteBean utente, int durataMin) {
        RiepilogoPrenotazioneBean r = new RiepilogoPrenotazioneBean();
        r.setIdPrenotazione(p.getIdPrenotazione());
        r.setUtente(utente);

        float importo = 0f;
        if (c != null && c.getCostoOrario() != null) {
            importo = c.getCostoOrario() * (durataMin / 60f); // pro-rata
        }
        r.setImportoTotale(importo);
        r.setDatiFiscali(null);
        return r;
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        final String t = email.trim();
        return t.isEmpty() ? null : t.toLowerCase();
    }

    // ✓ riconosce anche StatoPagamento.OK
    private boolean isPagamentoOk(StatoPagamento stato) {
        if (stato == null) return false;
        if (stato == StatoPagamento.OK) return true; // esplicito
        final String s = stato.name();
        return s.contains("ESEGUITO")
            || s.contains("APPROVATO")
            || s.contains(pagato)
            || s.contains("SUCCESSO")
            || s.contains("COMPLETATO");
    }

    private String newTxId(String prefix) { return prefix + "-" + System.currentTimeMillis(); }

    private StatoPagamentoBean esitoPagamento(boolean ok, String stato, String msg) {
        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(ok);
        bean.setStato(stato != null ? stato : (ok ? pagato : "KO"));
        bean.setIdTransazione(newTxId("TX"));
        bean.setDataPagamento(java.time.LocalDateTime.now());
        bean.setMessaggio(msg);
        return bean;
    }
}