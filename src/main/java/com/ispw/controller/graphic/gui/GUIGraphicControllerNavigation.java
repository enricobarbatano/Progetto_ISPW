package com.ispw.controller.graphic.gui;

import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.NavigableController;

/**
 * Navigation router per GUI (JavaFX/Swing).
 */
public class GUIGraphicControllerNavigation implements GraphicControllerNavigation {
    
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, NavigableController> routes = new HashMap<>();
    
    public GUIGraphicControllerNavigation() {
    }
    
    @Override
    public void goTo(String route, Map<String, Object> params) {
        if (route == null) {
            return;
        }
        
        NavigableController controller = routes.get(route);
        if (controller != null) {
            controller.onShow(params);
            // GUI: cambia scene/panel attivo
        }
    }

    @Override
    public void back() {
        // Implementa stack di navigazione per GUI
    }

    @Override
    public void exit() {
        System.exit(0);
    }
}
