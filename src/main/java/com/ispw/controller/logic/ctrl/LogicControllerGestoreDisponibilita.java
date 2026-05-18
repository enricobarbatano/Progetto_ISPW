package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.CampoBean;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaDisdetta;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaGestioneRegole;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaPrenotazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Prenotazione;

/**
 * Controller applicativo secondario per la gestione della disponibilità.
 *
 * Questo controller viene usato dai controller principali per:
 * - consultare i campi prenotabili;
 * - recuperare i dati di un campo;
 * - verificare se uno slot è disponibile per la prenotazione;
 * - liberare uno slot in fase di disdetta;
 * - attivare o rimuovere la disponibilità di un campo quando il gestore
 *   modifica le regole operative.
 *
 * Nota di progetto:
 * questa classe contiene la logica applicativa legata ai campi e alla
 * disponibilità della risorsa campo.
 *
 * I controller principali non devono accedere direttamente a CampoDAO:
 * devono passare da questo controller secondario.
 */
public class LogicControllerGestoreDisponibilita
        implements GestioneDisponibilitaDisdetta,
                   GestioneDisponibilitaGestioneRegole,
                   GestioneDisponibilitaPrenotazione {

    private static final int DEFAULT_DURATA_MIN = 60;

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

    private PrenotazioneDAO prenotazioneDAO() {
        return DAOFactory.getInstance().getPrenotazioneDAO();
    }

    // STEP 0: consultazione campi

    /**
     * Restituisce la lista dei campi presenti nel sistema.
     *
     * Questo metodo centralizza nel controller disponibilità la consultazione
     * dei campi, evitando che il controller prenotazione acceda a CampoDAO.
     */
    @Override
    public CampiBean listaCampi() {
        CampiBean out = new CampiBean();

        try {
            out.setCampi(campoDAO().findAll().stream()
                    .map(this::toBean)
                    .toList());
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento lista campi fallito: {0}", ex.getMessage());
            out.setCampi(List.of());
        }

        return out;
    }

    /**
     * Recupera un singolo campo e lo converte in bean.
     *
     * Il controller prenotazione usa questo metodo per ottenere i dati
     * necessari al riepilogo senza conoscere CampoDAO né l'entity Campo.
     */
    @Override
    public CampoBean recuperaCampo(int idCampo) {
        if (idCampo <= 0) {
            return new CampoBean();
        }

        Campo campo = loadCampoSafe(idCampo);
        return toBean(campo);
    }

    // STEP 1: libera slot

    /**
     * Libera lo slot occupato da una prenotazione.
     *
     * Il metodo:
     * - recupera la prenotazione;
     * - recupera il campo;
     * - prova a sbloccare lo slot sul campo;
     * - salva il campo aggiornato.
     *
     * È best-effort:
     * se qualcosa fallisce, il flusso chiamante non viene interrotto.
     *
     * Nota:
     * nel progetto la disponibilità reale dello slot è rappresentata soprattutto
     * dalle prenotazioni persistite. Questo metodo resta utile come operazione
     * di sincronizzazione con lo stato runtime del Campo.
     */
    @Override
    public void liberaSlot(int idPrenotazione) {
        if (idPrenotazione <= 0) {
            return;
        }

        Prenotazione prenotazione = loadPrenotazioneSafe(idPrenotazione);
        if (prenotazione == null) {
            return;
        }

        if (prenotazione.getData() == null || prenotazione.getOraInizio() == null) {
            return;
        }

        Campo campo = loadCampoDaPrenotazioneSafe(prenotazione);
        if (campo == null) {
            return;
        }

        try {
            campo.sbloccaSlot(
                    Date.valueOf(prenotazione.getData()),
                    Time.valueOf(prenotazione.getOraInizio())
            );
            campoDAO().store(campo);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Sblocco slot fallito: {0}", ex.getMessage());
        }
    }

    // STEP 2: rimuove disponibilità campo

    /**
     * Rimuove la disponibilità del campo.
     *
     * In pratica il campo viene reso non prenotabile.
     *
     * @return true se l'operazione riesce, false altrimenti.
     */
    @Override
    public Boolean rimuoviDisponibilita(int idCampo) {
        if (idCampo <= 0) {
            return false;
        }

        Campo campo = loadCampoSafe(idCampo);
        if (campo == null) {
            return false;
        }

        try {
            campo.updateStatoOperativo(false, campo.isFlagManutenzione());
            campoDAO().store(campo);
            return true;
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Rimozione disponibilità fallita: {0}", ex.getMessage());
            return false;
        }
    }

    // STEP 3: attiva disponibilità campo

    /**
     * Attiva la disponibilità del campo.
     *
     * Il metodo rende il campo prenotabile.
     * Non avendo parametri temporali, restituisce lista vuota.
     */
    @Override
    public List<DatiDisponibilitaBean> attivaDisponibilita(int idCampo) {
        if (idCampo <= 0) {
            return List.of();
        }

        Campo campo = loadCampoSafe(idCampo);
        if (campo == null) {
            return List.of();
        }

        try {
            campo.updateStatoOperativo(true, campo.isFlagManutenzione());
            campoDAO().store(campo);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Attivazione disponibilità fallita: {0}", ex.getMessage());
        }

        return List.of();
    }

    // STEP 4: verifica disponibilità

    /**
     * Verifica la disponibilità di uno slot.
     *
     * Il metodo:
     * - controlla i parametri;
     * - converte data e orari;
     * - calcola l'ora di fine;
     * - carica il campo richiesto o tutti i campi;
     * - arricchisce i campi con le prenotazioni attive;
     * - verifica se lo slot è disponibile;
     * - restituisce i bean degli slot disponibili.
     *
     * Se idCampo è valorizzato, viene controllato solo quel campo.
     * Altrimenti vengono controllati tutti i campi.
     */
    @Override
    public List<DatiDisponibilitaBean> verificaDisponibilita(ParametriVerificaBean param) {
        ParsedDisponibilitaInput parsed = parseDisponibilitaInput(param);
        if (parsed == null) {
            return List.of();
        }

        List<Campo> campi = loadCampiDaVerificare(parsed.idCampo());
        if (campi.isEmpty()) {
            return List.of();
        }

        return buildDisponibilita(campi, parsed);
    }

    // =====================================================================
    // CARICAMENTO DATI
    // =====================================================================

    /**
     * Carica un Campo tramite CampoDAO e lo arricchisce con le prenotazioni attive
     * tramite PrenotazioneDAO.
     *
     * Questa composizione viene fatta nel layer applicativo per evitare che CampoDAO
     * debba accedere direttamente alla persistenza delle Prenotazioni.
     */
    private Campo caricaCampoConPrenotazioniAttive(int idCampo) {
        Campo campo = campoDAO().findById(idCampo);
        if (campo == null) {
            return null;
        }

        List<Prenotazione> prenotazioniAttive = prenotazioneDAO().findAttiveByCampo(idCampo);
        campo.setPrenotazioniRuntime(prenotazioniAttive);

        return campo;
    }

    /**
     * Arricchisce un Campo già caricato con le sue prenotazioni attive.
     */
    private void caricaPrenotazioniAttiveSuCampo(Campo campo) {
        if (campo == null || campo.getIdCampo() <= 0) {
            return;
        }

        List<Prenotazione> prenotazioniAttive = prenotazioneDAO().findAttiveByCampo(campo.getIdCampo());
        campo.setPrenotazioniRuntime(prenotazioniAttive);
    }

    /**
     * Carica i campi da verificare.
     *
     * Se idCampo è valido, carica solo quel campo.
     * Altrimenti carica tutti i campi.
     */
    private List<Campo> loadCampiDaVerificare(int idCampo) {
        try {
            if (idCampo > 0) {
                Campo unico = caricaCampoConPrenotazioniAttive(idCampo);
                if (unico == null) {
                    return List.of();
                }

                return List.of(unico);
            }

            List<Campo> campi = campoDAO().findAll();
            for (Campo campo : campi) {
                caricaPrenotazioniAttiveSuCampo(campo);
            }

            return campi;
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento campi disponibilità fallito: {0}", ex.getMessage());
            return List.of();
        }
    }

    /**
     * Carica una prenotazione senza propagare errori.
     */
    private Prenotazione loadPrenotazioneSafe(int idPrenotazione) {
        try {
            return prenotazioneDAO().load(idPrenotazione);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento prenotazione fallito: {0}", ex.getMessage());
            return null;
        }
    }

    /**
     * Carica un campo senza propagare errori.
     */
    private Campo loadCampoSafe(int idCampo) {
        try {
            return campoDAO().findById(idCampo);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento campo fallito: {0}", ex.getMessage());
            return null;
        }
    }

    /**
     * Carica il campo associato a una prenotazione.
     */
    private Campo loadCampoDaPrenotazioneSafe(Prenotazione prenotazione) {
        try {
            if (prenotazione.getCampo() != null) {
                return prenotazione.getCampo();
            }

            return campoDAO().findById(prenotazione.getIdCampo());
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Caricamento campo da prenotazione fallito: {0}", ex.getMessage());
            return null;
        }
    }

    // =====================================================================
    // PARSING E COSTRUZIONE DISPONIBILITÀ
    // =====================================================================

    /**
     * Converte i parametri di verifica disponibilità in valori di dominio.
     */
    private ParsedDisponibilitaInput parseDisponibilitaInput(ParametriVerificaBean param) {
        if (param == null) {
            return null;
        }

        String sData = param.getData();
        String sOra = param.getOraInizio();

        if (LogicControllerHelper.isBlank(sData) || LogicControllerHelper.isBlank(sOra)) {
            return null;
        }

        final LocalDate data;
        final LocalTime oraInizio;

        try {
            data = LocalDate.parse(sData.trim());
            oraInizio = LocalTime.parse(sOra.trim());
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Parsing parametri disponibilità fallito: {0}", ex.getMessage());
            return null;
        }

        int durataMin = resolveDurata(param.getDurataMin());
        LocalTime oraFine = oraInizio.plusMinutes(durataMin);

        return new ParsedDisponibilitaInput(
                param.getIdCampo(),
                data,
                oraInizio,
                oraFine,
                durataMin
        );
    }

    /**
     * Risolve la durata dello slot.
     */
    private int resolveDurata(int durataMin) {
        if (durataMin > 0) {
            return durataMin;
        }

        return DEFAULT_DURATA_MIN;
    }

    /**
     * Costruisce la lista degli slot disponibili.
     */
    private List<DatiDisponibilitaBean> buildDisponibilita(List<Campo> campi,
                                                           ParsedDisponibilitaInput parsed) {
        List<DatiDisponibilitaBean> out = new ArrayList<>();

        Date sqlDate = Date.valueOf(parsed.data());
        Time sqlInizio = Time.valueOf(parsed.oraInizio());
        Time sqlFine = Time.valueOf(parsed.oraFine());

        for (Campo campo : campi) {
            if (!isCampoDisponibile(campo, sqlDate, sqlInizio, sqlFine)) {
                continue;
            }

            out.add(buildDisponibilitaBean(campo, parsed));
        }

        return out;
    }

    /**
     * Verifica se il campo è disponibile nello slot indicato.
     */
    private boolean isCampoDisponibile(Campo campo,
                                       Date data,
                                       Time oraInizio,
                                       Time oraFine) {
        if (campo == null) {
            return false;
        }

        try {
            return campo.isDisponibile(data, oraInizio, oraFine)
                    && campo.isAttivo()
                    && !campo.isFlagManutenzione();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Verifica disponibilità campo fallita: {0}", ex.getMessage());
            return false;
        }
    }

    /**
     * Costruisce il bean di disponibilità da restituire al layer chiamante.
     */
    private DatiDisponibilitaBean buildDisponibilitaBean(Campo campo,
                                                         ParsedDisponibilitaInput parsed) {
        DatiDisponibilitaBean bean = new DatiDisponibilitaBean();
        bean.setData(parsed.data().toString());
        bean.setOraInizio(parsed.oraInizio().toString());
        bean.setOraFine(parsed.oraFine().toString());
        bean.setCosto(resolveCosto(campo, parsed.durataMin()));

        return bean;
    }

    /**
     * Calcola il costo dello slot in base al costo orario del campo.
     */
    private float resolveCosto(Campo campo, int durataMin) {
        if (campo == null || campo.getCostoOrario() == null) {
            return 0f;
        }

        return campo.getCostoOrario() * (durataMin / 60f);
    }

    // =====================================================================
    // MAPPING ENTITY -> BEAN
    // =====================================================================

    /**
     * Converte un Campo di dominio nel bean usato dai layer superiori.
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
            bean.setCostoOrario(BigDecimal.valueOf(campo.getCostoOrario()));
        }

        bean.setAttivo(campo.isAttivo());
        bean.setFlagManutenzione(campo.isFlagManutenzione());

        return bean;
    }

    /**
     * Record interno usato per mantenere insieme i dati già validati
     * e convertiti della verifica disponibilità.
     */
    private record ParsedDisponibilitaInput(
            int idCampo,
            LocalDate data,
            LocalTime oraInizio,
            LocalTime oraFine,
            int durataMin
    ) {
    }
}