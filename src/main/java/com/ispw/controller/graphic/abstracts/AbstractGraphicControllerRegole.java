package com.ispw.controller.graphic.abstracts;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneRegole;

/**
 * Controller grafico astratto del caso d'uso "Configura regole".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - richiede la lista dei campi;
 * - seleziona un campo;
 * - costruisce i bean per regole campo, tempistiche e penalità;
 * - chiama il controller logico tramite interfaccia;
 * - aggiorna la route regole tramite navigator.
 *
 * Le classi concrete GUI e CLI gestiscono solo le differenze specifiche
 * del frontend, come il ritorno alla home.
 *
 * Nota di progetto:
 * il graphic controller non conosce l'implementazione concreta del logic controller.
 * Usa CtrlGestioneRegole ottenuto tramite LogicControllerFactory.
 */
public abstract class AbstractGraphicControllerRegole implements GraphicControllerRegole {

    // =====================================================================
    // COLLABORATORI
    // =====================================================================

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRegole(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================

    protected CtrlGestioneRegole logicController() {
        return LogicControllerFactory.getGestioneRegoleController();
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // In questa schermata non è necessario gestire parametri in modo comune.
    }

    // STEP 1: lista campi

    /**
     * Richiede la lista dei campi configurabili.
     *
     * Il metodo:
     * - chiama il controller logico;
     * - formatta i campi;
     * - aggiorna la route regole tramite navigator.
     */
    @Override
    public void richiediListaCampi() {
        try {
            List<String> campi = formatCampi(logicController().listaCampi());

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_REGOLE,
                        Map.of(GraphicControllerUtils.KEY_CAMPI, campi)
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore recupero lista campi", ex);
        }
    }

    // STEP 2: selezione campo

    /**
     * Seleziona un campo da configurare.
     *
     * Il metodo non modifica dati persistenti:
     * aggiorna soltanto la route con l'id del campo selezionato.
     */
    @Override
    public void selezionaCampo(int idCampo) {
        if (isIdCampoNonValido(idCampo)) {
            return;
        }

        if (navigator != null) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_REGOLE,
                    Map.of(GraphicControllerUtils.KEY_ID_CAMPO, idCampo)
            );
        }
    }

    // STEP 3: aggiorna stato campo

    /**
     * Aggiorna lo stato operativo di un campo.
     *
     * Il metodo:
     * - controlla i parametri;
     * - costruisce RegolaCampoBean;
     * - chiama il controller logico;
     * - naviga con successo o mostra errore.
     */
    @Override
    public void aggiornaStatoCampo(Map<String, Object> regolaCampo) {
        if (isNullParams(regolaCampo, GraphicControllerUtils.MSG_PARAMETRI_REGOLA_CAMPO_MANCANTI)) {
            return;
        }

        RegolaCampoBean bean = buildRegolaCampoBean(regolaCampo);
        EsitoOperazioneBean esito = logicController().aggiornaRegoleCampo(bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null
                    ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 4: aggiorna tempistiche

    /**
     * Aggiorna le regole temporali di prenotazione.
     *
     * Il metodo:
     * - controlla i parametri;
     * - costruisce TempisticheBean;
     * - chiama il controller logico;
     * - naviga con successo o mostra errore.
     */
    @Override
    public void aggiornaTempistiche(Map<String, Object> tempistiche) {
        if (isNullParams(tempistiche, GraphicControllerUtils.MSG_PARAMETRI_TEMPISTICHE_MANCANTI)) {
            return;
        }

        TempisticheBean bean = buildTempisticheBean(tempistiche);
        EsitoOperazioneBean esito = logicController().aggiornaRegolaTempistiche(bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null
                    ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 5: aggiorna penalità

    /**
     * Aggiorna le regole di penalità.
     *
     * Il metodo:
     * - controlla i parametri;
     * - costruisce PenalitaBean;
     * - chiama il controller logico;
     * - naviga con successo o mostra errore.
     */
    @Override
    public void aggiornaPenalita(Map<String, Object> penalita) {
        if (isNullParams(penalita, GraphicControllerUtils.MSG_PARAMETRI_PENALITA_MANCANTI)) {
            return;
        }

        PenalitaBean bean = buildPenalitaBean(penalita);
        EsitoOperazioneBean esito = logicController().aggiornaRegolepenalita(bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null
                    ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    // STEP 6: ritorno home

    /**
     * Torna alla home delegando il comportamento concreto alla classe GUI o CLI.
     */
    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // =====================================================================
    // HELPERS REGOLE
    // =====================================================================

    /**
     * Notifica un errore relativo alla configurazione regole.
     */
    private void notifyRegoleError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_REGOLE,
                GraphicControllerUtils.PREFIX_REGOLE,
                message
        );
    }

    /**
     * Controlla se l'id del campo è valido.
     */
    private boolean isIdCampoNonValido(int idCampo) {
        if (idCampo <= 0) {
            notifyRegoleError(GraphicControllerUtils.MSG_ID_CAMPO_NON_VALIDO);
            return true;
        }
        return false;
    }

    /**
     * Controlla che la mappa parametri non sia nulla.
     */
    private boolean isNullParams(Map<String, Object> params, String message) {
        if (params == null) {
            notifyRegoleError(message);
            return true;
        }
        return false;
    }

    /**
     * Naviga sulla route regole mostrando un messaggio di successo.
     */
    private void navigateSuccess(String message) {
        if (navigator != null) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_REGOLE,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, message)
            );
        }
    }

    /**
     * Costruisce il bean delle regole operative del campo.
     */
    private RegolaCampoBean buildRegolaCampoBean(Map<String, Object> regolaCampo) {
        RegolaCampoBean bean = new RegolaCampoBean();

        if (regolaCampo.containsKey(GraphicControllerUtils.KEY_ID_CAMPO)) {
            bean.setIdCampo((Integer) regolaCampo.get(GraphicControllerUtils.KEY_ID_CAMPO));
        }

        if (regolaCampo.containsKey(GraphicControllerUtils.KEY_ATTIVO)) {
            bean.setAttivo((Boolean) regolaCampo.get(GraphicControllerUtils.KEY_ATTIVO));
        }

        if (regolaCampo.containsKey(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE)) {
            bean.setFlagManutenzione((Boolean) regolaCampo.get(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE));
        }

        return bean;
    }

    /**
     * Formatta la lista dei campi in stringhe leggibili da GUI/CLI.
     */
    private List<String> formatCampi(CampiBean campi) {
        if (campi == null || campi.getCampi() == null || campi.getCampi().isEmpty()) {
            return List.of("Nessun campo disponibile");
        }

        return campi.getCampi().stream()
                .map(c -> String.format("#%d - %s (%s) [attivo=%s, manutenzione=%s]",
                        c.getIdCampo(),
                        c.getNome(),
                        c.getTipoSport(),
                        c.isAttivo(),
                        c.isFlagManutenzione()))
                .toList();
    }

    /**
     * Costruisce il bean delle regole temporali.
     */
    private TempisticheBean buildTempisticheBean(Map<String, Object> tempistiche) {
        TempisticheBean bean = new TempisticheBean();

        if (tempistiche.containsKey(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI)) {
            bean.setPreavvisoMinimoMinuti((Integer) tempistiche.get(
                    GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI));
        }

        if (tempistiche.containsKey(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI)) {
            bean.setDurataSlotMinuti((Integer) tempistiche.get(
                    GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI));
        }

        if (tempistiche.containsKey(GraphicControllerUtils.KEY_ORA_APERTURA)) {
            bean.setOraApertura((LocalTime) tempistiche.get(
                    GraphicControllerUtils.KEY_ORA_APERTURA));
        }

        if (tempistiche.containsKey(GraphicControllerUtils.KEY_ORA_CHIUSURA)) {
            bean.setOraChiusura((LocalTime) tempistiche.get(
                    GraphicControllerUtils.KEY_ORA_CHIUSURA));
        }

        return bean;
    }

    /**
     * Costruisce il bean delle regole penalità.
     */
    private PenalitaBean buildPenalitaBean(Map<String, Object> penalita) {
        PenalitaBean bean = new PenalitaBean();

        if (penalita.containsKey(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI)) {
            bean.setPreavvisoMinimoMinuti((Integer) penalita.get(
                    GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI));
        }

        if (penalita.containsKey(GraphicControllerUtils.KEY_VALORE_PENALITA)) {
            bean.setValorePenalita((BigDecimal) penalita.get(
                    GraphicControllerUtils.KEY_VALORE_PENALITA));
        }

        return bean;
    }
}