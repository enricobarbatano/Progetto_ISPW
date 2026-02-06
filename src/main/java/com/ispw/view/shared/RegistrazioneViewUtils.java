package com.ispw.view.shared;

import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;

public final class RegistrazioneViewUtils {

    private RegistrazioneViewUtils() {
    }

    public static Map<String, Object> buildForm(String nome, String cognome, String email, String password) {
        Map<String, Object> form = new HashMap<>();
        form.put(GraphicControllerUtils.KEY_NOME, nome);
        form.put(GraphicControllerUtils.KEY_COGNOME, cognome);
        form.put(GraphicControllerUtils.KEY_EMAIL, email);
        form.put(GraphicControllerUtils.KEY_PASSWORD, password);
        return form;
    }
}
