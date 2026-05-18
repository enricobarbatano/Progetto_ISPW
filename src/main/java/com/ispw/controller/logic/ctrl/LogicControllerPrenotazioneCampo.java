package com.ispw.controller.logic.ctrl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.CampoBean;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.bean.UtenteBean;
import com.ispw.controller.logic.ServiceFactory;
import com.ispw.controller.logic.interfaces.CtrlPrenotazione;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPrenotazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPrenotazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPrenotazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * Controller applicativo del caso d'uso "Prenota campo".
 *
 * Il caso d'uso permette a un utente finale di:
 * - consultare la lista dei campi;
 * - verificare gli slot disponibili;
 * - creare una prenotazione in stato DA_PAGARE;
 * - completare la prenotazione tramite pagamento;
 * - ricevere fattura e notifica di conferma.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo questi metodi pubblici e non conosce i DAO
 * né i controller secondari usati internamente.
 *
 * Responsabilità:
 * questa classe orchestra il caso d'uso di prenotazione.
 * Non gestisce direttamente i campi, non calcola direttamente la disponibilità
 * e non compone direttamente l'esito del pagamento.
 *
 * In particolare:
 * - i campi e la disponibilità sono responsabilità di GestioneDisponibilitaPrenotazione;
 * - il pagamento è responsabilità di GestionePagamentoPrenotazione;
 * - fattura e notifica restano effetti post-pagamento del caso d'uso.
 */
public class LogicControllerPrenotazioneCampo implements CtrlPrenotazione {

    private static final int DEFAULT_DURATA_MIN = 60;

    private static final String MSG_SLOT_NON_DISP = "[PRENOT] Slot non disponibile: campo={0} {1} {2}-{3}";
    private static final String MSG_NO_PREN_DA_PAG = "Nessuna prenotazione da pagare";
    private static final String MSG_INPUT_NON_VALIDO = "Input non valido";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_OPERAZIONE_NON_CONSENTITA = "Operazione non consentita";
    private static final String MSG_PAGAMENTO_RIFIUTATO = "Pagamento rifiutato";

    private static final String STATO_KO = "KO";

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    // =====================================================================
    // DAO
    // =====================================================================
    // Questo controller usa solo i DAO strettamente legati alla prenotazione
    // e all'utente.
    //
    // Non usa CampoDAO: tutto ciò che riguarda i campi viene delegato al
    // controller disponibilità.

    private PrenotazioneDAO prenotazioneDAO() {
        return DAOFactory.getInstance().getPrenotazioneDAO();
    }

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    // =====================================================================
    // SERVICE CONTROLLER
    // =====================================================================
    // I controller secondari vengono recuperati dalla ServiceFactory.
    // In questo modo il controller principale resta stateless e dipende
    // dalle interfacce dei servizi, non dalle implementazioni concrete.

    private GestioneDisponibilitaPrenotazione dispCtrl() {
        return ServiceFactory.getDisponibilitaPrenotazioneService();
    }

    private GestionePagamentoPrenotazione payCtrl() {
        return ServiceFactory.getPagamentoPrenotazioneService();
    }

    private GestioneFatturaPrenotazione fattCtrl() {
        return ServiceFactory.getFatturaPrenotazioneService();
    }

    private GestioneNotificaPrenotazione notiCtrl() {
        return ServiceFactory.getNotificaPrenotazioneService();
    }

    // STEP 0: lista campi

    /**
     * Restituisce la lista dei campi presenti nel sistema.
     *
     * Il controller prenotazione non accede direttamente a CampoDAO.
     * La lista dei campi viene richiesta al controller disponibilità,
     * perché i campi sono risorse prenotabili e fanno parte del contesto
     * della disponibilità.
     */
    @Override
    public CampiBean listaCampi() {
        if (dispCtrl() == null) {
            return new CampiBean();
        }

        return dispCtrl().listaCampi();
    }

    // STEP 1: trova slot disponibili

    /**
     * Verifica gli slot disponibili in base ai parametri inseriti dall'utente.
     *
     * Il metodo delega la verifica al controller secondario della disponibilità.
     * Se i parametri non sono validi o il servizio non è disponibile,
     * viene restituita una lista vuota.
     */
    @Override
    public List<DatiDisponibilitaBean> trovaSlotDisponibili(ParametriVerificaBean param) {
        if (param == null || dispCtrl() == null) {
            return List.of();
        }

        return dispCtrl().verificaDisponibilita(param);
    }

