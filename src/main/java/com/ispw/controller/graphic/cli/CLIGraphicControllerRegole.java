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
                navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE, Map.of("campi", campi));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista campi", e);
        }
    }

    @Override
    public void selezionaCampo(int idCampo) {
        if (idCampo <= 0) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE, "Id campo non valido");
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE, Map.of("idCampo", idCampo));
        }
    }

    /**
     * Aggiorna stato/regole del campo.
     */
    @Override
    public void aggiornaStatoCampo(Map<String, Object> regolaCampo) {
        if (regolaCampo == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE, "Parametri regola campo mancanti");
            return;
        }
        
        RegolaCampoBean bean = new RegolaCampoBean();
        if (regolaCampo.containsKey("idCampo")) {
            bean.setIdCampo((Integer) regolaCampo.get("idCampo"));
        }
        if (regolaCampo.containsKey("attivo")) {
            bean.setAttivo((Boolean) regolaCampo.get("attivo"));
        }
        if (regolaCampo.containsKey("flagManutenzione")) {
            bean.setFlagManutenzione((Boolean) regolaCampo.get("flagManutenzione"));
        }
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegoleCampo(bean);
        
        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE,
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void aggiornaTempistiche(Map<String, Object> tempistiche) {
        if (tempistiche == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE, "Parametri tempistiche mancanti");
            return;
        }
        
        TempisticheBean bean = new TempisticheBean();
        if (tempistiche.containsKey("preavvisoMinimoMinuti")) {
            bean.setPreavvisoMinimoMinuti((Integer) tempistiche.get("preavvisoMinimoMinuti"));
        }
        if (tempistiche.containsKey("durataSlotMinuti")) {
            bean.setDurataSlotMinuti((Integer) tempistiche.get("durataSlotMinuti"));
        }
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegolaTempistiche(bean);
        
        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE,
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void aggiornaPenalita(Map<String, Object> penalita) {
        if (penalita == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE, "Parametri penalità mancanti");
            return;
        }
        
        PenalitaBean bean = new PenalitaBean();
        if (penalita.containsKey("preavvisoMinimoMinuti")) {
            bean.setPreavvisoMinimoMinuti((Integer) penalita.get("preavvisoMinimoMinuti"));
        }
        // Nota: BigDecimal per valorePenalita
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegolepenalita(bean);
        
        if (esito != null && esito.isSuccesso()) {
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } else {
            GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
                    GraphicControllerUtils.PREFIX_REGOLE,
                    esito != null ? esito.getMessaggio() : "Operazione non riuscita");
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

}
