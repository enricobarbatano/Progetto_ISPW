package com.ispw.controller.graphic.cli;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerConfiguraRegole;

/**
 * Adapter CLI per configurazione regole campo.
 */
public class CLIGraphicControllerRegole implements GraphicControllerRegole {
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerRegole(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    @Override
    public void richiediListaCampi() {
        try {
            LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
            List<String> campi = logicController.listaCampi();
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                    Map.of(GraphicControllerUtils.KEY_CAMPI, campi));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista campi", e);
        }
    }

    @Override
    public void selezionaCampo(int idCampo) {
        if (isIdCampoNonValido(idCampo)) {
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                Map.of(GraphicControllerUtils.KEY_ID_CAMPO, idCampo));
        }
    }

    /**
     * Aggiorna stato/regole del campo.
     */
    @Override
    public void aggiornaStatoCampo(Map<String, Object> regolaCampo) {
        if (isNullParams(regolaCampo, "Parametri regola campo mancanti")) {
            return;
        }
        
        RegolaCampoBean bean = buildRegolaCampoBean(regolaCampo);
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegoleCampo(bean);
        
        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void aggiornaTempistiche(Map<String, Object> tempistiche) {
        if (isNullParams(tempistiche, "Parametri tempistiche mancanti")) {
            return;
        }
        
        TempisticheBean bean = buildTempisticheBean(tempistiche);
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegolaTempistiche(bean);
        
        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void aggiornaPenalita(Map<String, Object> penalita) {
        if (isNullParams(penalita, "Parametri penalità mancanti")) {
            return;
        }
        
        PenalitaBean bean = buildPenalitaBean(penalita);
        // Nota: BigDecimal per valorePenalita
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegolepenalita(bean);
        
        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

    private void notifyRegoleError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
            GraphicControllerUtils.PREFIX_REGOLE, message);
    }

    private boolean isIdCampoNonValido(int idCampo) {
        if (idCampo <= 0) {
            notifyRegoleError("Id campo non valido");
            return true;
        }
        return false;
    }

    private boolean isNullParams(Map<String, Object> params, String message) {
        if (params == null) {
            notifyRegoleError(message);
            return true;
        }
        return false;
    }

    private void navigateSuccess(String message) {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                Map.of(GraphicControllerUtils.KEY_SUCCESSO, message));
        }
    }

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

    private TempisticheBean buildTempisticheBean(Map<String, Object> tempistiche) {
        TempisticheBean bean = new TempisticheBean();
        if (tempistiche.containsKey(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI)) {
            bean.setPreavvisoMinimoMinuti((Integer) tempistiche.get(
                GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI));
        }
        if (tempistiche.containsKey(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI)) {
            bean.setDurataSlotMinuti((Integer) tempistiche.get(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI));
        }
        return bean;
    }

    private PenalitaBean buildPenalitaBean(Map<String, Object> penalita) {
        PenalitaBean bean = new PenalitaBean();
        if (penalita.containsKey(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI)) {
            bean.setPreavvisoMinimoMinuti((Integer) penalita.get(
                GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI));
        }
        return bean;
    }

}
