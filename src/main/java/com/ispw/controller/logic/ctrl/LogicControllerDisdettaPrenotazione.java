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
import com.ispw.controller.logic.ServiceFactory;
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
import com.ispw.model.entity.GeneralUser;
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
 * Controller applicativo del caso d'uso "Disdici prenotazione".
 *
 * Il caso d'uso è a due attori:
 * - l'utente crea una richiesta di disdetta;
 * - il gestore approva o rifiuta la richiesta.
 *
 * La prenotazione non viene annullata quando l'utente crea la richiesta:
 * viene soltanto salvata una RichiestaDisdettaRimborso in stato PENDING.
 *
 * Se il gestore approva, la richiesta viene eseguita e la prenotazione viene annullata.
 * Se il gestore rifiuta, la prenotazione resta invariata.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo questi metodi pubblici e non conosce i DAO
 * né i controller secondari usati internamente.
 */
public class LogicControllerDisdettaPrenotazione {

    // Messaggi comuni
    private static final String MSG_INPUT_NON_VALIDO = "Input non valido";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_PREN_INESISTENTE = "Prenotazione inesistente";
    private static final String MSG_GIA_ANNULLATA = "Prenotazione gia annullata";
    private static final String MSG_DISDETTA_OK = "Disdetta eseguita";
    private static final String MSG_DISDETTA_KO = "Disdetta non consentita";

    // Messaggi specifici del flusso a due attori
    private static final String MSG_RICHIESTA_OK = "Richiesta disdetta inviata";
    private static final String MSG_RICHIESTA_GIA_ESISTE = "Richiesta già presente per la prenotazione";
    private static final String MSG_RICHIESTA_ASSENTE = "Richiesta inesistente";
    private static final String MSG_RICHIESTA_GIA_GESTITA = "Richiesta già gestita";
    private static final String MSG_SOLO_GESTORE = "Operazione riservata al gestore";
    private static final String MSG_SOLO_UTENTE = "Operazione riservata all'utente";
    private static final String MSG_RICHIESTA_RIFIUTATA = "Richiesta rifiutata";
    private static final String MSG_RICHIESTA_ESEGUITA = "Richiesta approvata ed eseguita";
    private static final String MSG_APPROVA_KO = "Approvata ma esecuzione fallita";

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    // =====================================================================
    // DAO
    // =====================================================================
    // I DAO vengono recuperati dalla DAOFactory.
    // In questo modo il controller lavora sulle interfacce e non conosce
    // se la persistenza è su DBMS, filesystem o in-memory.

    private PrenotazioneDAO prenotazioneDAO() {
        return DAOFactory.getInstance().getPrenotazioneDAO();
    }

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    private PagamentoDAO pagamentoDAO() {
        return DAOFactory.getInstance().getPagamentoDAO();
    }

    private LogDAO logDAO() {
        return DAOFactory.getInstance().getLogDAO();
    }

    private RegoleTempisticheDAO tempisticheDAO() {
        return DAOFactory.getInstance().getRegoleTempisticheDAO();
    }

    private RegolePenalitaDAO penalitaDAO() {
        return DAOFactory.getInstance().getRegolePenalitaDAO();
    }

    private RichiestaDisdettaDAO richiestaDAO() {
        return DAOFactory.getInstance().getRichiestaDisdettaDAO();
    }

    // =====================================================================
    // SERVICE CONTROLLER
    // =====================================================================
    // I controller secondari vengono recuperati dalla ServiceFactory.
    // In questo modo il controller principale resta stateless e non dipende
    // direttamente dalle implementazioni concrete dei servizi applicativi.

    private GestionePagamentoRimborso payCtrl() {
        return ServiceFactory.getPagamentoRimborsoService();
    }

    private GestioneFatturaRimborso fattCtrl() {
        return ServiceFactory.getFatturaRimborsoService();
    }

    private GestioneNotificaDisdetta notiCtrl() {
        return ServiceFactory.getNotificaDisdettaService();
    }

    private GestioneDisponibilitaDisdetta dispCtrl() {
        return ServiceFactory.getDisponibilitaDisdettaService();
    }

    // STEP 0: consultazione prenotazioni e anteprima