    // STEP 2: nuova prenotazione

    /**
     * Crea una nuova prenotazione in stato DA_PAGARE.
     *
     * Il metodo:
     * - controlla input e sessione;
     * - converte data e orari;
     * - risolve l'utente reale tramite GeneralUserDAO;
     * - verifica che l'utente abbia ruolo UTENTE;
     * - chiede al controller disponibilità se lo slot è disponibile;
     * - chiede al controller disponibilità i dati del campo;
     * - crea e persiste la prenotazione;
     * - restituisce il riepilogo al layer grafico.
     */
    @Override
    public RiepilogoPrenotazioneBean nuovaPrenotazione(DatiInputPrenotazioneBean input,
                                                       SessioneUtenteBean sessione) {
        if (input == null || sessione == null || sessione.getUtente() == null || dispCtrl() == null) {
            return null;
        }

        final int idCampo = input.getIdCampo();
        final String sData = input.getData();
        final String sIniz = input.getOraInizio();
        final String sFine = input.getOraFine();

        if (idCampo <= 0
                || LogicControllerHelper.isBlank(sData)
                || LogicControllerHelper.isBlank(sIniz)) {
            return null;
        }

        ParsedPrenotazioneInput parsed = parsePrenotazioneInput(idCampo, sData, sIniz, sFine);
        if (parsed == null) {
            return null;
        }

        GeneralUser user = resolveUserFromSession(sessione);
        if (user == null) {
            return null;
        }

        if (user.getRuolo() != Ruolo.UTENTE) {
            return null;
        }

        /*
         * Prima di creare la prenotazione si verifica nuovamente la disponibilità.
         * Il controller prenotazione non interpreta direttamente gli slot:
         * si limita a chiedere al controller disponibilità se lo slot esiste.
         */
        ParametriVerificaBean pv = buildParametriVerifica(
                parsed.idCampo(),
                parsed.data(),
                parsed.inizio(),
                parsed.durataMin()
        );

        if (!isSlotDisponibile(pv)) {
            log().log(Level.WARNING, MSG_SLOT_NON_DISP,
                    new Object[]{parsed.idCampo(), parsed.data(), parsed.inizio(), parsed.fine()});
            return null;
        }

        /*
         * Anche il recupero dei dati campo viene delegato alla disponibilità.
         * In questo modo il controller prenotazione non conosce CampoDAO
         * e non fa mapping Campo -> CampoBean.
         */
        CampoBean campo = dispCtrl().recuperaCampo(parsed.idCampo());
        if (campo == null || campo.getIdCampo() <= 0) {
            return null;
        }

        Prenotazione prenotazione = buildPrenotazioneDaPagare(
                parsed.idCampo(),
                user.getIdUtente(),
                parsed.data(),
                parsed.inizio(),
                parsed.fine()
        );

        prenotazioneDAO().store(prenotazione);

        log().fine(() -> "[PRENOT] Creata prenotazione id=" + prenotazione.getIdPrenotazione()
                + " per utente=" + user.getEmail());

        return buildRiepilogo(prenotazione, campo, sessione.getUtente(), parsed.durataMin());
    }

    // STEP 3: completa prenotazione

    /**
     * Completa una prenotazione tramite pagamento.
     *
     * Il metodo:
     * - controlla input e sessione;
     * - risolve l'utente reale;
     * - verifica che l'utente abbia ruolo UTENTE;
     * - recupera la prima prenotazione DA_PAGARE;
     * - richiede il pagamento al controller secondario pagamento;
     * - se il pagamento va a buon fine, conferma la prenotazione;
     * - genera la fattura;
     * - invia la notifica di conferma;
     * - restituisce lo stato del pagamento.
     */
    @Override
    public StatoPagamentoBean completaPrenotazione(DatiPagamentoBean dati,
                                                   SessioneUtenteBean sessione) {
        if (!isCheckoutInputValid(dati, sessione)) {
            return buildPagamentoKo(MSG_INPUT_NON_VALIDO);
        }

        GeneralUser user = resolveUserFromSession(sessione);
        if (user == null) {
            return buildPagamentoKo(MSG_UTENTE_INESISTENTE);
        }

        if (user.getRuolo() != Ruolo.UTENTE) {
            return buildPagamentoKo(MSG_OPERAZIONE_NON_CONSENTITA);
        }

        Prenotazione prenotazione = getFirstDaPagare(user.getIdUtente());
        if (prenotazione == null) {
            return buildPagamentoKo(MSG_NO_PREN_DA_PAG);
        }

        StatoPagamentoBean esito = payCtrl().richiediPagamentoPrenotazione(
                dati,
                prenotazione.getIdPrenotazione()
        );

        if (esito == null) {
            return buildPagamentoKo(MSG_PAGAMENTO_RIFIUTATO);
        }

        if (esito.isSuccesso()) {
            postPagamentoActions(prenotazione, user.getEmail(), sessione);
        }

        return esito;
    }

