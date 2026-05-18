package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteSelezioneBean;
import com.ispw.bean.UtentiBean;
import com.ispw.controller.logic.ServiceFactory;
import com.ispw.controller.logic.interfaces.CtrlApplicaPenalita;
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

/**
 * Controller applicativo del caso d'uso "Applica penalità".
 *
 * Il caso d'uso è pensato per il gestore, che può applicare una penalità
 * a un utente finale.
 *
 * Il metodo principale:
 * - controlla i dati inseriti;
 * - recupera l'utente coinvolto;
 * - calcola o recupera l'importo della penalità;
 * - salva la penalità;
 * - gestisce pagamento, fattura e notifica;
 * - sospende l'account dell'utente;
 * - registra l'operazione nel log.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo i metodi pubblici e non conosce i DAO
 * né i controller secondari usati internamente.
 */
public final class LogicControllerApplicaPenalita implements CtrlApplicaPenalita {

    // Messaggi comuni
    private static final String MSG_OK = "Operazione completata";
    private static final String MSG_INPUT_NON_VALIDO = "Dati penalità non validi";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_IMPORTO_NON_VALIDO = "Importo penalità non valido";
    private static final String MSG_UTENTE_NON_VALIDO = "Utente non valido per penalita";
    private static final String MSG_SALVATAGGIO_KO = "Salvataggio penalità fallito";

    @SuppressWarnings("java:S1312")
    private static final Logger LOGGER =
            Logger.getLogger(LogicControllerApplicaPenalita.class.getName());

    // =====================================================================
    // DAO
    // =====================================================================
    // I DAO vengono recuperati dalla DAOFactory.
    // In questo modo il controller lavora sulle interfacce e non conosce
    // se la persistenza è su DBMS, filesystem o in-memory.

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    private PenalitaDAO penalitaDAO() {
        return DAOFactory.getInstance().getPenalitaDAO();
    }

    private RegolePenalitaDAO regolePenalitaDAO() {
        return DAOFactory.getInstance().getRegolePenalitaDAO();
    }

    private LogDAO logDAO() {
        return DAOFactory.getInstance().getLogDAO();
    }

    // =====================================================================
    // SERVICE CONTROLLER
    // =====================================================================
    // I controller secondari vengono recuperati dalla ServiceFactory.
    // In questo modo il controller principale resta stateless e non dipende
    // direttamente dalle implementazioni concrete dei servizi applicativi.

    private GestionePagamentoPenalita payCtrl() {
        return ServiceFactory.getPagamentoPenalitaService();
    }

    private GestioneFatturaPenalita fattCtrl() {
        return ServiceFactory.getFatturaPenalitaService();
    }

    private GestioneNotificaPenalita notiCtrl() {
        return ServiceFactory.getNotificaPenalitaService();
    }

    // STEP 0: lista utenti selezionabili

    /**
     * Restituisce la lista degli utenti a cui è possibile applicare una penalità.
     *
     * In questo caso vengono mostrati solo gli utenti finali.
     */
    @Override
    public UtentiBean listaUtentiPerPenalita() {
        UtentiBean out = new UtentiBean();

        out.setUtenti(userDAO().findAll().stream()
                .filter(u -> u != null && u.getRuolo() == Ruolo.UTENTE)
                .map(this::toBean)
                .toList());

        return out;
    }

    // STEP 1: applicazione della penalità

