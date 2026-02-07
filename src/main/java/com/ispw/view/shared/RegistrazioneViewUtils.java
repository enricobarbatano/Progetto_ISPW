package com.ispw.view.shared;

import java.util.HashMap;
import java.util.Map;

import com.ispw.controller.graphic.GraphicControllerUtils;

public final class RegistrazioneViewUtils {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: utility condivisa per view registrazione.
    // A2) IO: produce Map per controller grafico.

    private RegistrazioneViewUtils() {
    }

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) buildForm: costruisce i parametri di registrazione.

    public static Map<String, Object> buildForm(String nome, String cognome, String email, String password) {
        Map<String, Object> form = new HashMap<>();
        form.put(GraphicControllerUtils.KEY_NOME, nome);
        form.put(GraphicControllerUtils.KEY_COGNOME, cognome);
        form.put(GraphicControllerUtils.KEY_EMAIL, email);
        form.put(GraphicControllerUtils.KEY_PASSWORD, password);
        return form;
    }
}
