package com.ispw.controller.graphic.abstracts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.interfaces.GraphicControllerLog;
import com.ispw.controller.graphic.interfaces.GraphicControllerLogUtils;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneAccount;

/**
 * Controller grafico astratto del caso d'uso "Consultazione log".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - richiede gli ultimi log al controller logico;
 * - formatta i log per la presentazione;
 * - aggiorna la route log tramite navigator.
 *
 * Nota di progetto:
 * la consultazione dei log viene delegata al controller logico della gestione account,
 * perché in questo progetto la lista log è esposta da quel caso d'uso.
 */
public abstract class AbstractGraphicControllerLog implements GraphicControllerLog {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerLog(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    // =====================================================================
    // LOGIC CONTROLLER
    // =====================================================================

    protected CtrlGestioneAccount logicController() {
        return LogicControllerFactory.getGestioneAccountController();
    }

    /**
     * Recupera e formatta gli ultimi log.
     *
     * Questo metodo contiene solo logica di presentazione:
     * la logica applicativa resta nel controller logico.
     */
    protected List<String> listaUltimiLog(int limit) {
        return GraphicControllerLogUtils.formatLogs(
                logicController().listaUltimiLog(limit)
        );
    }

    // =====================================================================
    // NAVIGAZIONE
    // =====================================================================

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGS;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, "[LOG]");
    }

    // STEP 1: richiesta log

    /**
     * Richiede gli ultimi log e aggiorna la route log.
     */
    @Override
    public void richiediLog(int limit) {
        try {
            List<String> logs = listaUltimiLog(limit);

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_LOGS,
                        Map.of(GraphicControllerUtils.KEY_LOGS, logs)
                );
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore caricamento log", ex);

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_LOGS,
                        Map.of(GraphicControllerUtils.KEY_ERROR, "Errore caricamento log")
                );
            }
        }
    }

    // STEP 2: ritorno home

    /**
     * Torna alla home delegando il comportamento concreto alla classe GUI o CLI.
     */
    @Override
    public void tornaAllaHome() {
        goToHome();
    }
}