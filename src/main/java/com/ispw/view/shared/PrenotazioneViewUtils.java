package com.ispw.view.shared;

public final class PrenotazioneViewUtils {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: utility condivisa per view prenotazione.
    // A2) IO: parsing slot e formattazione esiti.

    private PrenotazioneViewUtils() {
    }

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) parseSlot: parsing data e orari da stringa.
    // L2) formatEsitoPagamento: stringa di riepilogo.

    public static SlotInfo parseSlot(String slot) {
        if (slot == null) return null;
        String[] parts = slot.split(" ");
        if (parts.length < 2) return null;
        String data = parts[0];
        String times = parts[1];
        int dash = times.indexOf('-');
        if (dash <= 0) return null;
        String oraInizio = times.substring(0, dash);
        String oraFine = times.substring(dash + 1);
        return new SlotInfo(data, oraInizio, oraFine);
    }

    public static String formatEsitoPagamento(Object success, Object stato, Object msg) {
        return String.format("Esito: %s - stato: %s - %s", success, stato, msg);
    }

    public record SlotInfo(String data, String oraInizio, String oraFine) { }
}
