package com.ispw.controller.logic.interfaces.pagamento;

import com.ispw.bean.DatiPagamentoBean;
import com.ispw.model.enums.StatoPagamento;

public interface GestionePagamentoPrenotazione {
    StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean datiPagamentoBean, int idPrenotazione);
}