    /**
     * Restituisce le prenotazioni dell'utente che possono ancora essere disdette.
     *
     * Una prenotazione è cancellabile se:
     * - non è già annullata;
     * - ha data e ora valide;
     * - è futura rispetto al momento corrente.
     */
    public List<RiepilogoPrenotazioneBean> ottieniPrenotazioniCancellabili(UtenteBean utente) {
        //controllo sul bean
        if (utente == null || LogicControllerHelper.isBlank(utente.getEmail())) {
            return List.of();
        }

        //controlla se l'utente (findbyemail) esiste e se ha RUOLO==UTENTE
        GeneralUser user = userDAO().findByEmail(LogicControllerHelper.normalizeEmail(utente.getEmail()));
        if (user == null || user.getRuolo() != Ruolo.UTENTE) {
            return List.of();
        }

        //genera la lista di prenotazioni collegate all'id dell'utente
        List<Prenotazione> prenotazioni = prenotazioneDAO().findByUtente(user.getIdUtente());

        //memorizza il time attuale
        LocalDateTime now = LocalDateTime.now();

        //genera una lista riepilogo prenotazionebean che vuota
        List<RiepilogoPrenotazioneBean> out = new ArrayList<>();

        //controlla se è cancellabile tramite metodo helper is cancellable
        for (Prenotazione prenotazione : prenotazioni) {
            if (!isCancellablePrenotazione(prenotazione, now)) {
                continue;
            }

            //se passa viene generata l'istanza della prenotazione cancellabile
            RiepilogoPrenotazioneBean riepilogo = new RiepilogoPrenotazioneBean();
            riepilogo.setIdPrenotazione(prenotazione.getIdPrenotazione());
            riepilogo.setUtente(utente);
            riepilogo.setImportoTotale(getImportoPagato(prenotazione.getIdPrenotazione()));
            riepilogo.setDatiFiscali(null);

            //infine aggiunge la singola prenotazione cancellabile alla lista
            out.add(riepilogo);
        }

        //ritorna la lista al graphic controller che la mostrarà all'utente
        return out;
    }

