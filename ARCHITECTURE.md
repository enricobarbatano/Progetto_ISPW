# ARCHITETTURA DEL SISTEMA - CENTRO SPORTIVO (ISPW)

## 📋 INDICE
1. [Overview](#overview)
2. [Stack Tecnologico](#stack-tecnologico)
3. [Architettura Generale](#architettura-generale)
4. [Layer del Sistema](#layer-del-sistema)
5. [Pattern Architetturali](#pattern-architetturali)
6. [Casi d'Uso Principali](#casi-duso-principali)
7. [Flussi End-to-End](#flussi-end-to-end)
8. [Organizzazione dei Package](#organizzazione-dei-package)
9. [Dipendenze e DIP](#dipendenze-e-dip)
10. [Considerazioni di Progetto](#considerazioni-di-progetto)

---

## Overview

**Progetto**: Centro Sportivo - Sistema di Prenotazione Campi
**Tipo**: Applicazione Java multistrato con supporto multi-backend e multi-frontend
**Scopo**: Gestire prenotazioni di campi sportivi, pagamenti, penalità e fatturazione

### Funzionalità Principali
- **Gestione Accesso**: Login/logout, registrazione utenti
- **Prenotazione Campi**: Ricerca disponibilità, creazione prenotazioni, pagamento
- **Gestione Disdette**: Richiesta annullamento con rimborso, approvazione gestore
- **Gestione Account**: Modifica dati, cambio password, visualizzazione log
- **Configurazione Regole**: Impostazione tempistiche, penalità, manutenzione campi
- **Applicazione Penalità**: Gestore applica sanzioni agli utenti
- **Sistema di Log**: Tracciamento operazioni utenti

### Supporto Multi-Backend e Multi-Frontend
- **Backend**: DBMS (MySQL), FileSystem (JSON), In-Memory (cache)
- **Frontend**: GUI (JavaFX/FXML), CLI (console)
- **Selezione**: A bootstrap time, non modificabile a runtime

---

## Stack Tecnologico

### Linguaggio e Framework
- **Linguaggio**: Java 11+
- **UI Desktop**: JavaFX + FXML
- **Build Tool**: Maven
- **Database**: MySQL (JDBC) / JSON (FileSystem) / In-Memory
- **Logging**: java.util.logging

### Dipendenze Principali
- JUnit 5 (testing)
- Mockito (mocking)
- JAX-B (JAXB per XML/JSON, opzionale)
- Jackson o GSON (JSON serialization)

### Struttura di Build
```
pom.xml
├─ Maven dependencies
├─ Plugins (compiler, shade, assembly)
└─ Properties (java version, encoding)

src/
├─ main/java/com/ispw/...
└─ test/java/com/...
```

---

## Architettura Generale

### Diagramma Complessivo (7 Layer + Bootstrap)

```
┌─────────────────────────────────────────────────────┐
│ LAYER 0: BOOTSTRAP                                  │
│ AppBootstrapper -> seleziona backend e frontend     │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ LAYER 1: VIEW (Presentation)                        │
│ ├─ GUI: JavaFX (FXML + FXMLController)             │
│ └─ CLI: Console (custom console views)             │
└────────────────────┬────────────────────────────────┘
                     ↓ NavigableController
┌─────────────────────────────────────────────────────┐
│ LAYER 2: GRAPHIC CONTROLLER (UI Logic)              │
│ ├─ Abstract: AbstractGraphicLoginController, ...    │
│ ├─ GUI: GUIGraphicLoginController, ...              │
│ └─ CLI: CLIGraphicLoginController, ...              │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ LAYER 3: NAVIGATOR (Routing)                        │
│ ├─ GraphicControllerNavigation (interfaccia)        │
│ ├─ AbstractGraphicControllerNavigation              │
│ ├─ GUIGraphicControllerNavigation                   │
│ └─ CLIGraphicControllerNavigation                   │
└────────────────────┬────────────────────────────────┘
                     ↓ LogicControllerFactory
┌─────────────────────────────────────────────────────┐
│ LAYER 4: LOGIC CONTROLLER (Business Logic)          │
│ ├─ Principale: LogicControllerGestioneAccesso,      │
│ │  LogicControllerPrenotazioneCampo, ...             │
│ ├─ Factory: LogicControllerFactory                  │
│ └─ **Stateless** - DAO e Service via factory        │
└────────────────────┬────────────────────────────────┘
                     ↓ ServiceFactory
┌─────────────────────────────────────────────────────┐
│ LAYER 5: SERVICE CONTROLLER (Business Services)     │
│ ├─ Pagamento, Fattura, Notifica,                    │
│ │  Disponibilità, Manutenzione                      │
│ ├─ Factory: ServiceFactory                          │
│ └─ **Stateless** - DAO via DAOFactory               │
└────────────────────┬────────────────────────────────┘
                     ↓ DAOFactory
┌─────────────────────────────────────────────────────┐
│ LAYER 6: DAO (Persistence Abstraction)              │
│ ├─ Interfacce: GeneralUserDAO, PrenotazioneDAO, ... │
│ ├─ Factory: DAOFactory (singleton + switch backend) │
│ ├─ DbmsDAOFactory -> DBMS implementation            │
│ ├─ FileSystemDAOFactory -> JSON files               │
│ └─ MemoryDAOFactory -> In-memory (seed load)        │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ LAYER 7: MODEL/ENTITY (Domain)                      │
│ ├─ Entity: GeneralUser, Prenotazione, Pagamento, ... │
│ ├─ Enum: Ruolo, StatoAccount, StatoPrenotazione, ...│
│ └─ Serializable per persistenza                     │
└────────────────────┬────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│ LAYER 8: BEAN/DTO (Data Transfer)                   │
│ ├─ DatiLoginBean, SessioneUtenteBean, ...            │
│ ├─ Separano Entity da layer grafico (DIP)           │
│ └─ Usati nella comunicazione View <-> Controller    │
└─────────────────────────────────────────────────────┘
```

### Principi Architetturali
- **MVC-like esteso**: View, Controller grafico, Controller logico, DAO, Entity
- **Dependency Inversion**: Dipendenze da astrazioni (interfacce), non da impl concrete
- **Stateless Controllers**: I controller logici non mantengono stato
- **Factory Everywhere**: FrontendControllerFactory, LogicControllerFactory, ServiceFactory, DAOFactory
- **Separation of Concerns**: Ogni layer ha una responsabilità ben definita
- **Bean/DTO Pattern**: Entity non esposte al layer grafico

---

## Layer del Sistema

### LAYER 0: BOOTSTRAP
**Responsabilità**: Inizializzazione applicazione, selezione backend e frontend

**File Principali**:
- `AppBootstrapper.main(String[] args)`: Entry point
- `AppConfigurator`: Chiede configurazione a utente
- `AppConfig`: Configurazione (PersistencyProvider + FrontendProvider)

**Flusso**:
1. Chiede backend (DBMS, FileSystem, In-Memory)
2. Chiede frontend (GUI, CLI)
3. Inizializza DAOFactory con provider scelto
4. Inizializza FrontendControllerFactory con provider scelto
5. Avvia applicazione

---

### LAYER 1: VIEW (Presentazione)

**Responsabilità**: Visualizzazione dati, raccolta input utente

#### Interfacce
- `GenericView`: Contratto base con `onShow(Map<String, Object> params)`, metodi di lettura error/success/session

#### Implementazioni GUI (JavaFX)
- `GUILoginView, GUIHomeView, GUIPrenotazioneView, ...`
- Estendono `GenericViewGUI` che estende `GenericViewBase`
- Caricano FXML via `FXMLLoader`
- Delegano a **LoginFXMLController, PrenotazioneFXMLController, ...** (controller FXML)
- Usano `GuiLauncher` per cambiare stage/root

#### Implementazioni CLI
- `CLILoginView, CLIHomeView, CLIPrenotazioneView, ...`
- Estendono `GenericViewCLI` che estende `GenericViewBase`
- Usano componenti console: `ConsoleLoginView, ConsolePagamentoView, ...`
- Input via `console.readChoice()`, `console.readEmail()`, etc.

#### Regole View
- ✅ Solo logica di presentazione
- ✅ No logica applicativa
- ✅ No dipendenza da Entity
- ✅ Dipendono da GraphicController (tramite NavigableController)
- ✅ Comunicano via Bean/DTO

---

### LAYER 2: GRAPHIC CONTROLLER (Controllo Interfaccia)

**Responsabilità**: Validazione input da view, delegazione a logic controller, navigazione

#### Interfacce
```
NavigableController (base)
├─ getRouteName(): String
├─ onShow(Map<String, Object> params): void
└─ onHide(): void

GraphicLoginController extends NavigableController
├─ effettuaLogin(DatiLoginBean)
├─ logout()
├─ vaiARegistrazione()
└─ vaiAHome()

[e altre interfacce specifiche per ogni caso d'uso]
```

#### Classi Astratte
- `AbstractGraphicLoginController`: Logica comune login per GUI/CLI
- `AbstractGraphicControllerPrenotazione`: Logica comune prenotazione
- `AbstractGraphicControllerNavigation`: Logica comune navigazione
- [altre astratte per ogni caso d'uso]

#### Implementazioni Concrete
- **GUI**: `GUIGraphicLoginController, GUIGraphicControllerPrenotazione, ...`
- **CLI**: `CLIGraphicLoginController, CLIGraphicControllerPrenotazione, ...`

#### Flusso Tipico
```
View.onShow() -> input user -> GraphicController.metodo(bean)
  ├─ Valida bean
  ├─ Ottiene LogicController via LogicControllerFactory
  ├─ Chiama logicController.metodo(bean)
  ├─ Riceve risultato (EsitoBean o RiepilogoBean)
  └─ Naviga a nuova route o mostra errore
```

#### Regole Graphic Controller
- ✅ No logica di dominio
- ✅ Validazioni di base (null check, formato)
- ✅ Delegazione a LogicController
- ✅ Dipendono da LogicControllerFactory (astratto)
- ✅ Dipendono da GraphicControllerNavigation (interfaccia)
- ⚠️ **PROBLEMA**: Alcune impl istanziano direttamente LogicController con `new` anziché factory

---

### LAYER 3: NAVIGATOR (Routing)

**Responsabilità**: Routing tra schermate, gestione history, disaccoppiamento controller-view

#### Interfaccia
```java
public interface GraphicControllerNavigation {
    void goTo(String route, Map<String, Object> params);
    void back();
    void exit();
}
```

#### Implementazione Astratta
- `AbstractGraphicControllerNavigation`: Logica comune routing
  - Map<String, NavigableController> routes
  - Deque<String> history (stack per back)
  - String currentRoute

#### Implementazioni Concrete
- `GUIGraphicControllerNavigation`: Routing GUI
- `CLIGraphicControllerNavigation`: Routing CLI

#### Flusso di Navigazione
1. GraphicController chiama `navigator.goTo(ROUTE_HOME, params)`
2. Navigator recupera NavigableController per quella route
3. Aggiorna history (push currentRoute)
4. Chiama `controller.onShow(params)` sulla view
5. View visualizza dati e attende interazione

#### Route Supportate (in GraphicControllerUtils)
```
ROUTE_LOGIN, ROUTE_HOME, ROUTE_REGISTRAZIONE, ROUTE_ACCOUNT,
ROUTE_PRENOTAZIONE, ROUTE_DISDETTA, ROUTE_REGOLE, ROUTE_PENALITA,
ROUTE_LOGS, ROUTE_RICHIESTE_DISDETTA
```

#### Vantaggi Navigator Pattern
- ✅ Disaccoppiamento: GraphicController non conosce View concrete
- ✅ Routing centralizzato
- ✅ History support (back)
- ✅ DIP: Navigator è interfaccia

---

### LAYER 4: LOGIC CONTROLLER (Logica Applicativa)

**Responsabilità**: Logica di dominio, orchestrazione DAO e Service, validazioni complesse

#### Interfacce Principali (Contratti Use Case)
```
CtrlAccesso
├─ SessioneUtenteBean verificaCredenziali(DatiLoginBean)
└─ void saveLog(SessioneUtenteBean)

CtrlPrenotazione
├─ CampiBean listaCampi()
├─ List<DatiDisponibilitaBean> trovaSlotDisponibili(ParametriVerificaBean)
├─ RiepilogoPrenotazioneBean nuovaPrenotazione(DatiInputPrenotazioneBean, SessioneUtenteBean)
└─ StatoPagamentoBean completaPrenotazione(DatiPagamentoBean, SessioneUtenteBean)

CtrlDisdetta, CtrlGestioneRegole, CtrlApplicaPenalita, CtrlGestioneAccount,
CtrlRegistrazione [similmente strutturate]
```

#### Implementazioni
- `LogicControllerGestioneAccesso implements CtrlAccesso`
- `LogicControllerPrenotazioneCampo implements CtrlPrenotazione`
- `LogicControllerDisdettaPrenotazione implements CtrlDisdetta`
- `LogicControllerConfiguraRegole implements CtrlGestioneRegole`
- `LogicControllerApplicaPenalita implements CtrlApplicaPenalita`
- `LogicControllerGestioneAccount implements CtrlGestioneAccount`
- `LogicControllerRegistrazione implements CtrlRegistrazione`

#### Caratteristiche
- **Stateless**: Nessun campo di istanza (stato mantenuto in Entity)
- **DAO via Factory**: `DAOFactory.getInstance().get*DAO()`
- **Service via Factory**: `ServiceFactory.get*Service()`
- **Bean per IO**: Input DatiXBean, output EsitoOperazioneBean o RiepilogoBean
- **DIP**: Dipendono da interfacce DAO/Service, non da impl concrete
- **Early Return**: Validazioni rapide con return
- **Logger On-Demand**: Evita campi statici, usa `Logger.getLogger(getClass().getName())`
- **Messaggi Centralizzati**: Costanti String per errori

#### LogicControllerFactory
```java
public final class LogicControllerFactory {
    public static CtrlAccesso getAccessoController()
        -> return new LogicControllerGestioneAccesso();
    public static CtrlPrenotazione getPrenotazioneController()
        -> return new LogicControllerPrenotazioneCampo();
    public static CtrlDisdetta getDisdettaController()
        -> return new LogicControllerDisdettaPrenotazione();
    [... altri metodi ...]
}
```

#### Regole Logic Controller
- ✅ No SQL diretto
- ✅ No import di impl concrete DAO/Service
- ✅ No campo di sessione
- ✅ Testabili (stateless)
- ✓ Conversione entity->bean manuale (potrebbe usare mapper)
- ⚠️ LogicControllerPrenotazioneCampo ha troppe responsabilità (coordina 4 service)

---

### LAYER 5: SERVICE CONTROLLER (Servizi Secondari)

**Responsabilità**: Servizi specifici usati dai logic controller principali

#### Interfacce (Role Interfaces - ISP)

**Pagamento**:
- `GestionePagamentoPrenotazione.richiediPagamentoPrenotazione(DatiPagamentoBean, int idPrenotazione)`
- `GestionePagamentoRimborso.eseguiRimborso(int idPrenotazione, float importo)`
- `GestionePagamentoPenalita.richiediPagamentoPenalita(DatiPagamentoBean, int idPenalita)`

**Fattura**:
- `GestioneFatturaPrenotazione.creaScontrino(DatiFatturaBean, int idPrenotazione)`
- `GestioneFatturaPenalita.creaFatturaPenalita(DatiFatturaBean, int idPenalita)`
- `GestioneFatturaRimborso.creaRimborsoFattura(int idPrenotazione, float importo)`

**Notifica**:
- `GestioneNotificaPrenotazione.inviaConfermaPrenotazione(UtenteBean, String dettaglio)`
- `GestioneNotificaDisdetta.inviaConfermaCancellazione(UtenteBean, String dettaglio)`
- `GestioneNotificaRegistrazione.inviaConfermaRegistrazione(UtenteBean)`
- [altre per account, penalità, regole]

**Disponibilità**:
- `GestioneDisponibilitaPrenotazione.trovaSlotDisponibili(ParametriVerificaBean)`
- `GestioneDisponibilitaPrenotazione.occupaSlot(int idCampo, LocalDate, LocalTime, Duration)`
- `GestioneDisponibilitaDisdetta.liberaSlot(...)`

**Manutenzione**:
- `GestioneManutenzioneConfiguraRegole.marcaCampoManutenzione(int idCampo)`
- `GestioneManutenzioneConfiguraRegole.marcaCampoAttivo(int idCampo)`

#### Implementazioni
- Una sola impl per tipo: `LogicControllerGestionePagamento` implementa le 3 interfacce pagamento
- `LogicControllerGestioneFattura` implementa le 3 interfacce fattura
- `LogicControllerGestioneNotifica` implementa le 6 interfacce notifica
- `LogicControllerGestoreDisponibilita` implementa le 3 interfacce disponibilità
- `LogicControllerGestioneManutenzione` implementa l'interfaccia manutenzione

#### Caratteristiche
- **Stateless**: No stato di istanza
- **DAO via DAOFactory**: Accesso persistenza tramite factory
- **DIP**: Dipendono da interfacce, non da impl concrete
- **ISP**: Interfacce piccole e mirate per ruoli specifici
- **Composizione**: Logic principal li usa via interfaccia

#### ServiceFactory
```java
public class ServiceFactory {
    public static GestionePagamentoPrenotazione getPagamentoPrenotazioneService()
        -> return new LogicControllerGestionePagamento();
    public static GestioneFatturaPrenotazione getFatturaPrenotazioneService()
        -> return new LogicControllerGestioneFattura();
    [... altri metodi ...]
}
```

---

### LAYER 6: DAO (Persistenza)

**Responsabilità**: Accesso dati, astrazione persistenza

#### Interfacce DAO
```
DAO (marker interface)

GeneralUserDAO
├─ GeneralUser findByEmail(String email)
├─ GeneralUser findById(int id)
└─ void store(GeneralUser user)

[Simili per CampoDAO, PrenotazioneDAO, PagamentoDAO, FatturaDAO, ...]
```

#### DAOFactory (Abstract Factory + Singleton)
```java
public abstract class DAOFactory {
    public static synchronized void initialize(PersistencyProvider p, Path root)
    public static synchronized DAOFactory getInstance()
    
    abstract GeneralUserDAO getGeneralUserDAO();
    abstract PrenotazioneDAO getPrenotazioneDAO();
    [... altri getter ...]
}
```

#### Implementazioni Concrete
- **DbmsDAOFactory**: SQL via JDBC
  - `DbmsGeneralUserDAO, DbmsPrenotazioneDAO, ...`
  - Usa DbmsConnectionFactory per pool connessioni
  
- **FileSystemDAOFactory**: JSON su filesystem
  - `FileSystemGeneralUserDAO, FileSystemPrenotazioneDAO, ...`
  - Legge/scrive file JSON per ogni entità
  - Usa JsonListFileStore per gestione JSON
  
- **MemoryDAOFactory**: Cache in-memory
  - `InMemoryGeneralUserDAO, InMemoryPrenotazioneDAO, ...`
  - Carica seed da filesystem all'init
  - Not persistente tra sessioni

#### Lazy Initialization
```java
// In ogni concrete factory:
@Override
public synchronized GeneralUserDAO getGeneralUserDAO() {
    if (generalUserDAO == null) {
        generalUserDAO = new DbmsGeneralUserDAO(connectionFactory);
    }
    return generalUserDAO;
}
```

#### Regole DAO
- ✅ No SQL nei controller (solo in DAO)
- ✅ Interfacce generiche (find, store, delete)
- ✅ Entity mappate da/verso database
- ✅ DIP: Logic controller dipende da interfaccia, non da impl
- ⚠️ Conversione entity da/verso DB manuale (no ORM)
- ⚠️ FileSystem legge/scrive intero JSON per ogni modifica (inefficiente)
- ⚠️ In-Memory non persistente tra sessioni

---

### LAYER 7: MODEL/ENTITY

**Responsabilità**: Rappresentazione delle entità di dominio

#### Entity Principali
```
GeneralUser (abstract base)
├─ int idUtente
├─ String nome, cognome, email, password
├─ StatoAccount statoAccount
├─ Ruolo ruolo
└─ [getters/setters]

Gestore extends GeneralUser
UtenteFinale extends GeneralUser

Campo
├─ int idCampo
├─ String nomeCampo
├─ boolean flagManutenzione
├─ LocalTime oraApertura, oraChiusura
└─ [getters/setters]

Prenotazione
├─ int idPrenotazione
├─ int idCampo (FK)
├─ int idUtente (FK)
├─ LocalDate dataPrenotazione
├─ StatoPrenotazione stato
├─ float importoTotale
└─ [getters/setters]

Pagamento
├─ int idPagamento
├─ BigDecimal importoFinale
├─ MetodoPagamento metodo
├─ StatoPagamento stato
└─ [getters/setters]

Fattura, Penalita, RegolePenalita, RegoleTempistiche,
RichiestaDisdettaRimborso, SystemLog [similmente strutturate]
```

#### Enum
```
Ruolo: GESTORE, UTENTE_FINALE
StatoAccount: ATTIVO, SOSPESO, CANCELLATO
StatoPrenotazione: DA_PAGARE, CONFERMATA, ANNULLATA
StatoPagamento: PAGATO, FALLITO, RIFIUTATO, RIMBORSATO
StatoPenalita: APPLICATA, PAGATA, SCADUTA
MetodoPagamento: CARTA, CONTANTI, BONIFICO
StatoRichiestaDisdetta: PENDING, APPROVATA, RIFIUTATA
TipoOperazione: LOGIN, LOGOUT, PRENOTAZIONE, DISDETTA, PENALITA, ...
```

#### Caratteristiche Entity
- ✅ POJO (Plain Old Java Object)
- ✅ Serializable per persistenza
- ✅ Relazioni rappresentate come FK (int)
- ✅ No logica di dominio complessa (solo dati + enum)
- ✓ No validazione nei setter (fiducia nel layer superiore)

---

### LAYER 8: BEAN/DTO

**Responsabilità**: Trasferimento dati tra layer grafico e logico

#### Naming Convention
- `Dati<Cosa>Bean`: Input (es. DatiLoginBean, DatiPrenotazioneBean)
- `<Cosa>Bean`: Generico (es. UtenteBean, CampoBean)
- `Esito<Cosa>Bean`: Output (es. EsitoOperazioneBean, EsitoDisdettaBean)
- `<Cosa>Riepilogo`: Riepilogo (es. RiepilogoPrenotazioneBean)

#### Bean Principali
```
// Input
DatiLoginBean (email, password)
DatiRegistrazioneBean (nome, cognome, email, password, ruolo)
DatiAccountBean (nome, cognome, email, attivo)
DatiInputPrenotazioneBean (idCampo, data, ora, durata)
DatiPagamentoBean (importo, metodo)
DatiPenalitaBean (idUtente, importo, motivazione, metodo)
ParametriVerificaBean (data, ora, durata)
RegolaCampoBean (idCampo, flagManutenzione)
TempisticheBean (preavviso, durataSlot, oraApertura, oraChiusura)
PenalitaBean (penalePercentuale, giorniTermineDisdetta)

// Output
SessioneUtenteBean (idSessione, UtenteBean, timeStamp)
UtenteBean (nome, cognome, email, ruolo)
CampiBean (List<CampoBean>)
CampoBean (idCampo, nomeCampo, descrizione, flagManutenzione)
RiepilogoPrenotazioneBean (idPrenotazione, idCampo, data, importoTotale, stato)
DatiDisponibilitaBean (idCampo, nomeCampo, oraInizio, oraFine, disponibile)
StatoPagamentoBean (successo, stato, idTransazione, messaggio)
EsitoOperazioneBean (successo, messaggio)
EsitoDisdettaBean (successo, messaggio, importoRimborso)
LogsBean (List<LogEntryBean>)
LogEntryBean (timestamp, descrizione, idUtenteCoinvolto, tipoOperazione)
RichiestaDisdettaBean (idRichiesta, idPrenotazione, stato, importoRichiesto, ...)
UtentiBean (List<UtenteSelezioneBean>)
UtenteSelezioneBean (idUtente, nome, cognome, email, ruolo)
```

#### Regole Bean
- ✅ Solo dati, no logica
- ✅ Separano Entity da layer grafico (DIP)
- ✅ Usati nella comunicazione View <-> Controller
- ✓ Nessuna validazione intrinseca
- ⚠️ Molti bean (potrebbe avere mapper utility per conversioni entity<->bean)

---

## Pattern Architetturali

### 1. MVC-like Esteso
- **Model**: Entity + DAO
- **View**: GUI/CLI
- **Controller**: GraphicController + LogicController (separati)
- **Esteso**: Aggiunge Navigator, Service layer, Factory everywhere

### 2. Abstract Factory Pattern
**Utilizzi**:
- `FrontendControllerFactory`: Crea controller/view GUI o CLI a runtime
- `LogicControllerFactory`: Crea logic controller principal
- `ServiceFactory`: Crea service controller
- `DAOFactory`: Crea DAO (DBMS, FileSystem, In-Memory)

```
    FrontendControllerFactory (abstract)
         ↓
    ├─ GUIFrontendControllerFactory
    └─ CLIFrontendControllerFactory
```

### 3. Singleton Pattern
- `DAOFactory.getInstance()`: Singleton factory
- `FrontendControllerFactory.getInstance()`: Singleton factory
- `DbmsConnectionFactory.getInstance()`: Singleton pool connessioni

### 4. Strategy Pattern
- Backend persistenza (Strategy): DBMS, FileSystem, In-Memory
- Frontend (Strategy): GUI, CLI

### 5. Navigator Pattern (Routing con History)
- Disaccoppia controller e view
- Supporta history (back)
- Centralizza routing

### 6. Facade Pattern
- `GeneralUserDAO`: Facciata che aggrega Gestore + UtenteFinale
- LogicController: Facciata del caso d'uso

### 7. Layering Pattern
- Strict layering: View -> Graphic -> Logic -> DAO -> Entity
- Dipendenze verso il basso (da alto a basso)
- No dipendenze circolari

### 8. Dependency Injection (by Parameter)
- LogicController riceve DAO/Service tramite getter che usa factory
- GraphicController riceve Navigator tramite costruttore
- ServiceFactory.get*Service(): Inietta interfacce nei logic controller

### 9. Interface Segregation (ISP)
- Service controller implementano interfacce piccole e specifiche
- Pagamento ha 3 interfacce (prenotazione, rimborso, penalità)
- Notifica ha 6 interfacce (per diversi contesti)

### 10. Template Method Pattern
- `AbstractGraphicControllerNavigation`: Implementa logica comune routing
- `AbstractGraphicLoginController`: Implementa logica comune login
- Sottoclassi concrete GUI/CLI override metodi specifici

---

## Casi d'Uso Principali

### UC1: Login/Accesso
**Attori**: Utente finale o Gestore
**Flow**:
1. View mostra form login
2. Utente inserisce email + password
3. GraphicController valida input
4. LogicControllerGestioneAccesso.verificaCredenziali()
   - Recupera utente da DAO
   - Confronta password
   - Verifica StatoAccount == ATTIVO
   - Crea SessioneUtenteBean (UUID token)
   - Salva log
5. Se ok: naviga a HOME con sessione in params
6. Se fail: mostra errore

### UC2: Registrazione
**Attori**: Utente esterno
**Flow**:
1. View mostra form registrazione
2. Utente riempie: nome, cognome, email, password, ruolo
3. GraphicController valida input
4. LogicControllerRegistrazione.creaUtenteFinale()
   - Valida email unica
   - Crea UtenteFinale
   - Salva via DAO
   - Invia notifica registro
5. Se ok: naviga a HOME

### UC3: Prenotazione Campi
**Attori**: Utente finale
**Flow**:
1. View mostra parametri ricerca (data, ora, durata)
2. Utente riempie e chiama "Cerca slot"
3. GraphicController.cercaDisponibilita()
   - LogicControllerPrenotazioneCampo.trovaSlotDisponibili()
   - Delega a GestioneDisponibilitaPrenotazione (service)
   - Ritorna List<DatiDisponibilitaBean>
4. View mostra slot disponibili
5. Utente seleziona e chiama "Prenota"
6. GraphicController.creaPrenotazione()
   - LogicControllerPrenotazioneCampo.nuovaPrenotazione()
   - Crea Prenotazione (stato DA_PAGARE)
   - Salva via DAO
   - Ritorna RiepilogoPrenotazioneBean
7. View mostra riepilogo + form pagamento
8. Utente inserisce dati pagamento
9. GraphicController.completaPrenotazione()
   - LogicControllerPrenotazioneCampo.completaPrenotazione()
   - Richiede pagamento (GestionePagamentoPrenotazione)
   - Se pagato: aggiorna stato CONFERMATA, genera fattura, invia notifica, occupa slot
   - Ritorna StatoPagamentoBean
10. Se ok: mostra successo e naviga a HOME

### UC4: Disdetta Prenotazione (Two-Actor)
**Attori**: Utente finale, Gestore
**Flow**:
1. **Utente**: Seleziona prenotazione e chiama "Chiedi disdetta"
2. **GraphicController.creaRichiestaDisdetta()**
   - LogicControllerDisdettaPrenotazione.creaRichiestaDisdetta()
   - Crea RichiestaDisdettaRimborso (stato PENDING)
   - Salva via DAO
   - Invia notifica
3. **Gestore**: Accede a "Richieste disdetta"
4. **GraphicController.approvaRichiestaDisdetta()** (o rifiuta)
   - LogicControllerDisdettaPrenotazione.approvaRichiestaDisdetta()
   - Se approva:
     - Aggiorna prenotazione stato ANNULLATA
     - Calcola rimborso (con sconto/penale)
     - Esegue rimborso (GestionePagamentoRimborso)
     - Genera rimborso fattura
     - Libera slot
     - Invia notifica cancellazione
   - Se rifiuta: aggiorna stato RIFIUTATA

### UC5: Configura Regole
**Attori**: Gestore
**Flow**:
1. Gestore accede a "Gestione Regole"
2. Vede tabs: Campo, Tempistiche, Penalità
3. **Aggiorna Regola Campo**: marca manutenzione
   - GraphicController.aggiornaRegolaCampo()
   - LogicControllerConfiguraRegole.aggiornaRegolaCampo()
   - Invalida slot via GestioneDisponibilitaGestioneRegole
4. **Aggiorna Tempistiche**: preavviso minimo, durata slot, orari
   - GraphicController.aggiornaTempistiche()
   - Salva RegoleTempistiche
   - Invia notifica broadcast
5. **Aggiorna Penalità**: % penale, giorni sconto disdetta
   - GraphicController.aggiornaPenalita()
   - Salva RegolePenalita
   - Invia notifica broadcast

### UC6: Applica Penalità
**Attori**: Gestore
**Flow**:
1. Gestore accede a "Applica Penalità"
2. Seleziona utente, inserisce importo + motivazione
3. GraphicController.applicaPenalita()
   - LogicControllerApplicaPenalita.applicaPenalita()
   - Crea Penalita
   - Richiede pagamento (GestionePagamentoPenalita)
   - Se pagato: genera fattura, invia notifica, sospende account utente
   - Registra log
4. Se ok: mostra successo

### UC7: Gestione Account
**Attori**: Utente finale o Gestore
**Flow**:
1. Utente accede a "Il mio account"
2. Visualizza dati anagrafici
3. Modifica nome/cognome/email
4. GraphicController.aggiornaDatiAccount()
   - LogicControllerGestioneAccount.aggiornaDatiAccountConNotifica()
   - Aggiorna utente via DAO
   - Invia notifica aggiornamento
5. Cambio password
6. GraphicController.cambiaPassword()
   - Valida vecchia password
   - Imposta nuova password
   - Invia notifica

### UC8: Visualizzazione Log
**Attori**: Utente finale o Gestore
**Flow**:
1. Utente accede a "Cronologia"
2. GraphicController.recuperaLog()
   - LogicControllerGestioneAccount.recuperaLog()
   - Recupera SystemLog da DAO
   - Converte a LogEntryBean
   - Ritorna LogsBean
3. View mostra lista log (timestamp, operazione, descrizione)

---

## Flussi End-to-End

### Flusso 1: Login Completo
```
USER
  ↓ Inserisce email+password
GUILoginView.onShow(params)
  ↓ Carica fxml + prende input
GUIGraphicLoginController.effettuaLogin(DatiLoginBean)
  ├─ Valida bean
  ├─ Ottiene logicController via LogicControllerFactory
  └─ Chiama verificaCredenziali(bean)
LogicControllerGestioneAccesso.verificaCredenziali()
  ├─ Valida bean
  ├─ Recupera GeneralUserDAO via DAOFactory
  ├─ userDAO.findByEmail(normEmail)
  ├─ Confronta password
  ├─ Verifica StatoAccount == ATTIVO
  ├─ Crea SessioneUtenteBean (UUID + user + date)
  ├─ Salva log via logDAO
  └─ Return SessioneUtenteBean
GraphicLoginController continua
  ├─ Se null: goToLoginWithError(msg)
  └─ Se sessione: navigator.goTo(ROUTE_HOME, params)
Navigator.goTo()
  ├─ Recupera GUIHomeView dalla map route
  ├─ Aggiorna history
  ├─ Chiama homeView.onShow(params)
GUIHomeView.onShow(params)
  ├─ Estrae SessioneUtenteBean da params
  └─ Visualizza home con info utente
```

### Flusso 2: Prenotazione (Semplificato)
```
USER seleziona data/ora/durata
GUIPrenotazioneView raccoglie input
  ↓ Crea ParametriVerificaBean
GUIGraphicControllerPrenotazione.cercaDisponibilita()
  ↓ Ottiene LogicController via factory
LogicControllerPrenotazioneCampo.trovaSlotDisponibili()
  ├─ Ottiene GestioneDisponibilitaPrenotazione via ServiceFactory
  ├─ dispCtrl.trovaSlotDisponibili()
  └─ Return List<DatiDisponibilitaBean>
GraphicController.navigator.goTo(ROUTE_PRENOTAZIONE, {slot})
  ↓ View mostra slot disponibili
USER seleziona slot
  ↓ Clicca "Prenota"
GUIGraphicControllerPrenotazione.creaPrenotazione()
  ├─ Valida input
  ├─ LogicControllerPrenotazioneCampo.nuovaPrenotazione()
  │  ├─ Verifica disponibilità (riverca)
  │  ├─ Crea Prenotazione (stato DA_PAGARE)
  │  └─ Salva via prenotazioneDAO
  └─ Return RiepilogoPrenotazioneBean
View mostra riepilogo + form pagamento
USER inserisce dati pagamento + Clicca "Paga"
  ↓ Crea DatiPagamentoBean
GUIGraphicControllerPrenotazione.completaPrenotazione()
  ├─ Valida input
  ├─ LogicControllerPrenotazioneCampo.completaPrenotazione()
  │  ├─ payCtrl.richiediPagamentoPrenotazione()
  │  ├─ Se pagato:
  │  │  ├─ Aggiorna prenotazione (stato CONFERMATA)
  │  │  ├─ fattCtrl.creaScontrino()
  │  │  ├─ notiCtrl.inviaConfermaPrenotazione()
  │  │  └─ dispCtrl.occupaSlot()
  │  └─ Return StatoPagamentoBean
  └─ Se successo: navigator.goTo(ROUTE_HOME)
GUIHomeView.onShow()
  └─ Visualizza home con messaggio successo
```

### Flusso 3: Disdetta (Two-Actor)
```
ATTORE 1: UTENTE FINALE
USER
  ↓ Seleziona prenotazione + Chiede disdetta
GUIGraphicControllerDisdetta.creaRichiestaDisdetta()
  ├─ Valida input
  ├─ LogicControllerDisdettaPrenotazione.creaRichiestaDisdetta()
  │  ├─ Verifica prenotazione esiste e non annullata
  │  ├─ Crea RichiestaDisdettaRimborso (stato PENDING)
  │  ├─ Salva via richiestaDisdettaDAO
  │  └─ notiCtrl.inviaConfermaCancellazione()
  └─ View mostra "Richiesta inviata"

                    ↓↓↓ TEMPO PASSA ↓↓↓

ATTORE 2: GESTORE
GESTORE
  ↓ Accede a "Richieste disdetta"
GUIGraphicControllerRichiesteDisdetta.onShow()
  ├─ LogicControllerDisdettaPrenotazione.listaRichiesteDisdetta()
  └─ View mostra lista PENDING
GESTORE seleziona richiesta + Clicca "Approva"
  ↓
GUIGraphicControllerRichiesteDisdetta.approvaRichiestaDisdetta()
  ├─ Valida (solo GESTORE)
  ├─ LogicControllerDisdettaPrenotazione.approvaRichiestaDisdetta()
  │  ├─ Verifica stato == PENDING
  │  ├─ Aggiorna prenotazione (stato ANNULLATA)
  │  ├─ payCtrl.eseguiRimborso()
  │  ├─ fattCtrl.creaRimborsoFattura()
  │  ├─ dispCtrl.liberaSlot()
  │  ├─ notiCtrl.inviaConfermaCancellazione()
  │  └─ Aggiorna richiesta (stato APPROVATA)
  └─ View mostra "Richiesta approvata"
```

---

## Organizzazione dei Package

```
com.ispw
├── bootstrap/
│   ├── AppBootstrapper.java (entry point)
│   ├── AppConfigurator.java
│   ├── AppConfig.java
│   ├── DbmsInitializer.java
│   ├── FileSystemInitializer.java
│   └── SetupBootstrapper.java
│
├── view/
│   ├── interfaces/
│   │   ├── GenericView.java
│   │   ├── ViewLogin.java
│   │   ├── ViewGestioneAccount.java
│   │   └── ... (altre interfacce view)
│   │
│   ├── gui/
│   │   ├── GUILoginView.java
│   │   ├── GUIHomeView.java
│   │   ├── GUIPrenotazioneView.java
│   │   ├── fxml/ (FXML controller)
│   │   │   ├── LoginFXMLController.java
│   │   │   ├── PrenotazioneFXMLController.java
│   │   │   └── ...
│   │   └── GuiLauncher.java
│   │
│   ├── cli/
│   │   ├── CLILoginView.java
│   │   ├── CLIHomeView.java
│   │   ├── console/ (console view specifiche)
│   │   │   ├── ConsoleLoginView.java
│   │   │   └── ...
│   │   └── GenericViewCLI.java
│   │
│   └── common/
│       └── GenericViewBase.java (base comune GUI/CLI)
│
├── controller/
│   ├── graphic/
│   │   ├── interfaces/
│   │   │   ├── NavigableController.java
│   │   │   ├── GraphicLoginController.java
│   │   │   ├── GraphicControllerAccount.java
│   │   │   ├── GraphicControllerPrenotazione.java
│   │   │   ├── GraphicControllerNavigation.java
│   │   │   └── GraphicControllerUtils.java (costanti route+keys)
│   │   │
│   │   ├── abstracts/
│   │   │   ├── AbstractGraphicLoginController.java
│   │   │   ├── AbstractGraphicControllerAccount.java
│   │   │   ├── AbstractGraphicControllerPrenotazione.java
│   │   │   ├── AbstractGraphicControllerNavigation.java
│   │   │   └── ...
│   │   │
│   │   ├── gui/
│   │   │   ├── GUIGraphicLoginController.java
│   │   │   ├── GUIGraphicControllerAccount.java
│   │   │   ├── GUIGraphicControllerPrenotazione.java
│   │   │   ├── GUIGraphicControllerNavigation.java
│   │   │   └── ...
│   │   │
│   │   ├── cli/
│   │   │   ├── CLIGraphicLoginController.java
│   │   │   ├── CLIGraphicControllerAccount.java
│   │   │   └── ...
│   │   │
│   │   └── factory/
│   │       ├── FrontendControllerFactory.java (abstract)
│   │       ├── GUIFrontendControllerFactory.java (concrete)
│   │       └── CLIFrontendControllerFactory.java (concrete)
│   │
│   ├── logic/
│   │   ├── interfaces/
│   │   │   ├── CtrlAccesso.java
│   │   │   ├── CtrlPrenotazione.java
│   │   │   ├── CtrlDisdetta.java
│   │   │   ├── CtrlGestioneRegole.java
│   │   │   ├── CtrlApplicaPenalita.java
│   │   │   └── ... (sottocartelle per interfacce specifiche)
│   │   │
│   │   ├── ctrl/
│   │   │   ├── LogicControllerGestioneAccesso.java
│   │   │   ├── LogicControllerPrenotazioneCampo.java
│   │   │   ├── LogicControllerDisdettaPrenotazione.java
│   │   │   ├── LogicControllerConfiguraRegole.java
│   │   │   ├── LogicControllerApplicaPenalita.java
│   │   │   ├── LogicControllerGestioneAccount.java
│   │   │   ├── LogicControllerRegistrazione.java
│   │   │   ├── LogicControllerGestionePagamento.java
│   │   │   ├── LogicControllerGestioneFattura.java
│   │   │   ├── LogicControllerGestioneNotifica.java
│   │   │   ├── LogicControllerGestoreDisponibilita.java
│   │   │   ├── LogicControllerGestioneManutenzione.java
│   │   │   └── LogicControllerHelper.java
│   │   │
│   │   ├── LogicControllerFactory.java (factory principale)
│   │   └── ServiceFactory.java (factory servizi secondari)
│   │
│   └── [... no altro package controller ...]
│
├── dao/
│   ├── interfaces/
│   │   ├── DAO.java (marker interface)
│   │   ├── GeneralUserDAO.java
│   │   ├── CampoDAO.java
│   │   ├── PrenotazioneDAO.java
│   │   ├── PagamentoDAO.java
│   │   ├── FatturaDAO.java
│   │   └── ... (altri DAO)
│   │
│   ├── factory/
│   │   ├── DAOFactory.java (abstract factory + singleton)
│   │   ├── DbmsDAOFactory.java
│   │   ├── FileSystemDAOFactory.java
│   │   └── MemoryDAOFactory.java
│   │
│   ├── impl/
│   │   ├── base/
│   │   │   ├── BaseGeneralUserDAO.java
│   │   │   ├── BaseCampoDAO.java
│   │   │   └── ... (impl base comuni)
│   │   │
│   │   ├── dbms/
│   │   │   ├── concrete/
│   │   │   │   ├── DbmsGeneralUserDAO.java
│   │   │   │   ├── DbmsCampoDAO.java
│   │   │   │   └── ...
│   │   │   ├── base/
│   │   │   │   └── DbmsDAO.java (base DBMS)
│   │   │   └── connection/
│   │   │       ├── DbmsConnectionFactory.java
│   │   │       └── ConnectionFactory.java
│   │   │
│   │   ├── filesystem/
│   │   │   ├── concrete/
│   │   │   │   ├── FileSystemGeneralUserDAO.java
│   │   │   │   └── ...
│   │   │   ├── json/
│   │   │   │   └── JsonListFileStore.java
│   │   │   └── FileSystemDAO.java (base filesystem)
│   │   │
│   │   ├── memory/
│   │   │   └── concrete/
│   │   │       ├── InMemoryGeneralUserDAO.java
│   │   │       └── ...
│   │   │
│   │   └── aggregate/
│   │       └── AggregatingGeneralUserDAO.java
│   │
│   └── exception/
│       └── DaoException.java
│
├── model/
│   ├── entity/
│   │   ├── GeneralUser.java (abstract)
│   │   ├── Gestore.java
│   │   ├── UtenteFinale.java
│   │   ├── Campo.java
│   │   ├── Prenotazione.java
│   │   ├── Pagamento.java
│   │   ├── Fattura.java
│   │   ├── Penalita.java
│   │   ├── RegolePenalita.java
│   │   ├── RegoleTempistiche.java
│   │   ├── RichiestaDisdettaRimborso.java
│   │   ├── SystemLog.java
│   │   └── ...
│   │
│   └── enums/
│       ├── Ruolo.java
│       ├── StatoAccount.java
│       ├── StatoPrenotazione.java
│       ├── StatoPagamento.java
│       ├── MetodoPagamento.java
│       ├── PersistencyProvider.java (DBMS, FILE_SYSTEM, IN_MEMORY)
│       ├── FrontendProvider.java (GUI, CLI)
│       └── ... (altri enum)
│
└── bean/
    ├── DatiLoginBean.java
    ├── SessioneUtenteBean.java
    ├── UtenteBean.java
    ├── DatiRegistrazioneBean.java
    ├── DatiAccountBean.java
    ├── DatiInputPrenotazioneBean.java
    ├── ParametriVerificaBean.java
    ├── RiepilogoPrenotazioneBean.java
    ├── DatiDisponibilitaBean.java
    ├── DatiPagamentoBean.java
    ├── StatoPagamentoBean.java
    ├── DatiFatturaBean.java
    ├── DatiPenalitaBean.java
    ├── EsitoOperazioneBean.java
    ├── EsitoDisdettaBean.java
    ├── LogsBean.java
    ├── LogEntryBean.java
    ├── RichiestaDisdettaBean.java
    ├── UtentiBean.java
    ├── UtenteSelezioneBean.java
    ├── Base*Bean.java (base comuni)
    └── ...
```

---

## Dipendenze e DIP

### Matrice Dipendenze Layer

| Layer | Dipende da | Tipo | DIP |
|-------|-----------|------|-----|
| View | GraphicController (NavigableController) | Interfaccia | ✅ |
| Graphic | GraphicControllerNavigation (interfaccia) | Interfaccia | ✅ |
| Graphic | LogicControllerFactory (astratto) | Astratto | ✅ |
| Graphic | Bean per IO | DTO | ✅ |
| Navigator | NavigableController (interfaccia) | Interfaccia | ✅ |
| Navigator | View (come NavigableController) | Interfaccia | ✅ |
| Logic | DAO (interfaccia) | Interfaccia | ✅ |
| Logic | Service (interfaccia) | Interfaccia | ✅ |
| Logic | Bean per IO | DTO | ✅ |
| Service | DAO (interfaccia) | Interfaccia | ✅ |
| Service | Bean per IO | DTO | ✅ |
| DAO | DAOFactory (astratto) | Astratto | ✅ |
| DAO | Entity | Model | ✅ |
| Entity | Nulla | - | N/A |
| Bean | Nulla | - | N/A |

### Problemi DIP Identificati

1. **⚠️ GUIGraphicLoginController istanzia LogicController direttamente**
   ```java
   @Override
   protected SessioneUtenteBean verificaCredenziali(DatiLoginBean credenziali) {
       return new LogicControllerGestioneAccesso().verificaCredenziali(credenziali);
   }
   ```
   **Dovrebbe**:
   ```java
   protected SessioneUtenteBean verificaCredenziali(DatiLoginBean credenziali) {
       return logicController().verificaCredenziali(credenziali);
   }
   // dove logicController() è nella abstract class:
   protected CtrlAccesso logicController() {
       return LogicControllerFactory.getAccessoController();
   }
   ```
   **Impatto**: Violazione DIP minore, testability ridotta

2. **Conversione entity -> bean manuale** nei LogicController
   - Duplicazione di codice
   - Difficile manutenere
   - **Fix**: Creare BeanMapper utility

---

## Considerazioni di Progetto

### Decisioni Architetturali

1. **Layering Strict**: Nessuna dipendenza circolare, flusso top-down
2. **Controller Stateless**: Facilita testing, thread-safety, scalabilità
3. **Factory Everywhere**: Runtime selection backend/frontend, DIP
4. **Bean/DTO Separati**: Protezione entity, cambiamento API senza impatto DB
5. **Navigator Pattern**: Disaccoppiamento controller-view, history support
6. **Interface Segregation**: Interfacce piccole e specifiche per ruoli

### Forze e Debolezze

**Forze**:
- ✅ Separazione chiara dei layer
- ✅ Testabilità elevata (stateless, factory, interfacce)
- ✅ Multi-backend supportato
- ✅ Multi-frontend supportato
- ✅ Manutenibilità: cada layer ha responsabilità definita
- ✅ DIP prevalentemente rispettato
- ✅ Facile aggiungere nuovi use case

**Debolezze**:
- ⚠️ Molti layer -> verbosità (classi abstract per ogni caso d'uso)
- ⚠️ Conversione entity->bean manuale
- ⚠️ LogicControllerPrenotazioneCampo troppo responsabile
- ⚠️ Password in plain text (security)
- ⚠️ Payment gateway fittizio
- ⚠️ Notifiche fittizie (solo logging)

### Decisioni di Trade-off

1. **Verbosità vs Separazione**: Scelto separazione (abstract login, graphic, logic per ogni caso d'uso) su brevità
2. **Flexibility vs Performance**: Scelto factory ovunque su performance (creazione oggetti a runtime)
3. **Security vs Semplicità**: Scelto password plain text per semplicità prototipo (va fixato per produzione)
4. **Transazione FileSystem**: No transazione (semplicità) su ACID (rischio dati)

### Production Readiness Checklist

- [ ] Password hashing (BCrypt/Argon2)
- [ ] Session validation lato server
- [ ] Payment gateway reale (Stripe/PayPal)
- [ ] Email service reale (SMTP/SendGrid)
- [ ] Transaction FileSystem DAO
- [ ] Logging robusto (SLF4J/Logback)
- [ ] Rate limiting
- [ ] HTTPS/TLS
- [ ] Database connection pooling (HikariCP)
- [ ] Unit test coverage > 80%
- [ ] Integration test suite

---

## Conclusioni

L'architettura è **solida, ben-strutturata, e orientata alla testabilità**. È appropriata per un progetto universitario e fornisce buone fondamenta per un prodotto reale con alcuni hardening necessari.

**Voto Architettura**: 8/10

- Buono: Layering, DIP, Testabilità, Multi-backend/frontend
- Migliorabile: Password hashing, Payment real, Service notification real

