package com.ispw.controller.graphic.interfaces;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface GraphicControllerRegole extends NavigableController {

    void richiediListaCampi();

    void selezionaCampo(int idCampo);

    void aggiornaStatoCampo(int idCampo, boolean attivo, boolean flagManutenzione);

    void aggiornaTempistiche(
            int preavvisoMinimoMinuti,
            int durataSlotMinuti,
            LocalTime oraApertura,
            LocalTime oraChiusura
    );

    void aggiornaPenalita(
            int preavvisoMinimoMinuti,
            BigDecimal valorePenalita
    );

    void tornaAllaHome();
}