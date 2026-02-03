package com.ispw.controller.logic.interfaces.pagamento;

import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.StatoPagamentoBean;
public interface GestionePagamentoPenalita {
    StatoPagamentoBean richiediPagamentoPenalità(DatiPagamentoBean datiPagamentoBean, int idPenalita);
}
