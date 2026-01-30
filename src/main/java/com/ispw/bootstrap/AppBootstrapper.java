
package com.ispw.bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import com.ispw.controller.factory.FrontendControllerFactory;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.impl.dbms.connection.DbmsConnectionFactory;
import com.ispw.model.enums.PersistencyProvider;

public final class AppBootstrapper {

    public static void main(String[] args) {

        // 1) Leggi configurazione
        AppConfigurator configurator = new AppConfigurator();
        AppConfig config = configurator.askUserConfiguration();

        // 2) Config DBMS (solo se DBMS)
        if (config.persistency() == PersistencyProvider.DBMS) {

            DbmsConnectionFactory.init(
                "jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome",
                "ispw_user",
                "ispw_user"
            );

            // (Opzionale) ping DB per verificare connessione
            try (var c  = DbmsConnectionFactory.getInstance().getConnection();
                 var ps = c.prepareStatement("SELECT 1");
                 var rs = ps.executeQuery()) {

                if (rs.next()) {
                    System.out.println("DB OK: " + rs.getInt(1));
                }

            } catch (SQLException e) {
                System.err.println("Errore connessione DB: " + e.getMessage());
                
                return;
            }
        }

        // 2b) Config FILE_SYSTEM (solo se FILE_SYSTEM)
        if (config.persistency() == PersistencyProvider.FILE_SYSTEM) {

            Path root = Paths.get("C:\\Users\\User\\OneDrive\\Desktop\\Progetti_Uni\\Progetto_ISPW\\FileSystem");

            try {
                Files.createDirectories(root);
            } catch (Exception e) {
                System.err.println("Impossibile creare directory root per FILE_SYSTEM: " + root);
                
                return;
            }

            DAOFactory.setFileSystemRoot(root);
            System.out.println("FILE_SYSTEM root impostata su: " + root.toAbsolutePath());
        }

        // 3) Configura persistenza (DAOFactory guidata dal provider)
        DAOFactory.setPersistencyProvider(config.persistency());

        // 4) Configura frontend
        FrontendControllerFactory.setFrontendProvider(config.frontend());

        // 5) Avvio UI
        FrontendControllerFactory.getInstance().startApplication();
    }
}
