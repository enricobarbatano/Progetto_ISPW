package com.ispw.controller.graphic.interfaces;

import com.ispw.bean.SessioneUtenteBean;

public interface GraphicControllerPrenotazione extends NavigableController {

    // View passa dati semplici → il controller crea i Bean

    void cercaDisponibilita(int idCampo, String data, String oraInizio, int durataMin);

    void richiediListaCampi();
    void creaPrenotazione(
            int idCampo,
            String data,
            String oraInizio,
            String oraFine,
            SessioneUtenteBean sessione
    );

    void procediAlPagamento(
            String metodo,
            String credenziale,
            float importo,
            SessioneUtenteBean sessione
    );

    void tornaAllaHome();
}
