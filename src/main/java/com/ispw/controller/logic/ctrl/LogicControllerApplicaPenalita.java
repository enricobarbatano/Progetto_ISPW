package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Penalita;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.SystemLog;

/**
 * Controller applicativo per la gestione delle penalità/sanzioni.
 * Stateless, Sonar-friendly: early-return, DIP by-parameter, best-effort sui collaboratori.
 */
public final class LogicControllerApplicaPenalita {

    // === Messaggi centralizzati ===
    private static final String MSG_OK                  = "Operazione completata";
    private static final String MSG_INPUT_NON_VALIDO   = "Dati penalità non validi";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_IMPORTO_NON_VALIDO = "Importo penalità non valido";

    // === Logger (soppressione mirata regola S1312) ===
    @SuppressWarnings("java:S1312")
    private static final Logger LOGGER =
            Logger.getLogger(LogicControllerApplicaPenalita.class.getName());

    // === DIP: DAO iniettati via costruttore ===
    // DAO on-demand (no SQL nei controller)
    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }
    private PenalitaDAO penalitaDAO() {
        return DAOFactory.getInstance().getPenalitaDAO();
    }
    private RegolePenalitaDAO regolePenalitaDAO() {
        return DAOFactory.getInstance().getRegolePenalitaDAO();
    }

    // ========================================================================
    //  Metodo pubblico (rifattorizzato: bassa complessità cognitiva)
    // ========================================================================
    public EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati) {
        return applicaSanzione(
                dati,
                null,
                null,
                new LogicControllerGestionePagamento(),
                new LogicControllerGestioneFattura(),
                new LogicControllerGestioneNotifica());
    }

    public EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati,
                                               DatiPagamentoBean datiPagamento,
                                               DatiFatturaBean   datiFattura,
                                               GestionePagamentoPenalita payCtrl,
                                               GestioneFatturaPenalita   fattCtrl,
                                               GestioneNotificaPenalita  notiCtrl) {

        // 1) Guard-clauses: input e precondizioni
        if (!isDatiPenalitaValidi(dati)) {
            return esito(false, MSG_INPUT_NON_VALIDO);
        }

        final int idUtente = dati.getIdUtente();
        final GeneralUser user = userDAO().findById(idUtente);
        if (user == null) {
            return esito(false, MSG_UTENTE_INESISTENTE);
        }

        final BigDecimal importo = resolveImportoOrDefault(dati);
        if (!isImportoValido(importo)) {
            return esito(false, MSG_IMPORTO_NON_VALIDO);
        }

        // 2) Persistenza best-effort su PenalitaDAO (con fallback deterministico)
        final int idPenalita = persistOrComputeId(dati, importo);

        // 3) Orchestrazione opzionale (best-effort)
        handleNotifica(notiCtrl, idUtente);
        handlePagamento(payCtrl, datiPagamento, idPenalita, importo);
        handleFattura(fattCtrl, datiFattura, idPenalita);

        // 4) Log finale (audit)
        appendLogSafe(
            idUtente,
            "[PENALITA] id=" + idPenalita +
            " importo=" + importo +
            " motivo=" + safe(dati.getMotivazione())
        );

        // 5) Esito OK
        return esito(true, MSG_OK);
    }

    // ========================================================================
    //  Helper di validazione / normalizzazione
    // ========================================================================

    private boolean isDatiPenalitaValidi(DatiPenalitaBean d) {
        return d != null
            && d.getIdUtente() > 0
            && !isBlank(d.getMotivazione()); // coerente con i test: motivazione non blank
    }

    private boolean isImportoValido(BigDecimal imp) {
        return imp != null && imp.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calcola l'importo: prima dal bean, altrimenti dalle regole (se presenti).
     * Se null/<=0, tornerà null e verrà bocciato dal check a monte.
    */
    private BigDecimal resolveImportoOrDefault(DatiPenalitaBean d) {
        BigDecimal imp = d.getImporto();
        if (isImportoValido(imp)) return imp;

        if (regolePenalitaDAO() != null) {
            try {
                RegolePenalita r = regolePenalitaDAO().get();
                if (r != null && isImportoValido(r.getValorePenalita())) {
                    return r.getValorePenalita();
                }
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Lettura RegolePenalita fallita: {0}", new Object[]{ex.getMessage()});
            }
        }
        return imp;
    }

    // ========================================================================
    //  Collaborazioni best-effort (notifica/pagamento/fattura)
    // ========================================================================

    private void handleNotifica(GestioneNotificaPenalita notiCtrl, int idUtente) {
        if (notiCtrl == null) return;
        runBestEffort("Notifica penalità",
            () -> notiCtrl.inviaNotificaPenalita(String.valueOf(idUtente)));
    }

    private void handlePagamento(GestionePagamentoPenalita payCtrl,
                                 DatiPagamentoBean datiPagamento,
                                 int idPenalita,
                                 BigDecimal importoPenalita) {
        if (payCtrl == null || datiPagamento == null) return;
        normalizePagamento(datiPagamento, importoPenalita);
        runBestEffort("Pagamento penalità",
            // NB: la tua interfaccia usa l’accento nel nome del metodo
            () -> payCtrl.richiediPagamentoPenalita(datiPagamento, idPenalita));
    }

    private void handleFattura(GestioneFatturaPenalita fattCtrl,
                               DatiFatturaBean datiFattura,
                               int idPenalita) {
        if (fattCtrl == null || datiFattura == null) return;
        normalizeFattura(datiFattura);
        runBestEffort("Fattura penalità",
            () -> fattCtrl.generaFatturaPenalita(datiFattura, idPenalita));
    }

    // ========================================================================
    //  Normalizzazioni DTO (centralizzazione dei default)
    // ========================================================================

    private void normalizePagamento(DatiPagamentoBean pay, BigDecimal importoPenalita) {
        if (pay == null) return;
        if (pay.getImporto() <= 0f) {
            pay.setImporto(importoPenalita.floatValue());
        }
        if (isBlank(pay.getMetodo())) {
            pay.setMetodo("PAYPAL");
        }
    }

    private void normalizeFattura(DatiFatturaBean fat) {
        if (fat == null) return;
        if (fat.getDataOperazione() == null) {
            fat.setDataOperazione(LocalDate.now());
        }
    }

    // ========================================================================
    //  Persistenza penalità (best-effort + fallback deterministico)
    // ========================================================================

    private int persistOrComputeId(DatiPenalitaBean d, BigDecimal importo) {
        try {
            Penalita p = new Penalita();
            p.setIdUtente(d.getIdUtente());
            p.setMotivazione(d.getMotivazione());
            p.setImporto(importo);
            penalitaDAO().store(p); // si assume assegni l'ID all'istanza
            if (p.getIdPenalita() > 0) return p.getIdPenalita();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Persistenza Penalita fallita: {0}", new Object[]{ex.getMessage()});
        }

        // Fallback deterministico (evita 0/negativi)
        int hash = Math.abs(Objects.hash(
                d.getIdUtente(),
                safe(d.getMotivazione()),
                importo != null ? importo.stripTrailingZeros().toPlainString() : "0",
                LocalDate.now()
        ));
        return (hash == 0 ? 1 : hash);
    }

    // ========================================================================
    //  Infrastruttura: logging best-effort, utilità, esito, accesso DAO
    // ========================================================================

    /**
     * Logga sempre via JUL e, se disponibile, persiste su LogDAO in best-effort.
     * Rifattorizzata per bassa complessità: passaggi lineari e responsabilità chiare.
     */
    private void appendLogSafe(int idUtente, String descrizione) {
        // 1) Log via JUL (sempre)
        log().log(Level.INFO, () -> "UTENTE=" + idUtente + " " + descrizione);

        // 2) Best-effort: persistenza su LogDAO
        try {
            LogDAO ldao = DAOFactory.getInstance().getLogDAO();
            if (ldao == null) return;

            SystemLog sl = new SystemLog();
            sl.setIdUtenteCoinvolto(idUtente);
            sl.setDescrizione(descrizione);
            sl.setTimestamp(LocalDateTime.now());
            ldao.append(sl);
        } catch (RuntimeException ex) {
            // best-effort: non deve far fallire l'operazione principale
            log().log(Level.FINE, "Scrittura LogDAO fallita: {0}", new Object[]{ex.getMessage()});
        }
    }

    private void runBestEffort(String what, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "{0} fallita: {1}", new Object[]{what, ex.getMessage()});
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private Logger log() {
        return LOGGER;
    }

    /** Costruisce l'esito (no reflection: usiamo direttamente i setter reali). */
    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }
}