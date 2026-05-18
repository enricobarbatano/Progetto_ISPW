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
import static org.mockito.ArgumentMatchers.intThat;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.UnitTestBase;
import com.ispw.bean.DatiFatturaBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.logic.ServiceFactory;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.model.entity.UtenteFinale;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

// Test con Mockito per verificare l'orchestrazione di LogicControllerApplicaPenalita.
// Dopo il refactoring, i service non vengono più passati come parametri:
// vengono recuperati tramite ServiceFactory.
// Per questo motivo la ServiceFactory viene mockata staticamente.
@ExtendWith(MockitoExtension.class)
class LogicControllerApplicaPenalitaMockitoTest extends UnitTestBase {

    @Mock
    private GestionePagamentoPenalita payCtrl;

    @Mock
    private GestioneFatturaPenalita fattCtrl;

    @Mock
    private GestioneNotificaPenalita notiCtrl;

    @Captor
    private ArgumentCaptor<DatiPagamentoBean> pagamentoCaptor;

    @Captor
    private ArgumentCaptor<DatiFatturaBean> fatturaCaptor;

    private LogicControllerApplicaPenalita controller;

    @BeforeEach
    void init() {
        controller = new LogicControllerApplicaPenalita();

        var userDAO = DAOFactory.getInstance().getGeneralUserDAO();

        try {
            userDAO.getClass().getMethod("clear").invoke(userDAO);
        } catch (ReflectiveOperationException ex) {
            // Alcune implementazioni DAO di test potrebbero non esporre clear().
            // In quel caso il test continua usando lo stato disponibile.
        }

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
        pay.setImporto(0f);
        pay.setMetodo("   ");

        DatiFatturaBean fatt = new DatiFatturaBean();
        fatt.setDataOperazione(null);

        when(payCtrl.richiediPagamentoPenalita(any(DatiPagamentoBean.class), anyInt()))
                .thenReturn(null);

        when(fattCtrl.generaFatturaPenalita(any(DatiFatturaBean.class), anyInt()))
                .thenReturn(null);

        /*
         * Mock statico della ServiceFactory.
         * Così, quando il controller chiama i metodi accessor interni,
         * riceve i mock definiti in questo test.
         */
        try (MockedStatic<ServiceFactory> serviceFactoryMock = Mockito.mockStatic(ServiceFactory.class)) {
            serviceFactoryMock.when(ServiceFactory::getPagamentoPenalitaService).thenReturn(payCtrl);
            serviceFactoryMock.when(ServiceFactory::getFatturaPenalitaService).thenReturn(fattCtrl);
            serviceFactoryMock.when(ServiceFactory::getNotificaPenalitaService).thenReturn(notiCtrl);

            // Act
            EsitoOperazioneBean esito = controller.applicaSanzione(dati, pay, fatt);

            // Assert esito
            assertNotNull(esito);
            assertTrue(esito.isSuccesso());

            // Verify notifica
            verify(notiCtrl, times(1)).inviaNotificaPenalita("10");

            // Verify pagamento
            verify(payCtrl, times(1))
                    .richiediPagamentoPenalita(pagamentoCaptor.capture(), intThat(id -> id > 0));

            DatiPagamentoBean paySent = pagamentoCaptor.getValue();
            assertNotNull(paySent);

            assertEquals(30.0f, paySent.getImporto(), 0.0001f);
            assertEquals("PAYPAL", paySent.getMetodo());

            // Verify fattura
            verify(fattCtrl, times(1))
                    .generaFatturaPenalita(fatturaCaptor.capture(), intThat(id -> id > 0));

            assertNotNull(fatturaCaptor.getValue());

            verifyNoMoreInteractions(notiCtrl, payCtrl, fattCtrl);
        }
    }
}