package com.ispw.view.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuiLauncher extends Application {

	// SEZIONE ARCHITETTURALE
	// Legenda architettura:
	// A1) Collaboratori: entry point JavaFX per GUI.
	// A2) Stato: stage e callback di avvio.

	private static Stage stage;
	private static Runnable onReady;
	private static boolean launched;

	public static void launchApp(Runnable readyAction) {
		onReady = readyAction;
		if (!launched) {
			launched = true;
			Application.launch(GuiLauncher.class);
		} else if (readyAction != null) {
			Platform.runLater(readyAction);
		}
	}

	public static void setRoot(javafx.scene.Parent root) {
		if (stage == null) {
			return;
		}
		Platform.runLater(() -> stage.getScene().setRoot(root));
	}

	private static synchronized void setStage(Stage primaryStage) {
		stage = primaryStage;
	}

	// SEZIONE LOGICA
	// Legenda logica:
	// L1) launchApp: avvio JavaFX e callback.
	// L2) setRoot/setStage: gestione scena.
	// L3) start: inizializzazione stage.

	@Override
	public void start(Stage primaryStage) {
		setStage(primaryStage);
		stage.setTitle("ISPW");
		stage.setScene(new Scene(new VBox(), 800, 600));
		stage.show();
		if (onReady != null) {
			Platform.runLater(onReady);
		}
	}
}
