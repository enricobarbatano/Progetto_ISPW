# Struttura del progetto (classi e relazioni)

Questo documento descrive l'architettura del progetto ISPW, le responsabilita' delle classi principali e le relazioni tra i moduli. Serve come base per il documento di progettazione.

## Visione d'insieme a livelli

- Bootstrap e configurazione: avvio dell'app, scelta persistency e frontend.
- Controller grafici (CLI/GUI): coordinano UI e logica, navigazione e passaggio dati.
- Controller logici: implementano i casi d'uso e delegano ai DAO.
- DAO e persistenza: incapsulano l'accesso a DBMS, file system o memoria.
- Model ed Entity: stato di dominio e regole/enum.
- Bean: oggetti di scambio tra UI, controller e logica.
- View (CLI/GUI): rendering e raccolta input.
- Test: verifica dei controller logici e DAO.

Dipendenze principali (alto livello):
App -> Bootstrap -> DAOFactory + FrontendControllerFactory -> Controller grafici -> Controller logici -> DAO -> Entity

## Entry point e bootstrap

### App
- com.ispw.App
  - Entry point minimale: delega a AppBootstrapper.

### Bootstrap
- com.ispw.bootstrap.AppBootstrapper
  - Avvia configurazione, inizializza persistenza e frontend, lancia la UI.
  - Relazioni: usa AppConfigurator, AppConfig, DAOFactory, FrontendControllerFactory.
- com.ispw.bootstrap.AppConfigurator
  - Interazione CLI per scegliere persistenza e frontend.
- com.ispw.bootstrap.AppConfig
  - Record con scelte di configurazione (persistency e frontend).
- com.ispw.bootstrap.ConsoleMenu
  - Utility per menu CLI durante la configurazione.

Motivazione: isolare la fase di bootstrap dal resto dell'app e permettere configurazioni diverse senza toccare i controller.

## Controller grafici (graphic)

### Interfacce di navigazione e base
- com.ispw.controller.graphic.NavigableController
  - Contratto base per view/controller navigabili.
- com.ispw.controller.graphic.GraphicControllerNavigation
  - Router di navigazione, astratto per CLI/GUI.
- com.ispw.controller.graphic.GraphicControllerUtils, GraphicControllerLogUtils, GraphicControllerPrenotazioneUtils
  - Costanti e helper di formattazione.

### Interfacce dei controller grafici
- GraphicLoginController, GraphicControllerAccount, GraphicControllerRegistrazione, GraphicControllerPrenotazione,
  GraphicControllerDisdetta, GraphicControllerRegole, GraphicControllerPenalita, GraphicControllerLog
  - Contratti UI-agnostici per ogni caso d'uso.

### Abstract controllers
- com.ispw.controller.graphic.abstracts.*
  - Centralizzano la logica comune tra CLI e GUI e delegano ai logic controller.
  - Riduzione della duplicazione tra implementazioni concrete.

### Implementazioni CLI/GUI
- com.ispw.controller.graphic.cli.*
- com.ispw.controller.graphic.gui.*
  - Adattano i controller astratti alla UI specifica.

Motivazione: separare la UI dalla logica e garantire DIP. Le implementazioni concrete dipendono solo da interfacce e da un navigator.

## Controller logici

### Casi d'uso principali
- com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso
- LogicControllerRegistrazione
- LogicControllerGestioneAccount
- LogicControllerPrenotazioneCampo
- LogicControllerDisdettaPrenotazione
- LogicControllerConfiguraRegole
- LogicControllerApplicaPenalita
- LogicControllerGestionePagamento
- LogicControllerGestioneFattura
- LogicControllerGestioneNotifica
- LogicControllerGestioneManutenzione
- LogicControllerGestoreDisponibilita

Relazioni:
- Ogni logic controller usa uno o piu' DAO (tramite DAOFactory) e lavora con entity/bean.
- Alcune interfacce in com.ispw.controller.logic.interfaces.* formalizzano responsabilita' specifiche (pagamento, notifica, manutenzione, disponibilita', fattura).

Motivazione: separare la logica di business dalla UI e dalla persistenza.

## DAO e persistenza

### Interfacce DAO
- com.ispw.dao.interfaces.*
  - CampoDAO, PrenotazioneDAO, PagamentoDAO, PenalitaDAO, LogDAO, GeneralUserDAO, GestoreDAO, UtenteFinaleDAO,
    RegolePenalitaDAO, RegoleTempisticheDAO, FatturaDAO.

### Factory
- com.ispw.dao.factory.DAOFactory
  - Seleziona il provider (IN_MEMORY, FILE_SYSTEM, DBMS) e restituisce implementazioni.
- MemoryDAOFactory, FileSystemDAOFactory, DbmsDAOFactory
  - Implementazioni concrete della factory per ciascun provider.

### Implementazioni
- InMemory: com.ispw.dao.impl.memory.*
  - InMemoryDAO base + concrete DAO.
- FileSystem: com.ispw.dao.impl.filesystem.*
  - FileSystemDAO base + concrete DAO; serializzazione su file.
