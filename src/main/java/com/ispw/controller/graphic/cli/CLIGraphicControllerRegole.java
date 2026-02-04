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
    
    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerRegole(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "regole";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    @Override
    public void richiediListaCampi() {
        // TODO: implementare richiesta lista campi
    }

    @Override
    public void selezionaCampo(int idCampo) {
        // TODO: memorizzare selezione
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
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
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
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegolaTempistiche(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // TODO: notificare View
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
        
        LogicControllerConfiguraRegole logicController = new LogicControllerConfiguraRegole();
        EsitoOperazioneBean esito = logicController.aggiornaRegolepenalita(bean);
        
        if (esito != null && esito.isSuccesso()) {
            // TODO: notificare View
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }
}
