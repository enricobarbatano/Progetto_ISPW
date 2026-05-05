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

/**
 * Controller per il wizard di prenotazione (Ricerca -> Slot -> Pagamento -> Esito).
 */
public class PrenotazioneFXMLController {

    private GUIGraphicControllerPrenotazione controller;
    private SessioneUtenteBean sessione;

    private int lastCampoId = 0;
    private float lastImporto = 0f;

    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    @FXML private VBox boxSearch;
    @FXML private VBox boxSlots;
    @FXML private VBox boxPagamento;
    @FXML private VBox boxEsito;

    @FXML private ListView<String> listCampi;
    @FXML private TextField txtIdCampo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboOra;
    @FXML private TextField txtDurata;

    @FXML private ListView<String> listSlots;

    @FXML private Label lblRiepilogo;
    @FXML private Label lblImporto;
    @FXML private TextField txtMetodo;
    @FXML private TextField txtCredenziale;

    @FXML private Label lblEsitoPagamento;

    /**
     * Inizializzazione: imposta i listener e i valori di default.
     */
    @FXML
    public void initialize() {
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

        // Valori di default per la UX
        if (datePicker != null && datePicker.getValue() == null) {
            datePicker.setValue(LocalDate.now());
        }
        if (txtMetodo != null && (txtMetodo.getText() == null || txtMetodo.getText().isBlank())) {
            txtMetodo.setText("PAYPAL");
        }
    }

    public void init(GUIGraphicControllerPrenotazione controller, SessioneUtenteBean sessione) {
        this.controller = controller;
        this.sessione = sessione;
    }

    /**
     * Metodo centrale per l'aggiornamento della vista basato sullo stato del wizard.
     */
    public void render(Map<String, Object> params) {
        if (params == null) return;

        // 1. Messaggi di stato
        handleMessages(params);

        // 2. Visibilità sezioni (Workflow del wizard)
        updateWorkflowVisibility(params);

        // 3. Popolamento dati (Campi, Slot, Pagamento)
        updateDataLists(params);
        handleRiepilogo(params.get(GraphicControllerUtils.KEY_RIEPILOGO));
        handleEsitoPagamento(params.get(GraphicControllerUtils.KEY_PAGAMENTO));
    }

    // --- HANDLERS EVENTI FXML (Resi public per eliminare i warning) ---

    @FXML
    public void onListaCampi() {
        clearLocalError();
        if (controller != null) controller.richiediListaCampi(sessione);
    }

    @FXML
    public void onCerca() {
        clearLocalError();
        if (controller == null) return;

        Integer idCampo = resolveCampoId();
        Integer durata = parsePositiveInt(txtDurata != null ? txtDurata.getText() : null);

        if (idCampo == null || durata == null || datePicker.getValue() == null || isComboOraEmpty()) {
            lblError.setText("Dati ricerca incompleti o non validi.");
            return;
        }

        lastCampoId = idCampo;
        String data = datePicker.getValue().toString();
        String ora = comboOra.getValue().trim();

        controller.cercaDisponibilitaRaw(idCampo, data, ora, durata);
    }

    @FXML
    public void onSelezionaSlot() {
        clearLocalError();
        if (controller == null || listSlots == null) return;

        int idx = listSlots.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            lblError.setText("Seleziona uno slot dalla lista.");
            return;
        }

        String slot = listSlots.getItems().get(idx);
        PrenotazioneViewUtils.SlotInfo info = PrenotazioneViewUtils.parseSlot(slot);
        
        if (info == null || lastCampoId <= 0) {
            lblError.setText("Errore nella selezione dello slot. Riprova.");
            return;
        }

