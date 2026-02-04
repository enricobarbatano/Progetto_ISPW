package com.ispw.controller.graphic.cli;

import java.util.Map;

import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.logic.ctrl.LogicControllerConfiguraRegole;

/**
 * Adapter CLI per configurazione regole campo.
 */
public class CLIGraphicControllerRegole implements GraphicControllerRegole {
    
    private LogicControllerConfiguraRegole logicController;
    private GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerRegole() {
    }
    
    public CLIGraphicControllerRegole(
        LogicControllerConfiguraRegole logicController,
        GraphicControllerNavigation navigator) {
        this.logicController = logicController;
        this.navigator = navigator;
    }
    
    public void setLogicController(LogicControllerConfiguraRegole controller) {
        this.logicController = controller;
    }
    
    @Override
    public String getRouteName() {
        return "regole";
    }

    @Override
    public void setNavigator(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void richiediListaCampi() {
        // List<DatiCampoBean> campi = logicController.recuperaCampi();
        // view.mostraCampi(campi);
    }

    @Override
    public void selezionaCampo(int idCampo) {
        // Memorizza selezione
    }

    /**
     * Aggiorna stato/regole del campo.
     */
    @Override
    public void aggiornaStatoCampo(Map<String, Object> regolaCampo) {
        if (regolaCampo == null) {
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
        
        EsitoOperazioneBean esito = logicController.aggiornaRegoleCampo(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // Notifica View
        }
    }

    @Override
    public void aggiornaTempistiche(Map<String, Object> tempistiche) {
        if (tempistiche == null) {
            return;
        }
        
        TempisticheBean bean = new TempisticheBean();
        if (tempistiche.containsKey("preavvisoMinimoMinuti")) {
            bean.setPreavvisoMinimoMinuti((Integer) tempistiche.get("preavvisoMinimoMinuti"));
        }
        if (tempistiche.containsKey("durataSlotMinuti")) {
            bean.setDurataSlotMinuti((Integer) tempistiche.get("durataSlotMinuti"));
        }
        
        EsitoOperazioneBean esito = logicController.aggiornaRegolaTempistiche(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // Notifica View
        }
    }

    @Override
    public void aggiornaPenalita(Map<String, Object> penalita) {
        if (penalita == null) {
            return;
        }
        
        PenalitaBean bean = new PenalitaBean();
        if (penalita.containsKey("preavvisoMinimoMinuti")) {
            bean.setPreavvisoMinimoMinuti((Integer) penalita.get("preavvisoMinimoMinuti"));
        }
        // Nota: BigDecimal per valorePenalita
        
        EsitoOperazioneBean esito = logicController.aggiornaRegolepenalita(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // Notifica View
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