    /**
     * Metodo che serve al layer grafico per la schermata di anteprima.
     *
     * In pratica, quando l'utente seleziona una prenotazione e preme
     * "Calcola Penale", il graphic controller può chiamare questo metodo.
     *
     * Il metodo non crea nessuna richiesta e non modifica la prenotazione:
     * si limita a controllare se la disdetta è possibile e a calcolare la penale.
     */
    public EsitoDisdettaBean anteprimaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        return validaDisdetta(idPrenotazione, sessione);
    }

    // STEP 1: UTENTE crea richiesta PENDING

    /**
     * Crea una richiesta di disdetta in stato PENDING.
     *
     * Questo metodo non annulla la prenotazione.
     * Salva soltanto la richiesta con:
     * - id della prenotazione;
     * - id dell'utente;
     * - penale stimata;
     * - rimborso stimato;
     * - nota dell'utente;
     * - timestamp della richiesta.
     *
     * La richiesta sarà poi valutata dal gestore.
     *
     * Nota importante:
     * la chiamata Java a questo metodo è sincrona, perché restituisce subito
     * un esito al graphic controller. Il caso d'uso però è differito:
     * la disdetta rimane PENDING finché il gestore non la approva o rifiuta.
     */
    public EsitoOperazioneBean richiediDisdetta(int idPrenotazione, String notaUtente, SessioneUtenteBean sessione) {
        //controllo sull'input
        if (sessione == null || sessione.getUtente() == null
                || LogicControllerHelper.isBlank(sessione.getUtente().getEmail())) {
            return LogicControllerHelper.esito(false, MSG_INPUT_NON_VALIDO);
        }

        //mi definisco la mia entity run time attraverso resolveUserFromSessione e sessione e faccio i relativi controlli
        GeneralUser user = resolveUserFromSession(sessione);
        if (user == null) {
            return LogicControllerHelper.esito(false, MSG_UTENTE_INESISTENTE);
        }

        if (user.getRuolo() != Ruolo.UTENTE) {
            return LogicControllerHelper.esito(false, MSG_SOLO_UTENTE);
        }

        /*
         * Prima di creare una richiesta si verifica che la disdetta sia possibile.
         * Questa è la "fotografia" del momento in cui l'utente invia la richiesta.
         */
        EsitoDisdettaBean preview = validaDisdetta(idPrenotazione, sessione);
        if (!preview.isPossibile()) {
            return LogicControllerHelper.esito(false, MSG_DISDETTA_KO);
        }

        /*
         * Evita di creare più richieste PENDING per la stessa prenotazione.
         * Se ne esiste già una in attesa, l'utente deve aspettare la decisione del gestore.
         */
        //controllo che la richiesta non sia già presente richiamando il metodo del dao findbyPrenotazione
        RichiestaDisdettaRimborso existing = richiestaDAO().findByPrenotazione(idPrenotazione);
        if (existing != null && existing.getStato() == StatoRichiestaDisdetta.PENDING) {
            return LogicControllerHelper.esito(false, MSG_RICHIESTA_GIA_ESISTE);
        }

        //definisco i valori della richiesta di disdetta
        float penale = Math.max(0f, preview.getPenale());
        float importoPagato = getImportoPagato(idPrenotazione);
        float rimborsoStimato = Math.max(0f, importoPagato - penale);

        //creo una nuova istanza richiesta di disdetta in stato Pending (siccome ho verificato che non esiste già una),
        //questa entity servirà a coordinare il caso d'uso complesso, che si baserà sullo stato di questa entity
        RichiestaDisdettaRimborso richiesta = new RichiestaDisdettaRimborso();
        richiesta.setIdPrenotazione(idPrenotazione);
        richiesta.setIdUtente(user.getIdUtente());
        richiesta.setTimestampRichiesta(LocalDateTime.now());
        richiesta.setPenaleStimata(BigDecimal.valueOf(penale));
        richiesta.setRimborsoStimato(BigDecimal.valueOf(rimborsoStimato));
        richiesta.setStato(StatoRichiestaDisdetta.PENDING);
        richiesta.setNotaUtente(notaUtente);

        //dopo averla creata in memoria la persisto
        richiestaDAO().store(richiesta);

        //faccio l'append dell'evento per tracciabilità
        appendLogSafe(user.getIdUtente(),
                "RICHIESTA_DISDETTA #" + richiesta.getIdRichiesta()
                        + " pren=" + idPrenotazione
                        + " penaleStimata=" + penale
                        + " rimborsoStimato=" + rimborsoStimato);

        //infine ritorno l'esito al graphic controller
        return LogicControllerHelper.esito(true, MSG_RICHIESTA_OK);
    }

    // STEP 2: GESTORE consulta richieste PENDING

    /**
     * Restituisce al gestore le richieste ancora da valutare.
     *
     * Il metodo restituisce bean e non entity, così il layer grafico
     * non dipende direttamente dalle classi del model persistente.
     *
     * Questa lista dovrebbe essere usata dalla schermata del gestore
     * per mostrare le richieste in attesa.
     */
    public List<RichiestaDisdettaBean> listaRichiestePending(SessioneUtenteBean sessioneGestore) {
        //controlla se il ruolo dell'utente della sessione è == gestore
        if (!isGestore(sessioneGestore)) {
            return List.of();
        }

        //recupera dal dao le richieste in stato pending e le inserisce nella lista che verrà passata al graphicController
        return richiestaDAO().findByStato(StatoRichiestaDisdetta.PENDING).stream()
                .filter(r -> r != null)
                .map(this::toBean)
                .toList();
    }

    // STEP 3: GESTORE approva o rifiuta

    /**
     * Permette al gestore di approvare o rifiutare una richiesta di disdetta.
     *
     * Se la richiesta viene rifiutata:
     * - la richiesta passa a RIFIUTATA;
     * - la prenotazione resta invariata.
     *
     * Se la richiesta viene approvata:
     * - la richiesta passa ad APPROVATA;
     * - il sistema prova ad annullare la prenotazione;
     * - se l'annullamento va a buon fine, la richiesta passa a ESEGUITA.
     */
    public EsitoOperazioneBean valutaRichiestaDisdetta(
            int idRichiesta,
            //approva è la variabile scelta dal gestore che definisce se ha approvato o no la disdetta
            boolean approva,
            String notaGestore,
            SessioneUtenteBean sessioneGestore) {

        //controlli del gestore e che l'input sia valido
        if (!isGestore(sessioneGestore)) {
            return LogicControllerHelper.esito(false, MSG_SOLO_GESTORE);
        }

        if (idRichiesta <= 0) {
            return LogicControllerHelper.esito(false, MSG_INPUT_NON_VALIDO);
        }

        //carico la richiesta dallo strato di persistenza tramite il suo id
        RichiestaDisdettaRimborso richiesta = richiestaDAO().load(idRichiesta);

        //controllo se esiste
        if (richiesta == null) {
            return LogicControllerHelper.esito(false, MSG_RICHIESTA_ASSENTE);
        }

        //controllo se lo stato è davvero pending
        if (richiesta.getStato() != StatoRichiestaDisdetta.PENDING) {
            return LogicControllerHelper.esito(false, MSG_RICHIESTA_GIA_GESTITA);
        }

        //carico l'id del gestore che servirà nel metodo update del dao
        Integer idGestore = resolveGestoreId(sessioneGestore);

        //se approva == FALSE
        if (!approva) {
            //aggiorno lo stato della richiesta a rifiutata
            richiestaDAO().updateStato(idRichiesta, StatoRichiestaDisdetta.RIFIUTATA, idGestore, notaGestore);

            //aggiungo il log dell'evento
            appendLogSafe(richiesta.getIdUtente(), "RICHIESTA_DISDETTA RIFIUTATA #" + idRichiesta);

            //return esito al graphic controller
            return LogicControllerHelper.esito(true, MSG_RICHIESTA_RIFIUTATA);
        }

        //approva è == TRUE
        //cambio lo stato della richiesta in approvata, quindi manca solo eseguirla concretamente
        richiestaDAO().updateStato(idRichiesta, StatoRichiestaDisdetta.APPROVATA, idGestore, notaGestore);

        //mi carico l'istanza dell'utente tramite l'id presente nella richiesta di disdetta
        GeneralUser user = userDAO().findById(richiesta.getIdUtente());

        //controllo se esiste
        if (user == null) {
            return LogicControllerHelper.esito(false, MSG_UTENTE_INESISTENTE);
        }

        //eseguo l'annullamento concreto della prenotazione
        EsitoOperazioneBean exec = eseguiAnnullamentoDaRichiesta(richiesta, toUtenteBean(user));

        //controllo se l'esito dell'annullamento della prenotazione è andato bene
        if (exec.isSuccesso()) {
            //cambio lo stato della richiesta da approvata ad eseguita
            richiestaDAO().updateStato(idRichiesta, StatoRichiestaDisdetta.ESEGUITA, idGestore, notaGestore);

            //segno il log dell'evento
            appendLogSafe(richiesta.getIdUtente(), "RICHIESTA_DISDETTA ESEGUITA #" + idRichiesta);

            //ritorno l'esito
            return LogicControllerHelper.esito(true, MSG_RICHIESTA_ESEGUITA);
        }

        //se l'annullamento non ha avuto successo ritorno esito approvato ma non eseguito
        return LogicControllerHelper.esito(false, MSG_APPROVA_KO);
    }

    // ESECUZIONE TECNICA DELLA RICHIESTA APPROVATA

    /**
     * Esegue l'annullamento tecnico di una prenotazione partendo da una richiesta approvata.
     *
     * Questo metodo è privato perché non rappresenta un'azione diretta della UI.
     * Viene chiamato solo dopo che il gestore ha approvato una richiesta.
     * Non richiama validaDisdetta(...), perché la verifica è già stata fatta
     * quando l'utente ha creato la richiesta.
     */
    private EsitoOperazioneBean eseguiAnnullamentoDaRichiesta(
            RichiestaDisdettaRimborso richiesta,
            UtenteBean utente) {

        //dalla richiesta mi prendo l'id prenotazione
        int idPrenotazione = richiesta.getIdPrenotazione();

        //grazie all'id prenotazione mi carico l'istanza prenotazione
        Prenotazione prenotazione = prenotazioneDAO().load(idPrenotazione);

        //controllo se esiste
        if (prenotazione == null) {
            return LogicControllerHelper.esito(false, MSG_PREN_INESISTENTE);
        }

        //controllo se è già stata annullata
        if (prenotazione.getStato() == StatoPrenotazione.ANNULLATA) {
            return LogicControllerHelper.esito(false, MSG_GIA_ANNULLATA);
        }

        //carico la penale per calcolare il rimborso
        float penale = getPenaleStimata(richiesta);

        /*
         * I controller secondari vengono recuperati tramite ServiceFactory.
         * In questo modo non vengono passati come parametri e non vengono creati
         * direttamente con new dentro il controller principale.
         */

        //esegue il rimborso definendo pagamento e fattura
        processRefundIfConfirmed(prenotazione, idPrenotazione, penale);

        /*
         * Prima liberiamo lo slot, poi eliminiamo la prenotazione.
         * Così eventuali collaboratori che cercano la prenotazione la trovano ancora.
         */
        //libero lo slot che era occupato
        liberaRisorsa(idPrenotazione);

        //cancello la prenotazione
        prenotazioneDAO().delete(idPrenotazione);

        //mando la notifica e segno l'evento con un log
        notifyAndLog(utente, richiesta.getIdUtente(), idPrenotazione, penale);

        //ritorno l'esito della disdetta
        return LogicControllerHelper.esito(true, MSG_DISDETTA_OK);
    }

    // VALIDAZIONE E CALCOLO PENALE

    /**
     * Verifica se l'utente può creare una richiesta di disdetta
     * per la prenotazione indicata con una serie di controlli.
     *
     * Questo metodo viene usato due volte:
     * - nella schermata di anteprima;
     * - prima di creare effettivamente la richiesta PENDING.
     */
    private EsitoDisdettaBean validaDisdetta(int idPrenotazione, SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null
                || LogicControllerHelper.isBlank(sessione.getUtente().getEmail())) {
            return invalidoEsito();
        }

        GeneralUser user = resolveUserFromSession(sessione);
        if (user == null || user.getRuolo() != Ruolo.UTENTE) {
            return invalidoEsito();
        }

        Prenotazione prenotazione = prenotazioneDAO().load(idPrenotazione);
        if (prenotazione == null) {
            return invalidoEsito();
        }

        if (prenotazione.getIdUtente() != user.getIdUtente()) {
            return invalidoEsito();
        }

        if (!isCancellablePrenotazione(prenotazione, LocalDateTime.now())) {
            return invalidoEsito();
        }

        //carico le regole tempistiche e di penalità
        RegoleTempistiche rt = loadRegoleTempisticheSafe();
        RegolePenalita rp = loadRegolePenalitaSafe();

        //creo un esito e setto la penale
        EsitoDisdettaBean out = new EsitoDisdettaBean();
        out.setPossibile(true);
        out.setPenale(calcolaPenale(prenotazione, rt, rp));
        return out;
    }

    // Esito standard per una disdetta non valida utilizzato se quei controlli in validaDisdetta falliscono.
    private EsitoDisdettaBean invalidoEsito() {
        EsitoDisdettaBean out = new EsitoDisdettaBean();
        out.setPossibile(false);
        out.setPenale(0f);
        return out;
    }

    /**
     * Calcola la penale in base al tempo residuo prima della prenotazione.
     *
     * Se il preavviso è sufficiente, la penale è zero.
     * Altrimenti viene usato il valore configurato nelle regole penalità.
     */
    private float calcolaPenale(Prenotazione prenotazione, RegoleTempistiche rt, RegolePenalita rp) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(prenotazione.getData(), prenotazione.getOraInizio());
        long minResidui = Duration.between(now, start).toMinutes();

        int preavvisoMin = 0;

        //serie di controlli su preavviso minimo
        if (rt != null) {
            preavvisoMin = Math.max(preavvisoMin, rt.getPreavvisoMinimo());
        }

        if (rp != null && preavvisoMin <= 0) {
            preavvisoMin = Math.max(preavvisoMin, rp.getPreavvisoMinimo());
        }

        //penale 0
        if (minResidui >= preavvisoMin) {
            return 0f;
        }

        //caso in cui il valore penalità non è impostato
        if (rp == null || rp.getValorePenalita() == null) {
            return 0f;
        }

        return Math.max(0f, rp.getValorePenalita().floatValue());
    }

    // =====================================================================
    // EFFETTI DELL'ANNULLAMENTO
    // =====================================================================

    /**
     * Libera lo slot occupato dalla prenotazione.
     *
     * È best-effort: se fallisce, il flusso non viene interrotto.
     */
    private void liberaRisorsa(int idPrenotazione) {
        try {
            dispCtrl().liberaSlot(idPrenotazione);
            log().fine(() -> "[DISDETTA] Slot liberato per prenotazione #" + idPrenotazione);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Rilascio slot fallito: {0}", ex.getMessage());
        }
    }

    /**
     * Esegue il rimborso solo se la prenotazione era confermata.
     *
     * Il rimborso effettivo viene calcolato sull'importo pagato,
     * sottraendo la penale salvata nella richiesta.
     */
    private void processRefundIfConfirmed(Prenotazione prenotazione,
                                          int idPrenotazione,
                                          float penale) {
        if (prenotazione.getStato() != StatoPrenotazione.CONFERMATA) {
            return;
        }

        float importoPagato = getImportoPagato(idPrenotazione);
        float rimborso = Math.max(0f, importoPagato - penale);
        if (rimborso <= 0f) {
            return;
        }

        try {
            payCtrl().eseguiRimborso(idPrenotazione, rimborso);
            fattCtrl().emettiNotaDiCredito(idPrenotazione);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Rimborso fallito: {0}", ex.getMessage());
        }
    }

    /**
     * Invia la conferma di cancellazione e scrive il log applicativo.
     */
    private void notifyAndLog(UtenteBean utente,
                              int idUtente,
                              int idPrenotazione,
                              float penale) {
        try {
            notiCtrl().inviaConfermaCancellazione(
                    utente,
                    "Prenotazione #" + idPrenotazione + " annullata (penale " + penale + " EUR)"
            );
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica disdetta fallita: {0}", ex.getMessage());
        }

        appendLogSafe(idUtente, "DISDETTA_PRENOTAZIONE #" + idPrenotazione + " - penale " + penale + " EUR");
    }

    // =====================================================================
    // HELPERS SPECIFICI DELLA DISDETTA
    // =====================================================================

    /**
     * Controlla se una prenotazione è futura e non annullata.
     */
    private boolean isCancellablePrenotazione(Prenotazione prenotazione, LocalDateTime now) {
        if (prenotazione == null || prenotazione.getStato() == StatoPrenotazione.ANNULLATA) {
            return false;
        }

        if (prenotazione.getData() == null || prenotazione.getOraInizio() == null) {
            return false;
        }

        LocalDateTime inizio = LocalDateTime.of(prenotazione.getData(), prenotazione.getOraInizio());
        return inizio.isAfter(now);
    }

    /**
     * Recupera l'importo effettivamente pagato per una prenotazione.
     */
    private float getImportoPagato(int idPrenotazione) {
        Pagamento pag = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pag == null || pag.getImportoFinale() == null) {
            return 0f;
        }
        return pag.getImportoFinale().floatValue();
    }

    /**
     * Estrae la penale salvata nella richiesta.
     */
    private float getPenaleStimata(RichiestaDisdettaRimborso richiesta) {
        BigDecimal penale = richiesta.getPenaleStimata();
        if (penale == null) {
            return 0f;
        }
        return Math.max(0f, penale.floatValue());
    }

    /**
     * Carica le regole tempistiche senza far fallire il flusso.
     */
    private RegoleTempistiche loadRegoleTempisticheSafe() {
        try {
            return tempisticheDAO().get();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento regole tempistiche fallito: {0}", ex.getMessage());
            return null;
        }
    }

    /**
     * Carica le regole penalità senza far fallire il flusso.
     */
    private RegolePenalita loadRegolePenalitaSafe() {
        try {
            return penalitaDAO().get();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento regole penalità fallito: {0}", ex.getMessage());
            return null;
        }
    }

    /**
     * Converte l'utente di dominio nel bean usato dalla notifica.
     */
    private UtenteBean toUtenteBean(GeneralUser user) {
        return new UtenteBean(
                user.getNome(),
                user.getCognome(),
                user.getEmail(),
                user.getRuolo()
        );
    }

    /**
     * Recupera l'id del gestore che sta valutando la richiesta.
     */
    private Integer resolveGestoreId(SessioneUtenteBean sessioneGestore) {
        GeneralUser gestore = resolveUserFromSession(sessioneGestore);
        return gestore != null ? gestore.getIdUtente() : null;
    }

    /**
     * Scrive un log senza interrompere il flusso in caso di errore.
     */
    private void appendLogSafe(int idUtente, String descr) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setTimestamp(LocalDateTime.now());
            systemLog.setIdUtenteCoinvolto(idUtente);
            systemLog.setDescrizione(descr);
            logDAO().append(systemLog);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log disdetta fallito: {0}", ex.getMessage());
        }
    }

    /**
     * Risolve l'utente reale partendo dalla sessione.
     */
    private GeneralUser resolveUserFromSession(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null
                || LogicControllerHelper.isBlank(sessione.getUtente().getEmail())) {
            return null;
        }
        return userDAO().findByEmail(LogicControllerHelper.normalizeEmail(sessione.getUtente().getEmail()));
    }

    /**
     * Verifica che la sessione appartenga a un gestore.
     */
    private boolean isGestore(SessioneUtenteBean sessione) {
        return sessione != null
                && sessione.getUtente() != null
                && sessione.getUtente().getRuolo() == Ruolo.GESTORE;
    }

    // =====================================================================
    // MAPPING ENTITY -> BEAN
    // =====================================================================

    /**
     * Converte la richiesta persistente in bean per la UI.
     */
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