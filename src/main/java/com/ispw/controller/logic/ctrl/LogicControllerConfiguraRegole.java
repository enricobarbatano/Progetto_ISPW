package com.ispw.controller.logic.ctrl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.CampoBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.logic.ServiceFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneRegole;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaGestioneRegole;
import com.ispw.controller.logic.interfaces.manutenzione.GestioneManutenzioneConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaConfiguraRegole;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.SystemLog;

/**
 * Controller applicativo del caso d'uso "Configura regole".
 *
 * Il caso d'uso permette al gestore di:
 * - consultare i campi disponibili;
 * - aggiornare lo stato operativo di un campo;
 * - impostare un campo in manutenzione;
 * - aggiornare le regole temporali di prenotazione;
 * - aggiornare le regole di penalità.
 *
 * Nota di progetto:
 * questa classe fa da "facciata applicativa" del caso d'uso.
 * Il layer grafico chiama solo questi metodi pubblici e non conosce i DAO
 * né i controller secondari usati internamente.
 */
public class LogicControllerConfiguraRegole implements CtrlGestioneRegole {

    // Messaggi comuni
    private static final String MSG_INPUT_CAMPO_KO = "Dati regola campo non validi";
    private static final String MSG_CAMPO_NOT_FOUND = "Campo inesistente";
    private static final String MSG_UPDATE_CAMPO_OK = "Regola campo aggiornata";

    private static final String MSG_INPUT_MANUT_KO = "Dati manutenzione non validi";
    private static final String MSG_MANUT_OK = "Manutenzione impostata per il campo";

    private static final String MSG_INPUT_TEMP_KO = "Tempistiche non valide";
    private static final String MSG_UPDATE_TEMP_OK = "Regole tempistiche aggiornate";

    private static final String MSG_INPUT_PEN_KO = "Regole penalita non valide";
    private static final String MSG_UPDATE_PEN_OK = "Regole penalita aggiornate";

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

    private CampoDAO campoDAO() {
        return DAOFactory.getInstance().getCampoDAO();
    }

    private RegoleTempisticheDAO tempDAO() {
        return DAOFactory.getInstance().getRegoleTempisticheDAO();
    }