- DBMS: com.ispw.dao.impl.dbms.*
  - DbmsDAO base + concrete DAO; accesso a DB tramite DbmsConnectionFactory.

Relazioni:
- I logic controller chiedono DAO tramite DAOFactory, non conoscono implementazioni concrete.
- Le entity sono il modello persistito.

Motivazione: supportare facilmente piu' forme di persistenza senza cambiare la logica.

## Model ed Entity

### Entity (dominio)
- com.ispw.model.entity.*
  - GeneralUser, UtenteFinale, Gestore
  - Campo, Prenotazione, Pagamento, Penalita, Fattura
  - RegolePenalita, RegoleTempistiche
  - SystemLog

### Enum di dominio
- com.ispw.model.enums.*
  - Ruolo, Permesso, StatoAccount, StatoPagamento, StatoPenalita, StatoPrenotazione
  - MetodoPagamento, TipoOperazione, PersistencyProvider, FrontendProvider, AppMode

Relazioni:
- Le entity sono usate dai DAO e mappate ai bean per la UI.

## Bean (DTO)

- com.ispw.bean.*
  - Bean di input/output dei casi d'uso: DatiLoginBean, DatiRegistrazioneBean, DatiAccountBean,
    DatiInputPrenotazioneBean, DatiDisponibilitaBean, DatiPagamentoBean, DatiPenalitaBean, DatiFatturaBean,
    ParametriVerificaBean, CampiBean, RegolaCampoBean, TempisticheBean, PenalitaBean, RiepilogoPrenotazioneBean,
    EsitoOperazioneBean, EsitoDisdettaBean, LogsBean, LogEntryBean, StatoPagamentoBean, SessioneUtenteBean,
    UtenteBean, UtentiBean, UtenteSelezioneBean.
  - BaseAnagraficaBean, BasePrenotazioneBean, BaseSlotBean per fattorizzare campi comuni.

Motivazione: disaccoppiare UI e logica dalle entity persistite e semplificare la serializzazione di dati.

## View

### Base e interfacce
- com.ispw.view.interfaces.*
  - GenericView (contratto base), ViewLogin, ViewRegistrazione, ViewGestionePrenotazione, ViewDisdettaPrenotazione,
    ViewGestioneAccount, ViewGestioneRegole, ViewGestionePenalita, ViewLog, ViewHomeProfilo.
- com.ispw.view.common.GenericViewBase
  - Gestisce sessione, parametri e default lifecycle.
- com.ispw.view.cli.GenericViewCLI / com.ispw.view.gui.GenericViewGUI
  - Base specifica per CLI/GUI.

### CLI
- com.ispw.view.cli.*
  - CLI*View: schermate principali.
  - console.*: componenti di input specifici per flussi complessi (login, prenotazione, disdetta).
  - CliViewUtils: helper di output e messaggi.

### GUI
- com.ispw.view.gui.*
  - GUI*View: schermate JavaFX.
  - GuiViewUtils: helper di componenti e layout.
  - GuiLauncher: avvio dell'app JavaFX.

### Shared helpers
- com.ispw.view.shared.*
  - PrenotazioneViewUtils, RegistrazioneViewUtils, LogViewUtils.

Relazioni:
- Le view dipendono dai controller grafici (CLI/GUI) e dal navigator.
- I controller grafici usano bean e mappe per passare dati alle view.

Motivazione: isolare la UI e garantire un flusso uniforme tra CLI e GUI.

## Factory frontend

- com.ispw.controller.factory.FrontendControllerFactory
  - Factory astratta per creare controller grafici e view.
- CLIFrontendControllerFactory, GUIFrontendControllerFactory
  - Instanziano controller e view specifiche e registrano le route nel navigator.

Motivazione: selezionare a runtime il frontend (CLI/GUI) senza cambiare la logica.

## Test

- com.ispw.BaseDAOTest
  - Base per test DAO.
- com.ispw.DbmsTestHelper
  - Helper per inizializzazione DBMS nei test.
- com.ispw.controller.logic.ctrl.*
  - TestControllerPrenotazioneCampo
  - TestControllerGestioneAccount
  - TestControllerApplicaPenalita

Motivazione: validare la logica di business e il comportamento dei DAO in isolamento.

## Relazioni principali (testo sintetico)

- App -> AppBootstrapper -> AppConfigurator/AppConfig
- AppBootstrapper -> DAOFactory + FrontendControllerFactory
- FrontendControllerFactory -> controller grafici + view (CLI/GUI) + navigator
- Controller grafici -> logic controller -> DAO (via DAOFactory)
- DAO -> entity
- View <-> controller grafici (scambio via bean e Map)

## Nota su DIP e responsabilita'

- DIP: le dipendenze passano per interfacce (controller grafici e DAO).
- SRP: i controller grafici orchestrano la UI, i logic controller implementano casi d'uso, i DAO gestiscono persistenza.
- Le entity rappresentano il dominio e i bean rappresentano i dati per la UI.
