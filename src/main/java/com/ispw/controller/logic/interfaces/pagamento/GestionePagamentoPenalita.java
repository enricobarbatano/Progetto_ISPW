package com.ispw.controller.logic.interfaces.pagamento;

import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.StatoPagamentoBean;
public interface GestionePagamentoPenalita {
    StatoPagamentoBean richiediPagamentoPenalita(DatiPagamentoBean datiPagamentoBean, int idPenalita);
}