    // =====================================================================
    // VALIDAZIONE
    // =====================================================================

    /**
     * Controlla che i dati minimi del checkout siano validi.
     *
     * Questo controllo riguarda solo l'input del caso d'uso.
     * La validazione tecnica del pagamento viene fatta dal controller pagamento.
     */
    private boolean isCheckoutInputValid(DatiPagamentoBean dati,
                                         SessioneUtenteBean sessione) {
        return dati != null
                && sessione != null
                && sessione.getUtente() != null;
    }

    // =====================================================================
    // PARSING INPUT PRENOTAZIONE
    // =====================================================================

    /**
     * Converte i dati testuali della prenotazione in valori di dominio.
     *
     * Questo parsing resta nel controller prenotazione perché riguarda
     * l'input specifico del caso d'uso "prenota campo".
     */
    private ParsedPrenotazioneInput parsePrenotazioneInput(int idCampo,
                                                           String sData,
                                                           String sIniz,
                                                           String sFine) {
        final LocalDate data;
        final LocalTime inizio;
        final LocalTime fine;

        try {
            data = LocalDate.parse(sData.trim());
            inizio = LocalTime.parse(sIniz.trim());

            if (!LogicControllerHelper.isBlank(sFine)) {
                fine = LocalTime.parse(sFine.trim());
            } else {
                fine = inizio.plusMinutes(DEFAULT_DURATA_MIN);
            }
        } catch (DateTimeParseException ex) {
            log().log(Level.WARNING, "[PRENOT] Formato data/ora non valido", ex);
            return null;
        }

        int durataMin = (int) Duration.between(inizio, fine).toMinutes();
        if (durataMin <= 0) {
            return null;
        }

        return new ParsedPrenotazioneInput(idCampo, data, inizio, fine, durataMin);
    }

    /**
     * Record interno usato solo per tenere insieme i dati già validati
     * e convertiti della prenotazione.
     */
    private record ParsedPrenotazioneInput(
            int idCampo,
            LocalDate data,
            LocalTime inizio,
            LocalTime fine,
            int durataMin
    ) {
    }

    // =====================================================================
    // DISPONIBILITÀ E CREAZIONE PRENOTAZIONE
    // =====================================================================

    /**
     * Costruisce i parametri per verificare la disponibilità dello slot richiesto.
     */
    private ParametriVerificaBean buildParametriVerifica(int idCampo,
                                                         LocalDate data,
                                                         LocalTime inizio,
                                                         int durataMin) {
        ParametriVerificaBean pv = new ParametriVerificaBean();
        pv.setIdCampo(idCampo);
        pv.setData(data.toString());
        pv.setOraInizio(inizio.toString());
        pv.setDurataMin(durataMin);

        return pv;
    }

    /**
     * Chiede al controller disponibilità se lo slot richiesto è disponibile.
     */
    private boolean isSlotDisponibile(ParametriVerificaBean param) {
        if (param == null || dispCtrl() == null) {
            return false;
        }

        return !dispCtrl().verificaDisponibilita(param).isEmpty();
    }

    /**
     * Crea una prenotazione in stato DA_PAGARE.
     */
    private Prenotazione buildPrenotazioneDaPagare(int idCampo,
                                                   int idUtente,
                                                   LocalDate data,
                                                   LocalTime inizio,
                                                   LocalTime fine) {
        Prenotazione p = new Prenotazione();
        p.setIdCampo(idCampo);
        p.setIdUtente(idUtente);
        p.setData(data);
        p.setOraInizio(inizio);
        p.setOraFine(fine);
        p.setStato(StatoPrenotazione.DA_PAGARE);

        return p;
    }

