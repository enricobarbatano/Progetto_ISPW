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
import com.ispw.bean.UtenteSelezioneBean;
import com.ispw.bean.UtentiBean;
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
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

public final class LogicControllerApplicaPenalita {

    private static final String MSG_OK                 = "Operazione completata";
    private static final String MSG_INPUT_NON_VALIDO   = "Dati penalità non validi";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_IMPORTO_NON_VALIDO = "Importo penalità non valido";

    // ✅ nuovo (solo messaggio, nessuna dipendenza/import)
    private static final String MSG_UTENTE_NON_VALIDO  = "Utente non valido per penalita";

    @SuppressWarnings("java:S1312")
    private static final Logger LOGGER =
            Logger.getLogger(LogicControllerApplicaPenalita.class.getName());

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }
    private PenalitaDAO penalitaDAO() {
        return DAOFactory.getInstance().getPenalitaDAO();
    }
    private RegolePenalitaDAO regolePenalitaDAO() {
        return DAOFactory.getInstance().getRegolePenalitaDAO();
    }

    public UtentiBean listaUtentiPerPenalita() {
        UtentiBean out = new UtentiBean();
        out.setUtenti(userDAO().findAll().stream()
            .filter(u -> u != null && u.getRuolo() == Ruolo.UTENTE)
            .map(this::toBean)
            .toList());
        return out;
    }

    public EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati,
                                               DatiPagamentoBean datiPagamento,
                                               DatiFatturaBean   datiFattura,
                                               GestionePagamentoPenalita payCtrl,
                                               GestioneFatturaPenalita   fattCtrl,
                                               GestioneNotificaPenalita  notiCtrl) {

        if (!isDatiPenalitaValidi(dati)) {
            return esito(false, MSG_INPUT_NON_VALIDO);
        }

        final int idUtente = dati.getIdUtente();
        final GeneralUser user = userDAO().findById(idUtente);
        if (user == null) {
            return esito(false, MSG_UTENTE_INESISTENTE);
        }

        //blindatura: penalità applicabili solo agli utenti finali
        if (user.getRuolo() != Ruolo.UTENTE) {
            return esito(false, MSG_UTENTE_NON_VALIDO);
        }

        final BigDecimal importo = resolveImportoOrDefault(dati);
        if (!isImportoValido(importo)) {
            return esito(false, MSG_IMPORTO_NON_VALIDO);
        }

        final int idPenalita = persistOrComputeId(dati, importo);

        handleNotifica(notiCtrl, idUtente);
        handlePagamento(payCtrl, datiPagamento, idPenalita, importo);
        handleFattura(fattCtrl, datiFattura, idPenalita);

        blockAccountSafe(user);

        appendLogSafe(
            idUtente,
            "[PENALITA] id=" + idPenalita +
            " importo=" + importo +
            " motivo=" + safe(dati.getMotivazione())
        );

        return esito(true, MSG_OK);
    }

    private boolean isDatiPenalitaValidi(DatiPenalitaBean d) {
        return d != null
            && d.getIdUtente() > 0
            && !isBlank(d.getMotivazione());
    }

    private boolean isImportoValido(BigDecimal imp) {
        return imp != null && imp.compareTo(BigDecimal.ZERO) > 0;
    }

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

    private int persistOrComputeId(DatiPenalitaBean d, BigDecimal importo) {
        try {
            Penalita p = new Penalita();
            p.setIdUtente(d.getIdUtente());
            p.setMotivazione(d.getMotivazione());
            p.setImporto(importo);
            penalitaDAO().store(p);
            if (p.getIdPenalita() > 0) return p.getIdPenalita();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Persistenza Penalita fallita: {0}", new Object[]{ex.getMessage()});
        }

        int hash = Math.abs(Objects.hash(
                d.getIdUtente(),
                safe(d.getMotivazione()),
                importo != null ? importo.stripTrailingZeros().toPlainString() : "0",
                LocalDate.now()
        ));
        return (hash == 0 ? 1 : hash);
    }

    private void appendLogSafe(int idUtente, String descrizione) {
        log().log(Level.INFO, () -> "UTENTE=" + idUtente + " " + descrizione);

        try {
            LogDAO ldao = DAOFactory.getInstance().getLogDAO();
            if (ldao == null) return;

            SystemLog sl = new SystemLog();
            sl.setIdUtenteCoinvolto(idUtente);
            sl.setDescrizione(descrizione);
            sl.setTimestamp(LocalDateTime.now());
            ldao.append(sl);
        } catch (RuntimeException ex) {
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

    private void blockAccountSafe(GeneralUser user) {
        if (user == null) return;

        // ✅ extra difensivo: non sospendere mai un gestore
        if (user.getRuolo() != Ruolo.UTENTE) return;

        try {
            user.setStatoAccount(StatoAccount.SOSPESO);
            userDAO().store(user);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Blocco account fallito: {0}", new Object[]{ex.getMessage()});
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

    private UtenteSelezioneBean toBean(GeneralUser u) {
        UtenteSelezioneBean bean = new UtenteSelezioneBean();
        if (u == null) return bean;

        bean.setIdUtente(u.getIdUtente());
        bean.setEmail(u.getEmail());
        bean.setRuolo(u.getRuolo());
        return bean;
    }

    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }
}
