package com.ispw.view.gui;

import java.util.Map;

import com.ispw.controller.graphic.gui.GUIGraphicLoginController;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.LoginFXMLController;
import com.ispw.view.interfaces.ViewLogin;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class GUILoginView extends GenericViewGUI implements ViewLogin, NavigableController {

    private final GUIGraphicLoginController controller;

    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_LOGIN;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        sessione = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginFXMLController fx = loader.getController();
            fx.setGraphicController(controller);
            fx.render(getLastParams());

            GuiLauncher.setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            // fallback minimo
            GuiLauncher.setRoot(GuiViewUtils.createRoot());
        }
    }
}
