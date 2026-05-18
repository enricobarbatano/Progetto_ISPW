package com.ispw.controller.logic.ctrl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
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
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoPagamento;
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
 */
public class LogicControllerPrenotazioneCampo implements CtrlPrenotazione {

    private static final String PAGATO = "PAGATO";

    private static final int DEFAULT_DURATA_MIN = 60;

    private static final String MSG_SLOT_NON_DISP = "[PRENOT] Slot non disponibile: campo={0} {1} {2}-{3}";
    private static final String MSG_NO_PREN_DA_PAG = "Nessuna prenotazione da pagare";
    private static final String MSG_INPUT_NON_VALIDO = "Input non valido";

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

    private CampoDAO campoDAO() {
        return DAOFactory.getInstance().getCampoDAO();
    }

    private GeneralUserDAO userDAO() {
        return DAOFactory.getInstance().getGeneralUserDAO();
    }

    private PagamentoDAO pagamentoDAO() {
        return DAOFactory.getInstance().getPagamentoDAO();
    }

    // =====================================================================
    // SERVICE CONTROLLER
    // =====================================================================
    // I controller secondari vengono recuperati dalla ServiceFactory.
    // In questo modo il controller principale resta stateless e non dipende
    // direttamente dalle implementazioni concrete dei servizi applicativi.

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
     * Questo metodo viene usato dal layer grafico per mostrare all'utente
     * quali campi sono disponibili per la prenotazione.
     */
    @Override
    public CampiBean listaCampi() {
        CampiBean out = new CampiBean();

        out.setCampi(campoDAO().findAll().stream()
                .map(this::toBean)
                .toList());

        return out;
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
        //controllo che i parametri di verifica siano presenti
        if (param == null || dispCtrl() == null) {
            return List.of();
        }

        //richiamo il controller secondario della disponibilità
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
     * - verifica la disponibilità dello slot;
     * - recupera il campo;
     * - crea e persiste la prenotazione;
     * - restituisce il riepilogo al layer grafico.
     */
    @Override
    public RiepilogoPrenotazioneBean nuovaPrenotazione(DatiInputPrenotazioneBean input,
                                                       SessioneUtenteBean sessione) {
        //controllo che input, sessione e utente in sessione siano presenti
        if (input == null || sessione == null || sessione.getUtente() == null || dispCtrl() == null) {
            return null;
        }

        //recupero i dati principali dal bean di input
        final int idCampo = input.getIdCampo();
        final String sData = input.getData();
        final String sIniz = input.getOraInizio();
        final String sFine = input.getOraFine();

        //controllo che id campo, data e ora inizio siano validi
        if (idCampo <= 0 || LogicControllerHelper.isBlank(sData) || LogicControllerHelper.isBlank(sIniz)) {
            return null;
        }

        final LocalDate data;
        final LocalTime inizio;
        final LocalTime fine;

        //converto data e orari dal formato stringa al formato di dominio
        try {
            data = LocalDate.parse(sData.trim());      // yyyy-MM-dd
            inizio = LocalTime.parse(sIniz.trim());    // HH:mm

            if (!LogicControllerHelper.isBlank(sFine)) {
                fine = LocalTime.parse(sFine.trim());
            } else {
                fine = inizio.plusMinutes(DEFAULT_DURATA_MIN);
            }
        } catch (DateTimeParseException ex) {
            log().log(Level.WARNING, "[PRENOT] Formato data/ora non valido", ex);
            return null;
        }

        //calcolo la durata della prenotazione
        final int durataMin = (int) Duration.between(inizio, fine).toMinutes();
        if (durataMin <= 0) {
            return null;
        }

        /*
         * Risoluzione utente demandata alla Facade GeneralUserDAO.
         * In questo modo il controller non deve conoscere il DAO concreto
         * dell'utente finale o del gestore.
         */
        final GeneralUser user = resolveUserFromSession(sessione);
        if (user == null) {
            return null;
        }

        //la prenotazione è consentita solo agli utenti finali
        if (user.getRuolo() != Ruolo.UTENTE) {
            return null;
        }

        /*
         * Prima di creare la prenotazione si verifica nuovamente la disponibilità.
         * Questa verifica serve a evitare di salvare una prenotazione su uno slot
         * che nel frattempo non risulta più disponibile.
         */
        ParametriVerificaBean pv = new ParametriVerificaBean();
        pv.setIdCampo(idCampo);
        pv.setData(data.toString());
        pv.setOraInizio(inizio.toString());
        pv.setDurataMin(durataMin);

        List<DatiDisponibilitaBean> slots = dispCtrl().verificaDisponibilita(pv);

        boolean disponibile = slots.stream().anyMatch(d ->
                Objects.equals(d.getData(), data.toString())
                        && Objects.equals(d.getOraInizio(), inizio.toString())
                        && Objects.equals(d.getOraFine(), fine.toString())
        );

        //se lo slot non è disponibile, il flusso termina senza creare la prenotazione
        if (!disponibile) {
            log().log(Level.WARNING, MSG_SLOT_NON_DISP, new Object[]{idCampo, data, inizio, fine});
            return null;
        }

        /*
         * Recupera il Campo puro.
         *
         * Non blocchiamo più lo slot dentro Campo con c.bloccoSlot(...),
         * perché listaPrenotazioni è runtime-only e @JsonIgnore.
         *
         * La persistenza effettiva dello slot è rappresentata dalla Prenotazione
         * salvata tramite PrenotazioneDAO.store(...).
         */
        Campo c = campoDAO().findById(idCampo);
        if (c == null) {
            return null;
        }

        //creo una nuova prenotazione in stato DA_PAGARE
        Prenotazione p = new Prenotazione();
        p.setIdCampo(idCampo);
        p.setIdUtente(user.getIdUtente());
        p.setData(data);
        p.setOraInizio(inizio);
        p.setOraFine(fine);
        p.setStato(StatoPrenotazione.DA_PAGARE);

        //persisto la prenotazione tramite DAO
        prenotazioneDAO().store(p);

        log().fine(() -> "[PRENOT] Creata prenotazione id=" + p.getIdPrenotazione()
                + " per utente=" + user.getEmail());

        //costruisco e restituisco il riepilogo al graphic controller
        return buildRiepilogo(p, c, sessione.getUtente(), durataMin);
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
     * - richiede il pagamento;
     * - se il pagamento va a buon fine, conferma la prenotazione;
     * - genera la fattura;
     * - invia la notifica di conferma;
     * - restituisce lo stato del pagamento.
     */
    @Override
    public StatoPagamentoBean completaPrenotazione(DatiPagamentoBean dati,
                                                   SessioneUtenteBean sessione) {
        //controllo che i dati minimi del checkout siano validi
        if (!isCheckoutInputValid(dati, sessione)) {
            return esitoPagamento(false, "KO", MSG_INPUT_NON_VALIDO);
        }

        /*
         * Risoluzione utente demandata alla Facade GeneralUserDAO.
         * Il controller lavora sulla facade e non sui DAO concreti.
         */
        final GeneralUser user = resolveUserFromSession(sessione);
        if (user == null) {
            return esitoPagamento(false, "KO", "Utente inesistente");
        }

        //il checkout è consentito solo agli utenti finali
        if (user.getRuolo() != Ruolo.UTENTE) {
            return esitoPagamento(false, "KO", "Operazione non consentita");
        }

        //recupero la prima prenotazione in stato DA_PAGARE dell'utente
        final Prenotazione p = getFirstDaPagare(user.getIdUtente());
        if (p == null) {
            return esitoPagamento(false, "KO", MSG_NO_PREN_DA_PAG);
        }

        //richiedo il pagamento tramite controller secondario pagamento
        final StatoPagamento statoEnum = payCtrl().richiediPagamentoPrenotazione(dati, p.getIdPrenotazione());

        //converto lo stato del pagamento in booleano di successo
        final boolean success = isPagamentoOk(statoEnum);

        //se il pagamento va a buon fine, confermo prenotazione, genero fattura e invio notifica
        if (success) {
            postPagamentoActions(p, user.getEmail(), sessione);
        }

        //compongo l'esito finale, preferendo quanto persistito nel DAO pagamento
        return composeEsito(p, success, statoEnum);
    }

    // =====================================================================
    // VALIDAZIONE
    // =====================================================================

    /**
     * Controlla che i dati minimi del checkout siano validi.
     */
    private boolean isCheckoutInputValid(DatiPagamentoBean dati,
                                         SessioneUtenteBean sessione) {
        return dati != null
                && sessione != null
                && sessione.getUtente() != null
                && payCtrl() != null
                && fattCtrl() != null
                && notiCtrl() != null;
    }


    // =====================================================================
    // PRENOTAZIONE E PAGAMENTO
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
     *
     * Il metodo:
     * - aggiorna lo stato della prenotazione a CONFERMATA;
     * - crea i dati minimi per la fattura;
     * - genera la fattura;
     * - invia la notifica di conferma prenotazione.
     */
    private void postPagamentoActions(Prenotazione p,
                                      String email,
                                      SessioneUtenteBean sessione) {
        //confermo la prenotazione
        prenotazioneDAO().updateStato(p.getIdPrenotazione(), StatoPrenotazione.CONFERMATA);

        //creo i dati fiscali minimi per la fattura
        DatiFatturaBean fatt = new DatiFatturaBean();
        fatt.setCodiceFiscaleCliente(email);

        //genero la fattura della prenotazione
        fattCtrl().generaFatturaPrenotazione(fatt, p.getIdPrenotazione());

        //invio la notifica di conferma prenotazione
        notiCtrl().inviaConfermaPrenotazione(sessione.getUtente(),
                "Prenotazione #" + p.getIdPrenotazione() + " confermata");
    }

    /**
     * Compone l'esito del pagamento.
     *
     * Se il pagamento è stato persistito, usa i dati del DAO pagamento.
     * Altrimenti usa lo stato restituito dal controller pagamento.
     */
    private StatoPagamentoBean composeEsito(Prenotazione p, boolean success, StatoPagamento statoEnum) {
        Pagamento pag = pagamentoDAO().findByPrenotazione(p.getIdPrenotazione());

        if (pag != null) {
            StatoPagamentoBean bean = new StatoPagamentoBean();
            bean.setSuccesso(success);

            if (pag.getStato() != null) {
                bean.setStato(pag.getStato().name());
            } else {
                bean.setStato(resolveStatoPagamentoFallback(success));
            }

            bean.setIdTransazione(newTxId("PX"));
            bean.setDataPagamento(pag.getDataPagamento());
            bean.setMessaggio(resolveMessaggioPagamento(success));
            return bean;
        }

        final String stato = resolveStatoPagamento(statoEnum, success);
        return esitoPagamento(success, stato, resolveMessaggioPagamento(success));
    }

    /**
     * Verifica se lo stato del pagamento indica un esito positivo.
     */
    private boolean isPagamentoOk(StatoPagamento stato) {
        if (stato == null) {
            return false;
        }

        if (stato == StatoPagamento.OK) {
            return true;
        }

        final String s = stato.name();
        return s.contains("ESEGUITO")
                || s.contains("APPROVATO")
                || s.contains(PAGATO)
                || s.contains("SUCCESSO")
                || s.contains("COMPLETATO");
    }

    /**
     * Costruisce un esito pagamento standard.
     */
    private StatoPagamentoBean esitoPagamento(boolean ok, String stato, String msg) {
        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(ok);
        bean.setStato(resolveStatoPagamentoOutput(stato, ok));
        bean.setIdTransazione(newTxId("TX"));
        bean.setDataPagamento(java.time.LocalDateTime.now());
        bean.setMessaggio(msg);
        return bean;
    }

    /**
     * Risolve lo stato pagamento quando non è presente nel DAO.
     *
     * Questo metodo evita ternari annidati e mantiene leggibile la logica.
     */
    private String resolveStatoPagamento(StatoPagamento statoEnum, boolean success) {
        if (statoEnum != null) {
            return statoEnum.name();
        }

        return resolveStatoPagamentoFallback(success);
    }

    /**
     * Risolve lo stato testuale in base al successo del pagamento.
     */
    private String resolveStatoPagamentoFallback(boolean success) {
        if (success) {
            return PAGATO;
        }

        return "KO";
    }

    /**
     * Risolve lo stato da inserire nell'output.
     *
     * Se lo stato è già presente, viene usato quello.
     * Altrimenti viene calcolato in base all'esito.
     */
    private String resolveStatoPagamentoOutput(String stato, boolean ok) {
        if (stato != null) {
            return stato;
        }

        return resolveStatoPagamentoFallback(ok);
    }

    /**
     * Risolve il messaggio di pagamento in base all'esito.
     */
    private String resolveMessaggioPagamento(boolean success) {
        if (success) {
            return "Pagamento eseguito";
        }

        return "Pagamento rifiutato";
    }

    /**
     * Genera un id transazione semplice usando un prefisso e il timestamp corrente.
     */
    private String newTxId(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }

    // =====================================================================
    // MAPPING E RIEPILOGO
    // =====================================================================

    /**
     * Converte un Campo di dominio nel bean usato dal layer grafico.
     */
    private CampoBean toBean(Campo campo) {
        CampoBean bean = new CampoBean();

        if (campo == null) {
            return bean;
        }

        bean.setIdCampo(campo.getIdCampo());
        bean.setNome(campo.getNome());
        bean.setTipoSport(campo.getTipoSport());

        if (campo.getCostoOrario() != null) {
            bean.setCostoOrario(java.math.BigDecimal.valueOf(campo.getCostoOrario()));
        }

        bean.setAttivo(campo.isAttivo());
        bean.setFlagManutenzione(campo.isFlagManutenzione());

        return bean;
    }

    /**
     * Costruisce il riepilogo della prenotazione da mostrare all'utente.
     */
    private RiepilogoPrenotazioneBean buildRiepilogo(Prenotazione p,
                                                     Campo c,
                                                     UtenteBean utente,
                                                     int durataMin) {
        RiepilogoPrenotazioneBean r = new RiepilogoPrenotazioneBean();
        r.setIdPrenotazione(p.getIdPrenotazione());
        r.setUtente(utente);

        float importo = 0f;
        if (c != null && c.getCostoOrario() != null) {
            importo = c.getCostoOrario() * (durataMin / 60f);
        }

        r.setImportoTotale(importo);
        r.setDatiFiscali(null);

        return r;
    }

    // =====================================================================
    // HELPERS SPECIFICI PRENOTAZIONE
    // =====================================================================

    /**
     * Normalizza l'email.
     *
     * Se l'email è nulla o vuota, ritorna null.
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        final String t = email.trim();
        if (t.isEmpty()) {
            return null;
        }

        return t.toLowerCase();
    }

    /**
     * Helper unico: risolve utente dalla sessione usando GeneralUserDAO.
     *
     * La risoluzione viene demandata alla facade runtime, così il controller
     * non conosce i DAO concreti degli utenti.
     */
    private GeneralUser resolveUserFromSession(SessioneUtenteBean sessione) {
        if (sessione == null || sessione.getUtente() == null) {
            return null;
        }

        final String email = normalizeEmail(sessione.getUtente().getEmail());
        if (email == null) {
            return null;
        }

        return userDAO().findByEmail(email);
    }
}