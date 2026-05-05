package com.ispw.view.gui.fxml;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller per la gestione delle regole dei campi da parte del Gestore.
 * Permette di modificare stato, orari e policy di penalità.
 */
public class RegoleFXMLController {

    private GUIGraphicControllerRegole controller;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private ListView<String> listCampi;
    @FXML private TextField txtIdCampo;

    @FXML private CheckBox chkAttivo;
    @FXML private CheckBox chkManutenzione;

    @FXML private TextField txtDurata;
    @FXML private TextField txtApertura;
    @FXML private TextField txtChiusura;
    @FXML private TextField txtPreavviso;

    @FXML private TextField txtValorePenalita;
    @FXML private TextField txtPreavvisoPenalita;

    /**
     * Inizializzazione del controller. 
     * Nota: sessione rimosso dai campi della classe se non utilizzato nei metodi core.
     */
    public void init(GUIGraphicControllerRegole controller, SessioneUtenteBean sessione) {
        this.controller = controller;
    }

    /**
     * Aggiorna la vista con i dati (lista campi e messaggi di feedback).
     */
    public void render(Map<String, Object> params) {
        if (params == null) return;

        // 1. Gestione messaggi
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
        if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        // 2. Aggiornamento lista campi
        if (listCampi != null) {
            listCampi.getItems().clear();
            Object raw = params.get(GraphicControllerUtils.KEY_CAMPI);
            if (raw instanceof List<?> l) {
                for (Object o : l) listCampi.getItems().add(String.valueOf(o));
            }
        }
    }

    // --- GESTIONE EVENTI FXML ---

    @FXML
    public void onListaCampi() {
        clearLocalError();
        if (controller != null) {
            controller.richiediListaCampi();
        }
    }

    @FXML
    public void onSelezionaCampo() {
        if (listCampi == null) return;
        String selectedItem = listCampi.getSelectionModel().getSelectedItem();
        
        if (selectedItem != null) {
            Integer id = parseIdFromHashString(selectedItem);
            if (id != null) {
                txtIdCampo.setText(String.valueOf(id));
                lblError.setText("");
            } else {
                lblError.setText("Errore nell'estrazione dell'ID");
            }
        }
    }

   @FXML
    public void onAggiornaStato() {
        clearLocalError();
        if (controller == null) return;

        Integer idObj = parsePositiveInt(txtIdCampo.getText());
        
        // ✅ RISOLTO "Unboxing possibly null value":
        // Prima verifichiamo che l'oggetto non sia null, poi lo usiamo.
        if (idObj == null) { 
            lblError.setText("Id campo non valido"); 
            return; 
        }

        // Ora passiamo idObj con la certezza che non sia null
        Map<String, Object> payload = new HashMap<>();
        payload.put(GraphicControllerUtils.KEY_ID_CAMPO, idObj);
        payload.put(GraphicControllerUtils.KEY_ATTIVO, chkAttivo.isSelected());
        payload.put(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE, chkManutenzione.isSelected());
        
        controller.aggiornaStatoCampo(payload);
    }
    @FXML
    public void onAggiornaTempistiche() {
        clearLocalError();
        if (controller == null) return;

        Integer durata = parsePositiveInt(txtDurata.getText());
        Integer preavv = parseNonNegativeInt(txtPreavviso.getText(), null);
        
        if (durata == null || preavv == null) { 
            lblError.setText("Durata/Preavviso non validi"); 
            return; 
        }

        try {
            LocalTime apertura = LocalTime.parse(txtApertura.getText().trim());
            LocalTime chiusura = LocalTime.parse(txtChiusura.getText().trim());

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI, durata);
            payload.put(GraphicControllerUtils.KEY_ORA_APERTURA, apertura);
            payload.put(GraphicControllerUtils.KEY_ORA_CHIUSURA, chiusura);
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, preavv);
            
            controller.aggiornaTempistiche(payload);

        } catch (java.time.format.DateTimeParseException e) { // ✅ Catch specifico
            lblError.setText("Formato ora non valido (usare HH:mm)");
        }
    }

    @FXML
    public void onAggiornaPenalita() {
        clearLocalError();
        if (controller == null) return;

        try {
            // ✅ Risolto "Unnecessary temporary": parsing diretto
            BigDecimal valore = new BigDecimal(txtValorePenalita.getText().trim());
            Integer preavv = parseNonNegativeInt(txtPreavvisoPenalita.getText(), 0);

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_VALORE_PENALITA, valore);
            payload.put(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI, preavv);
            
            controller.aggiornaPenalita(payload);

        } catch (NumberFormatException e) { // ✅ Catch specifico
            lblError.setText("Valore penalità non valido");
        }
    }

    @FXML
    public void onHome() {
        if (controller != null) {
            controller.tornaAllaHome();
        }
    }

    // --- HELPER METHODS ---

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
        if (lblSuccess != null) lblSuccess.setText("");
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int v = Integer.parseInt(raw.trim());
            // Se v > 0 restituiamo l'Integer, altrimenti null
            // Evitiamo Integer.valueOf(v) esplicito se causa "Unnecessary boxing"
            return (v > 0) ? v : null; 
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseNonNegativeInt(String raw, Integer def) {
        if (raw == null || raw.isBlank()) return def;
        try {
            int v = Integer.parseInt(raw.trim());
            return (v >= 0) ? v : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Estrae l'ID numerico da una stringa del tipo "#123 - Nome Campo"
     */
    private Integer parseIdFromHashString(String s) {
        if (s == null || !s.contains("#")) return null;
        try {
            String sub = s.split("#")[1].split(" ")[0].replaceAll("[^0-9]", "");
            if (sub.isEmpty()) return null;
            
            // ✅ RISOLTO "Unnecessary boxing": 
            // Usiamo direttamente Integer.parseInt. Java gestirà l'autoboxing 
            // verso il tipo di ritorno Integer in modo ottimale.
            return Integer.parseInt(sub);
        } catch (Exception e) { 
            // Qui manteniamo Exception generico perché split/replaceAll 
            // possono lanciare diverse runtime exceptions
            return null;
        }
    }
}