    private RegolePenalitaDAO penDAO() {
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

    private GestioneDisponibilitaGestioneRegole dispCtrl() {
        return ServiceFactory.getDisponibilitaRegoleService();
    }

    private GestioneManutenzioneConfiguraRegole manCtrl() {
        return ServiceFactory.getManutenzioneService();
    }

    private GestioneNotificaConfiguraRegole notiCtrl() {
        return ServiceFactory.getNotificaConfiguraRegoleService();
    }

    // STEP 0: lista campi

    /**
     * Restituisce la lista dei campi presenti nel sistema.
     *
     * Questo metodo viene usato dal layer grafico per mostrare al gestore
     * quali campi possono essere configurati.
     */
    @Override
    public CampiBean listaCampi() {
        CampiBean out = new CampiBean();

        out.setCampi(campoDAO().findAll().stream()
                .map(this::toBean)
                .toList());

        return out;
    }

    /**
     * Converte un Campo di dominio nel bean usato dalla UI.
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

    // STEP 1: aggiorna stato operativo campo

    /**
     * Aggiorna le regole operative di un campo.
     *
     * Il metodo:
     * - controlla i dati inseriti;
     * - recupera il campo dal DAO;
     * - aggiorna lo stato operativo del campo;
     * - salva il campo aggiornato;
     * - aggiorna la disponibilità del campo;
     * - invia eventuale alert manutenzione;
     * - invia una notifica automatica di aggiornamento regole.
     */
    @Override
    public EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean) {

        //controllo che i dati minimi della regola campo siano validi
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_CAMPO_KO);
        }

        //recupero il campo dal database tramite DAO
        final int idCampo = bean.getIdCampo();
        final Campo c = campoDAO().findById(idCampo);

        //se il campo non esiste ritorno errore
        if (c == null) {
            return esito(false, MSG_CAMPO_NOT_FOUND);
        }

        //leggo i parametri di stato arrivati dal bean
        final boolean attivo = Boolean.TRUE.equals(bean.getAttivo());
        final boolean manut = Boolean.TRUE.equals(bean.getFlagManutenzione());

        //aggiorno lo stato operativo del campo usando l'API del dominio
        updateCampoOperativo(c, attivo, manut);

        //salvo il campo aggiornato
        campoDAO().store(c);

        //registro l'aggiornamento nel log applicativo
        appendLogSafe(String.format(
                "[REGOLE] Aggiornato campo id=%d attivo=%s manutenzione=%s",
                idCampo, attivo, manut));

        //aggiorno la disponibilità in base allo stato del campo
        try {
            if (attivo && !manut) {
                dispCtrl().attivaDisponibilita(idCampo);
            } else {
                dispCtrl().rimuoviDisponibilita(idCampo);
            }
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Aggiorna disponibilità fallito: {0}", ex.getMessage());
        }

        //se il campo è in manutenzione, invio un alert al manutentore
        if (manut) {
            try {
                manCtrl().inviaAlertManutentore(idCampo);
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Alert manutenzione fallito: {0}", ex.getMessage());
            }
        }

        //invio notifica automatica di aggiornamento regole
        generaNotificaAutomatica();

        //ritorno l'esito al graphic controller
        return esito(true, MSG_UPDATE_CAMPO_OK);
    }

    // STEP 2: esegui manutenzione

    /**
     * Esegue la manutenzione su un campo.
     *
     * Il metodo:
     * - controlla che il campo sia valido;
     * - recupera il campo dal DAO;
     * - imposta il campo come non attivo e in manutenzione;
     * - salva il campo aggiornato;
     * - rimuove la disponibilità;
     * - invia un alert al manutentore;
     * - invia una notifica automatica di aggiornamento regole.
     */
    @Override
    public EsitoOperazioneBean eseguiManutenzione(RegolaCampoBean bean) {

        //controllo che il bean e l'id del campo siano validi
        if (bean == null || bean.getIdCampo() <= 0) {
            return esito(false, MSG_INPUT_MANUT_KO);
        }

        //recupero il campo dal database tramite DAO
        final int idCampo = bean.getIdCampo();
        final Campo c = campoDAO().findById(idCampo);

        //se il campo non esiste ritorno errore
        if (c == null) {
            return esito(false, MSG_CAMPO_NOT_FOUND);
        }

        //imposta manutenzione = true e campo non attivo
        updateCampoOperativo(c, false, true);

        //salvo il campo aggiornato
        campoDAO().store(c);

        //registro l'operazione nel log applicativo
        appendLogSafe(String.format(
                "[REGOLE] Impostata manutenzione su campo id=%d",
                idCampo));

        //rimuovo la disponibilità del campo
        try {
            dispCtrl().rimuoviDisponibilita(idCampo);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Rimozione disponibilità fallita: {0}", ex.getMessage());
        }

        //invio alert manutenzione
        try {
            manCtrl().inviaAlertManutentore(idCampo);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Alert manutenzione fallito: {0}", ex.getMessage());
        }

        //invio notifica automatica di aggiornamento regole
        generaNotificaAutomatica();

        //ritorno l'esito al graphic controller
        return esito(true, MSG_MANUT_OK);
    }

    // STEP 3: aggiorna regole tempistiche

    /**
     * Aggiorna le regole temporali usate per le prenotazioni.
     *
     * Il metodo:
     * - controlla i dati inseriti;
     * - crea l'entity delle regole temporali;
     * - salva le nuove regole tramite DAO;
     * - registra l'operazione nel log.
     */
    @Override
    public EsitoOperazioneBean aggiornaRegolaTempistiche(TempisticheBean bean) {
        //controllo che le tempistiche siano valide
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_TEMP_KO);
        }

        //creo una nuova entity con le regole temporali ricevute dal bean
        RegoleTempistiche rt = new RegoleTempistiche();
        rt.setDurataSlot(bean.getDurataSlotMinuti());
        rt.setOraApertura(bean.getOraApertura());
        rt.setOraChiusura(bean.getOraChiusura());
        rt.setPreavvisoMinimo(bean.getPreavvisoMinimoMinuti());

        //salvo le regole temporali tramite DAO
        try {
            tempDAO().save(rt);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Salvataggio regole tempistiche fallito: {0}", ex.getMessage());
            return esito(false, MSG_INPUT_TEMP_KO);
        }

        //registro l'operazione nel log applicativo
        appendLogSafe(String.format("[REGOLE] Tempistiche aggiornate: durata=%d' %s-%s, preavviso=%d'",
                rt.getDurataSlot(), rt.getOraApertura(), rt.getOraChiusura(), rt.getPreavvisoMinimo()));

        //ritorno l'esito al graphic controller
        return esito(true, MSG_UPDATE_TEMP_OK);
    }

    // STEP 4: aggiorna regole penalità

    /**
     * Aggiorna le regole di penalità.
     *
     * Il metodo:
     * - controlla i dati inseriti;
     * - crea l'entity delle regole penalità;
     * - salva le nuove regole tramite DAO;
     * - registra l'operazione nel log.
     */
    @Override
    public EsitoOperazioneBean aggiornaRegolepenalita(PenalitaBean bean) {
        //controllo che le regole penalità siano valide
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_PEN_KO);
        }

        //creo una nuova entity con le regole penalità ricevute dal bean
        RegolePenalita rp = new RegolePenalita();
        rp.setValorePenalita(bean.getValorePenalita());
        rp.setPreavvisoMinimo(bean.getPreavvisoMinimoMinuti());

        //salvo le regole penalità tramite DAO
        try {
            penDAO().save(rp);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Salvataggio regole penalita fallito: {0}", ex.getMessage());
            return esito(false, MSG_INPUT_PEN_KO);
        }

        //registro l'operazione nel log applicativo
        appendLogSafe(String.format("[REGOLE] Penalita aggiornata: valore=%s, preavviso=%d'",
                rp.getValorePenalita(), rp.getPreavvisoMinimo()));

        //ritorno l'esito al graphic controller
        return esito(true, MSG_UPDATE_PEN_OK);
    }

    // =====================================================================
    // VALIDAZIONE
    // =====================================================================

    /**
     * Controlla che i dati della regola campo siano validi.
     */
    private boolean isValid(RegolaCampoBean b) {
        return b != null
                && b.getIdCampo() > 0
                && b.getAttivo() != null
                && b.getFlagManutenzione() != null;
    }

    /**
     * Controlla che le regole temporali siano valide.
     */
    private boolean isValid(TempisticheBean b) {
        return b != null
                && b.getDurataSlotMinuti() > 0
                && b.getOraApertura() != null
                && b.getOraChiusura() != null
                && b.getOraApertura().isBefore(b.getOraChiusura())
                && b.getPreavvisoMinimoMinuti() >= 0;
    }

    /**
     * Controlla che le regole di penalità siano valide.
     */
    private boolean isValid(PenalitaBean b) {
        return b != null
                && b.getValorePenalita() != null
                && b.getValorePenalita().signum() > 0
                && b.getPreavvisoMinimoMinuti() >= 0;
    }

    /**
     * Costruisce un esito standard per il layer grafico.
     */
    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }

    // =====================================================================
    // LOG E NOTIFICHE
    // =====================================================================

    /**
     * Scrive un log senza interrompere il flusso in caso di errore.
     */
    private void appendLogSafe(String descr) {
        try {
            SystemLog l = new SystemLog();
            l.setTimestamp(LocalDateTime.now());
            l.setIdUtenteCoinvolto(0); // sistema
            l.setDescrizione(Objects.toString(descr, ""));
            logDAO().append(l);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log REGOLE fallito: {0}", ex.getMessage());
        }
    }

    /**
     * Invia una notifica automatica di aggiornamento regole.
     *
     * Se il controller di notifica non è disponibile, il flusso principale continua.
     */
    private void generaNotificaAutomatica() {
        if (notiCtrl() == null) {
            return;
        }

        try {
            notiCtrl().inviaNotificaAggiornamentoRegole();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica aggiornamento regole fallita: {0}", ex.getMessage());
        }
    }

    // =====================================================================
    // HELPERS SPECIFICI CONFIGURAZIONE REGOLE
    // =====================================================================

    /**
     * Aggiorna lo stato operativo del campo.
     *
     * Usa il metodo di dominio già presente su Campo.
     * Se il dominio solleva un errore, il flusso non viene interrotto.
     */
    private void updateCampoOperativo(Campo c, boolean attivo, boolean manut) {
        if (c == null) {
            return;
        }

        try {
            c.updateStatoOperativo(attivo, manut);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "updateStatoOperativo fallito: {0}", ex.getMessage());
        }
    }
}
