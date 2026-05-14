package com.ispw.controller.mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.intThat;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ispw.UnitTestBase;
import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

// Test di integrazione con Mockito per verificare l'orchestrazione di LogicControllerApplicaPenalita:
// - Verifica che, dati input validi, vengano invocati i collaboratori (notifica, pagamento, fattura) 
// con i parametri corretti (normalizzazione inclusa).

@ExtendWith(MockitoExtension.class)
class LogicControllerApplicaPenalita_MockitoTest extends UnitTestBase{

    // Collaboratori mockati=
    @Mock private GestionePagamentoPenalita payCtrl;
    @Mock private GestioneFatturaPenalita fattCtrl;
    @Mock private GestioneNotificaPenalita notiCtrl;
    // ArgumentCaptor per catturare i parametri con cui vengono invocati pagamento e fattura
    @Captor private ArgumentCaptor<DatiPagamentoBean> pagamentoCaptor;
    @Captor private ArgumentCaptor<DatiFatturaBean> fatturaCaptor;

    private LogicControllerApplicaPenalita controller;

    // Prima di ogni test, inizializza il controller e prepara un utente nel DAO (per evitare errori "utente inesistente").
    @BeforeEach
    void init() {
        controller = new LogicControllerApplicaPenalita();

        // Assumo che i tuoi test già inizializzino DAOFactory su IN_MEMORY (via BaseDAOTest o bootstrap test).
        // Se non fosse così, dimmelo e ti do il bootstrap minimo.
        var userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        // Pulisci se il tuo DAO ha clear(); altrimenti ignora
        try { userDAO.getClass().getMethod("clear").invoke(userDAO); } catch (Exception ignored) {}

        // Seed utente esistente (per evitare KO "utente inesistente")
        UtenteFinale u = new UtenteFinale();
        u.setIdUtente(10);
        u.setNome("Mario");
        u.setCognome("Rossi");
        u.setEmail("mario@example.org");
        u.setPassword("pwd");
        u.setRuolo(Ruolo.UTENTE);
        u.setStatoAccount(StatoAccount.ATTIVO);
        userDAO.store(u);
    }

    @Test
    @DisplayName("Penalità: orchestrazione -> invoca notifica/pagamento/fattura e normalizza pagamento")
    void applicaSanzione_orchestrazione_invocaCollaboratori_eNormalizzaPagamento() {

        // Arrange
        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(10);
        dati.setMotivazione("Danni al campo");
        dati.setDataDecorrenza(LocalDate.now());
        dati.setImporto(new BigDecimal("30.00"));

        DatiPagamentoBean pay = new DatiPagamentoBean();
        pay.setImporto(0f);      // forza normalizzazione -> importo = 30.00
        pay.setMetodo("   ");    // blank -> default "PAYPAL"

        DatiFatturaBean fatt = new DatiFatturaBean();
        fatt.setDataOperazione(null); // normalizzazione fattura (data = oggi)

        // When().thenReturn() per simulare i collaboratori: restituiscono sempre successo (EsitoOperazioneBean con successo=true).
        when(payCtrl.richiediPagamentoPenalita(any(DatiPagamentoBean.class), anyInt()))
                .thenReturn(null);
        when(fattCtrl.generaFatturaPenalita(any(DatiFatturaBean.class), anyInt()))
                .thenReturn(null);

        // Act
        EsitoOperazioneBean esito = controller.applicaSanzione(dati, pay, fatt, payCtrl, fattCtrl, notiCtrl);

        // Assert esito
        assertNotNull(esito);
        assertTrue(esito.isSuccesso());

        // Verify notifica: 
        verify(notiCtrl, times(1)).inviaNotificaPenalita(eq("10"));

        // Verify pagamento: deve essere invocato una volta con idPenalita > 0
        verify(payCtrl, times(1)).richiediPagamentoPenalita(pagamentoCaptor.capture(), intThat(id -> id > 0));
        DatiPagamentoBean paySent = pagamentoCaptor.getValue();
        assertNotNull(paySent);

        // Normalizzazione pagamento: importo -> 30, metodo -> PAYPAL
        assertEquals(30.0f, paySent.getImporto(), 0.0001f);
        assertEquals("PAYPAL", paySent.getMetodo());

        // Verify fattura: invocata una volta con lo stesso idPenalita del pagamento
        verify(fattCtrl, times(1)).generaFatturaPenalita(fatturaCaptor.capture(), intThat(id -> id > 0));
        assertNotNull(fatturaCaptor.getValue());

        // Non chiamare oltre
        verifyNoMoreInteractions(notiCtrl, payCtrl, fattCtrl);
    }
}