    /**
     * Applica una penalità a un utente.
     *
     * Il metodo:
     * - controlla i dati inseriti;
     * - recupera l'utente;
     * - determina l'importo;
     * - salva la penalità;
     * - gestisce notifica, pagamento e fattura;
     * - sospende l'account dell'utente;
     * - registra l'operazione nel log.
     */
    @Override
    public EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati,
                                               DatiPagamentoBean datiPagamento,
                                               DatiFatturaBean datiFattura) {

        //controllo che i dati minimi della penalità siano validi
        if (!isDatiPenalitaValidi(dati)) {
            return LogicControllerHelper.esito(false, MSG_INPUT_NON_VALIDO);
        }

        //recupero l'utente a cui applicare la penalità
        final int idUtente = dati.getIdUtente();
        final GeneralUser user = userDAO().findById(idUtente);
        if (user == null) {
            return LogicControllerHelper.esito(false, MSG_UTENTE_INESISTENTE);
        }

        //la penalità può essere applicata solo agli utenti finali
        if (user.getRuolo() != Ruolo.UTENTE) {
            return LogicControllerHelper.esito(false, MSG_UTENTE_NON_VALIDO);
        }

        //se l'importo non è presente, provo a recuperarlo dalle regole
        final BigDecimal importo = resolveImportoOrDefault(dati);
        if (!isImportoValido(importo)) {
            return LogicControllerHelper.esito(false, MSG_IMPORTO_NON_VALIDO);
        }

        //salvo la penalità e recupero l'id assegnato dal DAO
        final int idPenalita = salvaPenalita(dati, importo);
        if (idPenalita <= 0) {
            return LogicControllerHelper.esito(false, MSG_SALVATAGGIO_KO);
        }

        //gestisco gli effetti collegati alla penalità
        handleNotifica(idUtente);
        handlePagamento(datiPagamento, idPenalita, importo);
        handleFattura(datiFattura, idPenalita);

        //dopo la penalità, l'account dell'utente viene sospeso
        blockAccountSafe(user);

        //registro l'operazione nel log applicativo
        appendLogSafe(
                idUtente,
                "[PENALITA] id=" + idPenalita
                        + " importo=" + importo
                        + " motivo=" + LogicControllerHelper.safe(dati.getMotivazione())
        );

        return LogicControllerHelper.esito(true, MSG_OK);
    }

    // =====================================================================
    // VALIDAZIONE E IMPORTO
    // =====================================================================

    /**
     * Controlla che i dati minimi della penalità siano presenti.
     */
    private boolean isDatiPenalitaValidi(DatiPenalitaBean d) {
        return d != null
                && d.getIdUtente() > 0
                && !LogicControllerHelper.isBlank(d.getMotivazione());
    }

    /**
     * Controlla che l'importo sia positivo.
     */
    private boolean isImportoValido(BigDecimal imp) {
        return imp != null && imp.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Recupera l'importo della penalità.
     *
     * Se l'importo è già presente nel bean, viene usato quello.
     * Altrimenti viene usato il valore configurato nelle regole di penalità.
     */
    private BigDecimal resolveImportoOrDefault(DatiPenalitaBean d) {
        BigDecimal imp = d.getImporto();
        if (isImportoValido(imp)) {
            return imp;
        }

        try {
            RegolePenalita r = regolePenalitaDAO().get();
            if (r != null && isImportoValido(r.getValorePenalita())) {
                return r.getValorePenalita();
            }
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Lettura RegolePenalita fallita: {0}", ex.getMessage());
        }

        return imp;
    }

    // =====================================================================
    // PERSISTENZA PENALITÀ
    // =====================================================================

    /**
     * Salva la penalità tramite DAO.
     *
     * Se il salvataggio va a buon fine, restituisce l'id della penalità.
     * Se qualcosa va storto, restituisce 0.
     */
    private int salvaPenalita(DatiPenalitaBean d, BigDecimal importo) {
        try {
            Penalita p = new Penalita();
            p.setIdUtente(d.getIdUtente());
            p.setMotivazione(d.getMotivazione());
            p.setImporto(importo);

            penalitaDAO().store(p);
            return p.getIdPenalita();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Salvataggio penalità fallito: {0}", ex.getMessage());
            return 0;
        }
    }

    // =====================================================================
    // EFFETTI SECONDARI
    // =====================================================================

    /**
     * Invia la notifica della penalità.
     *
     * Se il controller non è disponibile, il flusso principale continua.
     */
    private void handleNotifica(int idUtente) {
        if (notiCtrl() == null) {
            return;
        }

        runBestEffort("Notifica penalità",
                () -> notiCtrl().inviaNotificaPenalita(String.valueOf(idUtente)));
    }

    /**
     * Gestisce il pagamento della penalità.
     */
    private void handlePagamento(DatiPagamentoBean datiPagamento,
                                 int idPenalita,
                                 BigDecimal importoPenalita) {
        if (payCtrl() == null || datiPagamento == null) {
            return;
        }

        normalizePagamento(datiPagamento, importoPenalita);

        runBestEffort("Pagamento penalità",
                () -> payCtrl().richiediPagamentoPenalita(datiPagamento, idPenalita));
    }

    /**
     * Gestisce la fattura collegata alla penalità.
     */
    private void handleFattura(DatiFatturaBean datiFattura,
                               int idPenalita) {
        if (fattCtrl() == null || datiFattura == null) {
            return;
        }

        normalizeFattura(datiFattura);

        runBestEffort("Fattura penalità",
                () -> fattCtrl().generaFatturaPenalita(datiFattura, idPenalita));
    }

    /**
     * Completa i dati di pagamento se alcuni valori non sono stati inseriti.
     */
    private void normalizePagamento(DatiPagamentoBean pay, BigDecimal importoPenalita) {
        if (pay.getImporto() <= 0f) {
            pay.setImporto(importoPenalita.floatValue());
        }

        if (LogicControllerHelper.isBlank(pay.getMetodo())) {
            pay.setMetodo("PAYPAL");
        }
    }

    /**
     * Completa i dati di fattura se manca la data dell'operazione.
     */
    private void normalizeFattura(DatiFatturaBean fat) {
        if (fat.getDataOperazione() == null) {
            fat.setDataOperazione(LocalDate.now());
        }
    }

    // =====================================================================
    // LOG E BLOCCO ACCOUNT
    // =====================================================================

    /**
     * Scrive il log della penalità.
     *
     * Se la scrittura fallisce, il caso d'uso non viene interrotto.
     */
    private void appendLogSafe(int idUtente, String descrizione) {
        log().log(Level.INFO, () -> "UTENTE=" + idUtente + " " + descrizione);

        try {
            LogDAO ldao = logDAO();
            if (ldao == null) {
                return;
            }

            SystemLog sl = new SystemLog();
            sl.setIdUtenteCoinvolto(idUtente);
            sl.setDescrizione(descrizione);
            sl.setTimestamp(LocalDateTime.now());

            ldao.append(sl);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Scrittura LogDAO fallita: {0}", ex.getMessage());
        }
    }

    /**
     * Esegue un'azione secondaria senza bloccare il flusso principale.
     */
    private void runBestEffort(String what, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "{0} fallita: {1}", new Object[]{what, ex.getMessage()});
        }
    }

    /**
     * Sospende l'account dell'utente dopo l'applicazione della penalità.
     */
    private void blockAccountSafe(GeneralUser user) {
        try {
            user.setStatoAccount(StatoAccount.SOSPESO);
            userDAO().store(user);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Blocco account fallito: {0}", ex.getMessage());
        }
    }

    // =====================================================================
    // MAPPING E LOGGER
    // =====================================================================

    /**
     * Restituisce il logger della classe.
     */
    private Logger log() {
        return LOGGER;
    }

    /**
     * Converte un GeneralUser nel bean usato dalla schermata di selezione utente.
     */
    private UtenteSelezioneBean toBean(GeneralUser u) {
        UtenteSelezioneBean bean = new UtenteSelezioneBean();

        if (u == null) {
            return bean;
        }

        bean.setIdUtente(u.getIdUtente());
        bean.setEmail(u.getEmail());
        bean.setRuolo(u.getRuolo());

        return bean;
    }
}