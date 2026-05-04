package com.ispw.view.gui;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.graphic.interfaces.NavigableController;
import com.ispw.view.gui.fxml.HomeFXMLController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class GUIHomeView extends GenericViewGUI implements NavigableController {

    private final GraphicControllerNavigation navigator;

    public GUIHomeView(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_HOME;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            HomeFXMLController fx = loader.getController();
            SessioneUtenteBean s = this.sessione;
            fx.init(navigator, s);

            GuiLauncher.setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            GuiLauncher.setRoot(GuiViewUtils.createRoot());
        }
    }
}