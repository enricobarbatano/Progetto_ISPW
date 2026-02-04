// src/main/java/com/ispw/view/cli/ConsoleLoginView.java
package com.ispw.view.cli.console;

import java.util.Scanner;
import java.util.logging.Logger;

public class ConsoleLoginView {
    private static final Logger logger = Logger.getLogger(ConsoleLoginView.class.getName());
    private final Scanner in = new Scanner(System.in);
    public void render() { logger.info("\n=== LOGIN ==="); }
    public String readEmail() { System.out.print("Email: "); return in.nextLine(); }
    public String readPassword() { System.out.print("Password: "); return in.nextLine(); }
    public void showError(String msg) { logger.severe("[ERRORE] " + msg); }
    public void showWelcome(String name) { logger.info("Benvenuto, " + name + "!"); }
}