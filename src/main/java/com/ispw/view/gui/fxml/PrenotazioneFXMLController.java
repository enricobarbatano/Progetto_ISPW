package com.ispw.view.gui.fxml;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.gui.GUIGraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.view.shared.PrenotazioneViewUtils;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PrenotazioneFXMLController {

    private GUIGraphicControllerPrenotazione controller;
    private SessioneUtenteBean sessione;

    private int lastCampoId = 0;
    private float lastImporto = 0f;

    // ===== Header =====
    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    // ===== Sezioni =====
    @FXML private VBox boxSearch;
    @FXML private VBox boxSlots;
    @FXML private VBox boxPagamento;
    @FXML private VBox boxEsito;

    // ===== Campi =====
    @FXML private ListView<String> listCampi;
    @FXML private TextField txtIdCampo;

    // ✅ Nuovi controlli moderni
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboOra;

    @FXML private TextField txtDurata;

    // ===== Slots =====
    @FXML private ListView<String> listSlots;

    // ===== Pagamento =====
    @FXML private Label lblRiepilogo;
    @FXML private Label lblImporto;
    @FXML private TextField txtMetodo;
    @FXML private TextField txtCredenziale;

    // ===== Esito =====
    @FXML private Label lblEsitoPagamento;

    /**
     * Installo comportamento moderno:
     * - click su campo -> auto-compila txtIdCampo e aggiorna lastCampoId
     * (così non perdi l'id e non compare più "Id campo mancante...")
     */
    @FXML
    private void initialize() {
        if (listCampi != null) {
            listCampi.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                Integer id = parseIdFromHashString(newV);
                if (id != null) {
                    lastCampoId = id;
                    if (txtIdCampo != null) txtIdCampo.setText(String.valueOf(id));
                    clearLocalError();
                }
            });
        }

        // opzionale: valori di default comodi
        if (datePicker != null && datePicker.getValue() == null) {
            datePicker.setValue(LocalDate.now());
        }
        if (comboOra != null && comboOra.getValue() == null && comboOra.getItems() != null && !comboOra.getItems().isEmpty()) {
            comboOra.getSelectionModel().selectFirst();
        }
        if (txtMetodo != null && (txtMetodo.getText() == null || txtMetodo.getText().isBlank())) {
            txtMetodo.setText("PAYPAL");
        }
    }

    public void init(GUIGraphicControllerPrenotazione controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    public void render(Map<String, Object> params) {
        Object err = params != null ? params.get(GraphicControllerUtils.KEY_ERROR) : null;

        Object ok = null;
        if (params != null) {
            ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
            if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
            if (ok == null) ok = params.get(GraphicControllerUtils.KEY_MESSAGGIO);
        }

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");

        // default: ricerca visibile
        show(boxSearch, true);
        show(boxSlots, false);
        show(boxPagamento, false);
        show(boxEsito, false);
        if (lblEsitoPagamento != null) lblEsitoPagamento.setText("");

        // campi
        if (listCampi != null) {
            listCampi.getItems().clear();
            Object rawCampi = params != null ? params.get(GraphicControllerUtils.KEY_CAMPI) : null;
            if (rawCampi instanceof List<?> campi) {
                for (Object o : campi) listCampi.getItems().add(String.valueOf(o));
            }
        }

        // slot disponibili
        if (listSlots != null) {
            listSlots.getItems().clear();
            Object rawSlots = params != null ? params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) : null;
            if (rawSlots instanceof List<?> slots) {
                show(boxSlots, true);
                for (Object o : slots) listSlots.getItems().add(String.valueOf(o));
            }
        }

        // riepilogo
        Object rawRiep = params != null ? params.get(GraphicControllerUtils.KEY_RIEPILOGO) : null;
        if (rawRiep instanceof Map<?, ?> riepilogo) {
            show(boxPagamento, true);

            Object riepilogoStr = riepilogo.get(GraphicControllerUtils.KEY_RIEPILOGO);
            Object importo = riepilogo.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);

            if (lblRiepilogo != null) lblRiepilogo.setText(riepilogoStr != null ? String.valueOf(riepilogoStr) : "");

            lastImporto = (importo instanceof Number n) ? n.floatValue() : 0f;
            if (lblImporto != null) lblImporto.setText(String.format("%.2f EUR", lastImporto));
        }

        // pagamento
        Object rawPay = params != null ? params.get(GraphicControllerUtils.KEY_PAGAMENTO) : null;
        if (rawPay instanceof Map<?, ?> pay) {
            Object success = pay.get(GraphicControllerUtils.KEY_SUCCESSO);
            Object stato = pay.get(GraphicControllerUtils.KEY_STATO);
            Object msg = pay.get(GraphicControllerUtils.KEY_MESSAGGIO);

            String esito = PrenotazioneViewUtils.formatEsitoPagamento(success, stato, msg);
            if (lblEsitoPagamento != null) lblEsitoPagamento.setText(esito);

            show(boxSearch, false);
            show(boxSlots, false);
            show(boxPagamento, false);
            show(boxEsito, true);
        }
    }

    // ===================== HANDLERS =====================

    @FXML
    private void onListaCampi() {
        clearLocalError();
        if (controller == null) return;
        controller.richiediListaCampi(sessione);
    }

    @FXML
    private void onCerca() {
        clearLocalError();
        if (controller == null) return;

        Integer idCampo = resolveCampoId();
        Integer durata = parsePositiveInt(txtDurata != null ? txtDurata.getText() : null);

        if (idCampo == null) {
            lblError.setText("Id campo non valido (seleziona dalla lista o inserisci un numero)");
            return;
        }
        if (durata == null) {
            lblError.setText("Durata non valida");
            return;
        }

        if (datePicker == null || datePicker.getValue() == null) {
            lblError.setText("Seleziona una data");
            return;
        }
        if (comboOra == null || comboOra.getValue() == null || comboOra.getValue().trim().isEmpty()) {
            lblError.setText("Seleziona un orario di inizio");
            return;
        }

        lastCampoId = idCampo;

        String data = datePicker.getValue().toString();   // yyyy-MM-dd
        String ora  = comboOra.getValue().trim();         // HH:mm

        controller.cercaDisponibilitaRaw(idCampo, data, ora, durata);
    }

    @FXML
    private void onSelezionaSlot() {
        clearLocalError();
        if (controller == null) return;

        if (listSlots == null) return;
        int idx = listSlots.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            lblError.setText("Seleziona uno slot dalla lista");
            return;
        }

        String slot = listSlots.getItems().get(idx);
        PrenotazioneViewUtils.SlotInfo info = PrenotazioneViewUtils.parseSlot(slot);
        if (info == null) {
            lblError.setText("Formato slot non valido: " + slot);
            return;
        }

        if (lastCampoId <= 0) {
            lblError.setText("Id campo mancante: seleziona un campo e rifai la ricerca");
            return;
        }

        controller.creaPrenotazioneRaw(lastCampoId, info.data(), info.oraInizio(), info.oraFine(), sessione);
    }

    @FXML
    private void onPaga() {
        clearLocalError();
        if (controller == null) return;

        String metodo = (txtMetodo != null && txtMetodo.getText() != null) ? txtMetodo.getText().trim() : "";
        String cred = (txtCredenziale != null && txtCredenziale.getText() != null) ? txtCredenziale.getText().trim() : "";

        if (metodo.isBlank()) {
            lblError.setText("Metodo pagamento mancante");
            return;
        }
        if (cred.isBlank()) {
            lblError.setText("Codice fiscale mancante");
            return;
        }
        if (lastImporto <= 0f) {
            lblError.setText("Importo non valido");
            return;
        }

        controller.procediAlPagamentoRaw(metodo, cred, lastImporto, sessione);
    }

    @FXML
    private void onHome() {
        clearLocalError();
        if (controller == null) return;
        controller.tornaAllaHome();
    }

    @FXML
    private void onReset() {
        clearLocalError();

        if (txtIdCampo != null) txtIdCampo.setText("");
        if (txtDurata != null) txtDurata.setText("");
        if (txtCredenziale != null) txtCredenziale.setText("");
        if (txtMetodo != null) txtMetodo.setText("PAYPAL");

        if (datePicker != null) datePicker.setValue(LocalDate.now());
        if (comboOra != null && comboOra.getItems() != null && !comboOra.getItems().isEmpty()) {
            comboOra.getSelectionModel().selectFirst();
        }

        lastCampoId = 0;
        lastImporto = 0f;

        if (lblRiepilogo != null) lblRiepilogo.setText("");
        if (lblImporto != null) lblImporto.setText("");
        if (lblEsitoPagamento != null) lblEsitoPagamento.setText("");
        if (lblSuccess != null) lblSuccess.setText("");

        show(boxSearch, true);
        show(boxSlots, false);
        show(boxPagamento, false);
        show(boxEsito, false);

        if (controller != null) controller.richiediListaCampi(sessione);
    }

    // ===================== HELPERS =====================

    private void show(VBox box, boolean v) {
        if (box == null) return;
        box.setVisible(v);
        box.setManaged(v);
    }

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
    }

    private Integer resolveCampoId() {
        if (lastCampoId > 0) return lastCampoId;
        return parsePositiveInt(txtIdCampo != null ? txtIdCampo.getText() : null);
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;
        try {
            int v = Integer.parseInt(t);
            return v > 0 ? v : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseIdFromHashString(String s) {
        if (s == null) return null;
        int hash = s.indexOf('#');
        if (hash < 0) return null;
        int i = hash + 1;
        int value = 0;
        boolean found = false;
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            found = true;
            value = value * 10 + Character.digit(s.charAt(i), 10);
            i++;
        }
        return found && value > 0 ? value : null;
    }
}
