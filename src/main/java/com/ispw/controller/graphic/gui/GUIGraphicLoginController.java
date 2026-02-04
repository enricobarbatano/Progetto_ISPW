package com.ispw.controller.graphic.gui;

import java.util.Map;

import com.ispw.bean.DatiLoginBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicLoginController;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso;

/**
 * Adapter GUI per il login (JavaFX/Swing).
 */
public class GUIGraphicLoginController implements GraphicLoginController {
    
    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicLoginController(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return "login";
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    @Override
    public void effettuaLogin(DatiLoginBean credenziali) {
        if (credenziali == null) {
            if (navigator != null) {
                navigator.goTo("login", Map.of("error", "Credenziali mancanti"));
            }
            return;
        }
        
        LogicControllerGestioneAccesso logicController = new LogicControllerGestioneAccesso();
        SessioneUtenteBean sessione = logicController.verificaCredenziali(credenziali);
        
        if (sessione != null) {
            logicController.saveLog(sessione);
            if (navigator != null) {
                navigator.goTo("home", Map.of("sessione", sessione));
            }
        } else if (navigator != null) {
            navigator.goTo("login", Map.of("error", "Credenziali non valide"));
        }
    }

    @Override
    public void logout() {
        if (navigator != null) {
            navigator.goTo("login", null);
        }
    }

    @Override
    public void vaiARegistrazione() {
        if (navigator != null) {
            navigator.goTo("registrazione", null);
        }
    }

    @Override
    public void vaiAHome() {
        if (navigator != null) {
            navigator.goTo("home", null);
        }
    }
}
