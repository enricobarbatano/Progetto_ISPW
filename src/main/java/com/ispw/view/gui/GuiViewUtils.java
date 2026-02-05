package com.ispw.view.gui;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public final class GuiViewUtils {

    private GuiViewUtils() {
        // utility class
    }

    public static VBox createRoot() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        return root;
    }

    public static Label buildErrorLabel(String message) {
        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");
        if (message != null && !message.isBlank()) {
            error.setText(message);
        }
        return error;
    }

    public static Label buildSuccessLabel(String message) {
        Label success = new Label();
        if (message != null && !message.isBlank()) {
            success.setText(message);
        }
        return success;
    }

    public static Button buildHomeButton(Runnable action) {
        Button home = new Button("Home");
        home.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
        return home;
    }

    public static void fillList(ListView<String> listView, List<?> items) {
        if (listView == null) return;
        listView.getItems().clear();
        if (items == null) return;
        for (Object item : items) {
            listView.getItems().add(String.valueOf(item));
        }
    }
}