        controller.creaPrenotazioneRaw(lastCampoId, info.data(), info.oraInizio(), info.oraFine(), sessione);
    }

    @FXML
    public void onPaga() {
        clearLocalError();
        if (controller == null) return;

        String metodo = getSafeText(txtMetodo);
        String cred = getSafeText(txtCredenziale);

        if (metodo.isEmpty() || cred.isEmpty() || lastImporto <= 0f) {
            lblError.setText("Dati di pagamento incompleti.");
            return;
        }

        controller.procediAlPagamentoRaw(metodo, cred, lastImporto, sessione);
    }

    @FXML
    public void onHome() {
        if (controller != null) controller.tornaAllaHome();
    }

    @FXML
    public void onIndietroSlots() {
        show(boxPagamento, false);
        show(boxSlots, true);
    }

    @FXML
    public void onReset() {
        clearLocalError();
        resetFields();
        show(boxSearch, true);
        show(boxSlots, false);
        show(boxPagamento, false);
        show(boxEsito, false);
        if (controller != null) controller.richiediListaCampi(sessione);
    }

    // --- HELPER DI SUPPORTO ---

    private void handleMessages(Map<String, Object> params) {
        Object err = params.get(GraphicControllerUtils.KEY_ERROR);
        Object ok = params.get(GraphicControllerUtils.KEY_MESSAGE);
        if (ok == null) ok = params.get(GraphicControllerUtils.KEY_SUCCESSO);
        if (ok == null) ok = params.get(GraphicControllerUtils.KEY_MESSAGGIO);

        lblError.setText(err != null ? String.valueOf(err) : "");
        lblSuccess.setText(ok != null ? String.valueOf(ok) : "");
    }

    private void updateWorkflowVisibility(Map<String, Object> params) {
        // Logica di progressione automatica basata sulla presenza di dati specifici
        boolean hasSlots = params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) != null;
        boolean hasRiep = params.get(GraphicControllerUtils.KEY_RIEPILOGO) != null;
        boolean hasPay = params.get(GraphicControllerUtils.KEY_PAGAMENTO) != null;

        show(boxSearch, !hasPay);
        show(boxSlots, hasSlots && !hasRiep && !hasPay);
        show(boxPagamento, hasRiep && !hasPay);
        show(boxEsito, hasPay);
    }

    private void updateDataLists(Map<String, Object> params) {
        // Lista Campi
        if (listCampi != null) {
            listCampi.getItems().clear();
            if (params.get(GraphicControllerUtils.KEY_CAMPI) instanceof List<?> campi) {
                campi.forEach(c -> listCampi.getItems().add(String.valueOf(c)));
            }
        }
        // Lista Slot
        if (listSlots != null) {
            listSlots.getItems().clear();
            if (params.get(GraphicControllerUtils.KEY_SLOT_DISPONIBILI) instanceof List<?> slots) {
                slots.forEach(s -> listSlots.getItems().add(String.valueOf(s)));
            }
        }
    }

    private void handleRiepilogo(Object rawRiep) {
        if (rawRiep instanceof Map<?, ?> riep) {
            Object msg = riep.get(GraphicControllerUtils.KEY_RIEPILOGO);
            Object imp = riep.get(GraphicControllerUtils.KEY_IMPORTO_TOTALE);

            if (lblRiepilogo != null) lblRiepilogo.setText(msg != null ? String.valueOf(msg) : "");
            lastImporto = (imp instanceof Number n) ? n.floatValue() : 0f;
            if (lblImporto != null) lblImporto.setText(String.format("%.2f EUR", lastImporto));
        }
    }

    private void handleEsitoPagamento(Object rawPay) {
        if (rawPay instanceof Map<?, ?> pay) {
            Object success = pay.get(GraphicControllerUtils.KEY_SUCCESSO);
            Object stato = pay.get(GraphicControllerUtils.KEY_STATO);
            Object msg = pay.get(GraphicControllerUtils.KEY_MESSAGGIO);

            String esito = PrenotazioneViewUtils.formatEsitoPagamento(success, stato, msg);
            if (lblEsitoPagamento != null) lblEsitoPagamento.setText(esito);
        }
    }

    private void show(VBox box, boolean v) {
        if (box != null) {
            box.setVisible(v);
            box.setManaged(v);
        }
    }

    private void clearLocalError() {
        if (lblError != null) lblError.setText("");
        if (lblSuccess != null) lblSuccess.setText("");
    }

    private Integer resolveCampoId() {
        if (lastCampoId > 0) return lastCampoId;
        return parsePositiveInt(getSafeText(txtIdCampo));
    }

    private String getSafeText(TextField tf) {
        return (tf != null && tf.getText() != null) ? tf.getText().trim() : "";
    }

    private boolean isComboOraEmpty() {
        return comboOra == null || comboOra.getValue() == null || comboOra.getValue().trim().isEmpty();
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int v = Integer.parseInt(raw);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIdFromHashString(String s) {
        if (s == null || !s.contains("#")) return null;
        try {
            String sub = s.split("#")[1].split(" ")[0].replaceAll("[^0-9]", "");
            return Integer.parseInt(sub);
        } catch (Exception e) {
            return null;
        }
    }

    private void resetFields() {
        if (txtIdCampo != null) txtIdCampo.setText("");
        if (txtDurata != null) txtDurata.setText("");
        if (txtCredenziale != null) txtCredenziale.setText("");
        if (datePicker != null) datePicker.setValue(LocalDate.now());
        lastCampoId = 0;
        lastImporto = 0f;
    }
}