package com.ispw.controller.graphic.cli;

import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerNavigation;

/**
 * Controller grafico CLI della navigazione.
 *
 * Al momento non aggiunge comportamento rispetto alla classe astratta,
 * ma viene mantenuto per coerenza con l'Abstract Factory del frontend.
 *
 * In questo modo la famiglia CLI ha una propria navigation concreta,
 * pronta per eventuali comportamenti specifici della CLI.
 */
public class CLIGraphicControllerNavigation extends AbstractGraphicControllerNavigation {
    // Nessun comportamento specifico CLI al momento.
}