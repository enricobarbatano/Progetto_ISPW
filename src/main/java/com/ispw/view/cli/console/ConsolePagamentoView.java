// src/main/java/com/ispw/view/cli/ConsolePagamentoView.java
package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsolePagamentoView {
    private final Scanner in = new Scanner(System.in);
    public void renderPaymentForm(float importo, String metodo) {
        System.out.println(String.format("%n=== PAGAMENTO ===%nImporto: %.2f €%nMetodo suggerito: %s", importo, metodo));
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