    // =====================================================================
    // PRENOTAZIONE E POST-PAGAMENTO
    // =====================================================================

    /**
     * Recupera la prima prenotazione dell'utente ancora da pagare.
     */
    private Prenotazione getFirstDaPagare(int idUtente) {
        List<Prenotazione> daPagare =
                prenotazioneDAO().findByUtenteAndStato(idUtente, StatoPrenotazione.DA_PAGARE);

        return daPagare.isEmpty() ? null : daPagare.get(0);
    }

    /**
     * Esegue le azioni successive al pagamento andato a buon fine.
     */
    private void postPagamentoActions(Prenotazione prenotazione,
                                      String email,
                                      SessioneUtenteBean sessione) {
        confermaPrenotazione(prenotazione);
        generaFatturaPrenotazione(prenotazione, email);
        inviaNotificaPrenotazione(prenotazione, sessione);
    }

    /**
     * Conferma la prenotazione dopo il pagamento.
     */
    private void confermaPrenotazione(Prenotazione prenotazione) {
        prenotazioneDAO().updateStato(prenotazione.getIdPrenotazione(), StatoPrenotazione.CONFERMATA);
    }

    /**
     * Genera la fattura collegata alla prenotazione.
     */
    private void generaFatturaPrenotazione(Prenotazione prenotazione, String email) {
        DatiFatturaBean fatt = new DatiFatturaBean();
        fatt.setCodiceFiscaleCliente(email);

        fattCtrl().generaFatturaPrenotazione(fatt, prenotazione.getIdPrenotazione());
    }

    /**
     * Invia la notifica di conferma prenotazione.
     */
    private void inviaNotificaPrenotazione(Prenotazione prenotazione, SessioneUtenteBean sessione) {
        notiCtrl().inviaConfermaPrenotazione(
                sessione.getUtente(),
                "Prenotazione #" + prenotazione.getIdPrenotazione() + " confermata"
        );
    }

    /**
     * Costruisce un esito negativo per errori preliminari del caso d'uso.
     */
    private StatoPagamentoBean buildPagamentoKo(String messaggio) {
        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(false);
        bean.setStato(STATO_KO);
        bean.setIdTransazione("TX-" + System.currentTimeMillis());
        bean.setDataPagamento(LocalDateTime.now());
        bean.setMessaggio(messaggio);

        return bean;
    }

    // =====================================================================
    // RIEPILOGO
    // =====================================================================

    /**
     * Costruisce il riepilogo della prenotazione da mostrare all'utente.
     *
     * Il costo viene calcolato usando il CampoBean restituito dal controller
     * disponibilità. Il controller prenotazione non accede direttamente
     * all'entity Campo.
     */
    private RiepilogoPrenotazioneBean buildRiepilogo(Prenotazione prenotazione,
                                                     CampoBean campo,
                                                     UtenteBean utente,
                                                     int durataMin) {
        RiepilogoPrenotazioneBean r = new RiepilogoPrenotazioneBean();
        r.setIdPrenotazione(prenotazione.getIdPrenotazione());
        r.setUtente(utente);
        r.setImportoTotale(resolveImportoTotale(campo, durataMin));
        r.setDatiFiscali(null);

        return r;
    }

    /**
     * Calcola l'importo totale della prenotazione.
     */
    private float resolveImportoTotale(CampoBean campo, int durataMin) {
        if (campo == null || campo.getCostoOrario() == null) {
            return 0f;
        }

        return campo.getCostoOrario().floatValue() * (durataMin / 60f);
    }

    // =====================================================================
    // HELPERS SPECIFICI PRENOTAZIONE
    // =====================================================================

    /**
     * Risolve utente dalla sessione usando GeneralUserDAO.
     *
     * La risoluzione viene demandata alla facade runtime, così il controller
     * non conosce i DAO concreti degli utenti.
     */
    private GeneralUser resolveUserFromSession(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) {
            return null;
        }

        String email = LogicControllerHelper.normalizeEmail(sessione.getUtente().getEmail());
        if (email == null) {
            return null;
        }

        return userDAO().findByEmail(email);
    }
}