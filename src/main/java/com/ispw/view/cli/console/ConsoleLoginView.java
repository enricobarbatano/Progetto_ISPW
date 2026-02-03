// src/main/java/com/ispw/view/cli/ConsoleLoginView.java
package com.ispw.view.cli.console;

import java.util.Scanner;

public class ConsoleLoginView {
    private final Scanner in = new Scanner(System.in);
    public void render() { System.out.println("\n=== LOGIN ==="); }
    public String readEmail() { System.out.print("Email: "); return in.nextLine(); }
    public String readPassword() { System.out.print("Password: "); return in.nextLine(); }
    public void showError(String msg) { System.out.println("[ERRORE] " + msg); }
    public void showWelcome(String name) { System.out.println("Benvenuto, " + name + "!"); }
}