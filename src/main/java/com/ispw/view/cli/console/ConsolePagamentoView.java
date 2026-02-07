package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsolePagamentoView {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: console view per flusso pagamento.
    // A2) IO: input/output su standard input/output.

    private final Scanner in = new Scanner(System.in);

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) renderPaymentForm: stampa riepilogo pagamento.
    // L2) readMetodoPagamento/readCredenzialiPagamento: input utente.
    // L3) showPaymentOutcome/showError: output messaggi.
    public void renderPaymentForm(float importo, String metodo) {
        System.out.println(String.format("%n=== PAGAMENTO ===%nImporto: %.2f â‚¬%nMetodo suggerito: %s", importo, metodo));
    }
    public String readMetodoPagamento() {
        System.out.print("Metodo (SATISPAY|PAYPAL|BONIFICO): ");
        return in.nextLine().trim();
    }
    public String readCredenzialiPagamento() {
        System.out.print("Credenziale/Token/Email: ");
        return in.nextLine().trim();
    }
    public void showPaymentOutcome(String esito) { System.out.println(esito); }
    public void showError(String msg) { System.err.println("[ERRORE] " + msg); }
} 
