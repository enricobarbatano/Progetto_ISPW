package com.ispw.controller.logic.interfaces.pagamento;

import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.StatoPagamentoBean;

public interface GestionePagamentoPrenotazione {

    StatoPagamentoBean richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione);
}