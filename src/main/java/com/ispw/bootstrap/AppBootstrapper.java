package com.ispw.bootstrap;


import com.ispw.controller.factory.FrontendControllerFactory;
import com.ispw.dao.factory.DAOFactory;

public final class AppBootstrapper {

    public static void main(String[] args) {

        // 1) Leggi configurazione da input utente (console)
        AppConfigurator configurator = new AppConfigurator();
        AppConfig config = configurator.askUserConfiguration();

        // 2) Configura la persistenza (Singleton DAOFactory guidata da PersistencyProvider) [1](https://uniroma2-my.sharepoint.com/personal/enrico_barbatano_students_uniroma2_eu/Documents/File%20chat%20di%20Microsoft%20Copilot/ClassDiagramMVC.drawio%20(1).xml).xml).xml).xml).xml)
        DAOFactory.setPersistencyProvider(config.persistency());

        // 3) Configura il frontend (Singleton FrontendControllerFactory)
       
        FrontendControllerFactory.setFrontendProvider(config.frontend());

        // 4) Avvio UI (UNA sola volta)
        FrontendControllerFactory.getInstance().startApplication();
    }
}


