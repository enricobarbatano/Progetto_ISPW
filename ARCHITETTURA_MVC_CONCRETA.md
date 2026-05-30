# 🏗️ ANALISI ARCHITETTURALE CONCRETA - CENTRO SPORTIVO

## Indice
1. [Overview Architettura Reale](#1-overview-architettura-reale)
2. [Mapping MVC Reale](#2-mapping-mvc-reale-con-giustificazioni)
3. [Descrizione Concreta dei Layer](#3-descrizione-concreta-dei-layer)
4. [Flusso End-to-End Reale: REGISTRAZIONE](#4-flusso-end-to-end-reale-registrazione)
5. [Dipendenze e Direzione del Flusso](#5-dipendenze-e-direzione-del-flusso)
6. [Pattern Effettivamente Usati](#6-pattern-effettivamente-usati)
7. [Ruolo dei Bean nella Codebase](#7-ruolo-dei-bean-nella-codebase)
8. [Anatomia delle Responsabilità (SRP)](#8-anatomia-delle-responsabilità-single-responsibility-principle)
9. [Problemi Architetturali Reali](#9-problemi-architetturali-reali-identificati-dal-codice)
10. [Riepilogo Architetturale](#10-riepilogo-architetturale)

---

## 1. Overview Architettura Reale

Il sistema è strutturato come un'**architettura multi-layer MVC estesa** con **supporto polimorfico** per backend (DBMS, FileSystem, In-Memory) e frontend (GUI JavaFX, CLI Console).

### Struttura organizzativa reale:

```
com.ispw/
├── App.java                    ← Entry point banale
├── bootstrap/                  ← LAYER 0: Inizializzazione
│   ├── AppBootstrapper         ← Orchestrazione bootstrap
│   ├── AppConfigurator         ← Selezione backend/frontend
│   ├── DbmsInitializer         ← Init connessione MySQL
│   └── FileSystemInitializer   ← Init cartelle JSON
├── controller/                 ← LAYER 2-5: Logica applicativa
│   ├── graphic/                ← Layer 2: UI Controller (GUI/CLI)
│   │   ├── gui/                ← Impl concrete GUI
│   │   ├── cli/                ← Impl concrete CLI
│   │   ├── abstracts/          ← Logica comune GUI/CLI
│   │   ├── interfaces/         ← Contratti GraphicController
│   │   └── factory/            ← FrontendControllerFactory
│   └── logic/                  ← Layer 4-5: Business Logic
│       ├── ctrl/               ← LogicController concreti
│       ├── interfaces/         ← Contratti DAO-based
│       ├── LogicControllerFactory
│       └── ServiceFactory
├── view/                       ← LAYER 1: Presentazione
│   ├── gui/                    ← JavaFX Views + FXML
│   ├── cli/                    ← Console Views
│   ├── interfaces/             ← Contratto View
│   └── shared/                 ← Componenti riusabili
├── dao/                        ← LAYER 6: Persistenza
│   ├── interfaces/             ← Contratti DAO
│   ├── factory/                ← DAOFactory (polimorsfico)
│   ├── impl/
│   │   ├── dbms/               ← MySQL
│   │   ├── filesystem/         ← JSON
│   │   └── memory/             ← In-Memory
│   └── exception/
├── model/                      ← LAYER 7: Dominio
│   ├── entity/                 ← Domain objects (Entity)
│   └── enums/                  ← Costanti enumerate
├── bean/                       ← LAYER 8: DTO Transfer
│   ├── Dati*Bean               ← Input (View->Controller)
│   ├── Esito*Bean              ← Output (Controller->View)
│   └── *Bean                   ← Sessione, Riepilogo
└── service/                    ← Servizi esterni (Email)
    └── EmailNotificationService
```

### I 9 Layer Effettivi (Bottom-Up):

| Layer | Pacchetto | Responsabilità | Es. di Classe |
|-------|-----------|-----------------|---------------|
| **7** | `model.entity` | Dominio persistente | `GeneralUser`, `Prenotazione`, `Pagamento` |
| **6** | `dao` | Astrazione persistenza | `GeneralUserDAO`, `PrenotazioneDAO` |
| **5** | `controller.logic` | Servizi secondari | `LogicControllerGestioneFattura`, `LogicControllerGestioneNotifica` |
| **4** | `controller.logic` | Logica principale use case | `LogicControllerPrenotazioneCampo`, `LogicControllerGestioneAccesso` |
| **3** | `controller.graphic` | Routing/Navigazione | `GraphicControllerNavigation`, `AbstractGraphicControllerNavigation` |
| **2** | `controller.graphic` | UI Logic (validazione input) | `GUIGraphicControllerRegistrazione`, `CLIGraphicControllerPrenotazione` |
| **1** | `view` | Presentazione | `GUIRegistrazioneView`, `CLIPrenotazioneView` |
| **0** | `bootstrap` | Inizializzazione | `AppBootstrapper` |
| **DTO** | `bean` | Transfer objects | `DatiRegistrazioneBean`, `SessioneUtenteBean` |

---

## 2. Mapping MVC Reale (con Giustificazioni)

### Cosa è il **MODEL**?

**Definizione concreta nel codice:**
- **Entity**: `com.ispw.model.entity.*` (GeneralUser, UtenteFinale, Prenotazione, Pagamento, Fattura, Penalita, RichiestaDisdetta, SystemLog, Regole)
- **DAO**: `com.ispw.dao.interfaces.*` + implementazioni (GeneralUserDAO, PrenotazioneDAO, PagamentoDAO)
- **Enums**: `com.ispw.model.enums.*` (Ruolo, StatoAccount, StatoPrenotazione, TipoOperazione)
- **Entity Relationship**: Mapping 1:1 con tabelle/JSON (id, campo, stato)

**Perché è il Model:**
```
Entity = Stato persistente del dominio (dati business)
DAO = Accesso astratto al persistente (interfacce, non SQL)
Enum = Vincoli business codificati
```

**NON sono Model:**
- ❌ Bean (DatiXBean, EsitoXBean) - sono DTO, non Entity
- ❌ LogicController - è logica, non dati
- ❌ View - è presentazione, non dati

---

### Cosa è la **VIEW**?

**Definizione concreta nel codice:**

#### **GUI (JavaFX + FXML)**:
- `com.ispw.view.gui.GUILoginView` → carica `/fxml/login.fxml` → delega a `LoginFXMLController`
- `com.ispw.view.gui.GUIRegistrazioneView` → carica `/fxml/registrazione.fxml` → delega a `RegistrazioneFXMLController`
- `com.ispw.view.gui.GUIPrenotazioneView` → carica `/fxml/prenotazione.fxml` → delega a `PrenotazioneFXMLController`
- **Estendono**: `GenericViewGUI` → `GenericViewBase`
- **Usano**: `GuiLauncher.setRoot(Parent)` per cambiare stage

#### **CLI (Console)**:
- `com.ispw.view.cli.CLILoginView` → usa `ConsoleLoginView`, `ConsoleMenu`
- `com.ispw.view.cli.CLIRegistrazioneView` → usa `ConsoleRegistrazioneView`
- **Estendono**: `GenericViewCLI` → `GenericViewBase`
- **Input**: `console.readEmail()`, `console.readChoice()`, etc.

**Responsabilità reale della View:**
- ✅ Carica layout FXML (GUI) o disegna menu (CLI)
- ✅ Raccoglie input da utente
- ✅ Chiama `GraphicController.metodo(bean)`
- ✅ Visualizza risultati (EsitoBean, RiepilogoBean)
- ✅ Mostra errori

**NON deve fare:**
- ❌ Logica di dominio
- ❌ Accesso diretto a DAO
- ❌ Conversione Entity → Bean
- ❌ Navigazione tra schermate (delega a Navigator)

**Contratto View**: `GenericView` + `NavigableController`
```java
public interface NavigableController {
    String getRouteName();
    void onShow(Map<String, Object> params);
    void onHide();
}
```

---

### Cosa è il **CONTROLLER**? (Complesso, multi-livello)

Il controller nel codice è **diviso in 3 responsabilità**:

#### **CONTROLLER GRAFICO** (Layer 2)
**Classe**: `GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione`

**Responsabilità**:
1. Riceve dati grezzi dalla View (String nome, String cognome, String email, String password)
2. Costruisce Bean: `DatiRegistrazioneBean bean = buildRegistrazioneBean(...)`
3. Valida basic: null check, lunghezza campi
4. Ottiene LogicController: `logicController().registraNuovoUtente(bean)`
5. Gestisce eccezioni specifiche
6. Naviga a nuova pagina o mostra errore

**Flusso concreto**:
```java
@Override
public void inviaDatiRegistrazione(String nome, String cognome, String email, String password, Ruolo ruolo) {
    DatiRegistrazioneBean bean = buildRegistrazioneBean(nome, cognome, email, password);
    try {
        EsitoOperazioneBean esito = logicController().registraNuovoUtente(bean);
        if (esito != null && esito.isSuccesso()) {
            vaiAlLogin();  // Delega navigator
        }
    } catch (PasswordTooShortException | InvalidEmailFormatException | EmailAlreadyExistsException e) {
        showError(e.getMessage());
    }
}
```

**Caratteristiche**:
- ✅ Stateless (no campo di istanza)
- ✅ No logica di dominio
- ✅ DIP: `logicController()` torna interfaccia, impl via factory

---

#### **LOGIC CONTROLLER** (Layer 4)
**Classe**: `LogicControllerRegistrazione implements CtrlRegistrazione`

**Responsabilità**:
1. **Validazione complessa**: password >= 6 char, email valida, email unica in DB
2. **Logica di dominio**: Creazione UtenteFinale, hash password, stato account
3. **Orchestrazione persistenza**: `userDAO().store(nuovo)`
4. **Logging operazione**: `logDAO().store(systemLog)`
5. **Trigger servizi secondari**: `notiCtrl().inviaConfermaRegistrazione(...)`
6. **Conversione Entity → Bean** per risposta

**Flusso concreto**:
```java
@Override
public EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput) throws RegistrationException {
    // 1) Validazione basic
    if (!isValid(datiInput)) throw new InvalidRegistrationDataException();
    
    // 2) Validazione password
    if (datiInput.getPassword().length() < 6) throw new PasswordTooShortException();
    
    // 3) Validazione email
    if (!LogicControllerHelper.isValidEmailFormat(datiInput.getEmail()))
        throw new InvalidEmailFormatException();
    
    // 4) Normalizzazione email
    final String emailNorm = LogicControllerHelper.normalizeEmail(datiInput.getEmail());
    
    // 5) Controllo unicità
    final GeneralUser existing = userDAO().findByEmail(emailNorm);
    if (existing != null) throw new EmailAlreadyExistsException();
    
    // 6) Creazione entity
    final UtenteFinale nuovo = new UtenteFinale();
    nuovo.setNome(datiInput.getNome());
    nuovo.setCognome(datiInput.getCognome());
    nuovo.setEmail(emailNorm);
    nuovo.setPassword(datiInput.getPassword());
    nuovo.setStatoAccount(StatoAccount.DA_CONFERMARE);
    nuovo.setRuolo(Ruolo.UTENTE);
    
    // 7) Persistenza
    userDAO().store(nuovo);
    
    // 8) Logging
    appendLog(nuovo.getIdUtente(), TipoOperazione.REGISTRAZIONE_ACCOUNT, "Registrazione avviata");
    
    // 9) Notifica
    inviaNotificaConfermaRegistrazione(toBean(nuovo));
    
    // 10) Risposta
    return new EsitoOperazioneBean(true, MSG_REG_OK);
}
```

**Caratteristiche**:
- ✅ Stateless
- ✅ No SQL diretto (accesso via DAO)
- ✅ No import impl concrete DAO (via DAOFactory)
- ✅ Testabile (DAO mockato)
- ⚠️ Assenza di mapper Bean→Entity (conversione manuale)

---

#### **NAVIGATOR** (Layer 3)
**Classe**: `GraphicControllerNavigation` (interfaccia) → `GUIGraphicControllerNavigation` (impl)

**Responsabilità**:
1. Routing: `goTo(String route, Map<String, Object> params)`
2. History management: Stack di route visitate
3. Disaccoppiamento: GraphicController non conosce View concreta

**Flusso concreto**:
```java
// Graphic controller chiama:
navigator.goTo(GraphicControllerUtils.ROUTE_LOGIN, null);

// Navigator:
// 1) Pushes currentRoute su history
// 2) Recupera LoginView dalla map routes
// 3) Chiama view.onShow(params)
// 4) View carica FXML e mostra login
```

---

### Sintesi MVC nel Codice

| Componente | Cosa è | Nel Codice |
|-----------|---------|-----------|
| **Model** | Entity + DAO + Enum | `model.entity.*`, `dao.interfaces.*`, `model.enums.*` |
| **View** | Presentazione | `view.gui.*` (FXML), `view.cli.*` (Console) |
| **Controller** | **3-layer** | |
|   - Grafico | Validazione UI, delegazione | `controller.graphic.gui.*` / `cli.*` |
|   - Logica | Dominio, orchestrazione DAO | `controller.logic.ctrl.*` |
|   - Navigator | Routing | `GraphicControllerNavigation` |

---

## 3. Descrizione Concreta dei Layer

### **LAYER 7: ENTITY (Dominio)**

**Package**: `com.ispw.model.entity`

**Classi Principali**:
- `GeneralUser` (base astratta) → `UtenteFinale`, `Gestore`
- `Prenotazione`
- `Pagamento`
- `Fattura`
- `Penalita`
- `RichiestaDisdetta`
- `SystemLog`
- `Campo`, `Disponibilita`, `RegolePenalita`, `RegoleTemistiche`

**Caratteristiche**:
- ✅ `Serializable` (persistenza JSON/DBMS)
- ✅ Getters/Setters per ogni campo
- ✅ No logica di dominio (anemia intenzionale)
- ✅ Commentati con sezione ARCHITETTURALE e LOGICA

**Esempio reale**:
```java
public abstract class GeneralUser implements Serializable {
    private int idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private StatoAccount statoAccount;
    private Ruolo ruolo;
    
    // getters/setters
}

public class UtenteFinale extends GeneralUser {
    // specializzazioni se necessarie
}
```

**Responsabilità**:
- ✅ Rappresentare lo stato persistente del dominio
- ✅ Vincoli (Serializable, ID)
- ❌ No logica di validazione
- ❌ No accesso DB

---

### **LAYER 6: DAO (Astrazione Persistenza)**

**Package**: `com.ispw.dao`

#### **Interfacce DAO** (Contratti):
```java
public interface GeneralUserDAO extends DAO<GeneralUser> {
    GeneralUser findByEmail(String email);
    UtenteFinale findUtenteFinaleByEmail(String email);
    Gestore findGestoreByEmail(String email);
}

public interface PrenotazioneDAO extends DAO<Prenotazione> {
    List<Prenotazione> findByIdCampo(int idCampo);
    List<Prenotazione> findByIdUtente(int idUtente);
    Prenotazione findById(int id);
}

public interface PagamentoDAO extends DAO<Pagamento> {
    List<Pagamento> findByIdPrenotazione(int idPrenotazione);
    StatoPagamentoDAO getStatoPagamento(int idPrenotazione);
}
// ... altri DAO
```

#### **DAOFactory** (Polimorfismo backend):

```java
public abstract class DAOFactory {
    private static PersistencyProvider provider;  // DBMS, FILE_SYSTEM, IN_MEMORY
    private static DAOFactory instance;
    
    public static synchronized void initialize(PersistencyProvider p, Path root) {
        provider = p;
        instance = switch (provider) {
            case DBMS        -> new DbmsDAOFactory();
            case FILE_SYSTEM -> new FileSystemDAOFactory();
            case IN_MEMORY   -> new MemoryDAOFactory();
        };
    }
    
    public static synchronized DAOFactory getInstance() {
        return instance;
    }
    
    // Metodi astratti
    public abstract GeneralUserDAO getGeneralUserDAO();
    public abstract PrenotazioneDAO getPrenotazioneDAO();
    // ... altri
}
```

#### **Implementazioni Concrete**:

**DbmsDAOFactory**:
- Istanzia `DbmsGeneralUserDAO` (implementa GeneralUserDAO)
- Usa JDBC per MySQL
- Query SQL dirette
- `DbmsConnectionFactory` gestisce connessioni

**FileSystemDAOFactory**:
- Istanzia `FileSystemGeneralUserDAO`
- Legge/scrive JSON file
- Carica da `filesystem/` cartella
- Serializzazione Jackson/GSON

**MemoryDAOFactory**:
- Istanzia `MemoryGeneralUserDAO`
- Carica da `seed/` cartella JSON (read-only)
- Mantiene in `HashMap<Integer, Entity>`
- Niente persistenza

**Regole DAO**:
- ✅ Interfacce, non impl concrete
- ✅ Un metodo per ogni query
- ✅ Return Entity, non risultati grezzi
- ✅ Delegano SQL al DBMS DAO concreto
- ✅ No logica di dominio
- ✅ Eccezioni `DaoException` per errori

**Flusso reale**:
```java
// LogicControllerRegistrazione.java
private GeneralUserDAO userDAO() {
    return DAOFactory.getInstance().getGeneralUserDAO();
}

// A runtime, dipendendo dalla config:
// - Se DBMS: torna DbmsGeneralUserDAO (esegue SELECT da MySQL)
// - Se FILE_SYSTEM: torna FileSystemGeneralUserDAO (legge JSON)
// - Se IN_MEMORY: torna MemoryGeneralUserDAO (cerca in HashMap)
```

---

### **LAYER 5: SERVICE CONTROLLER (Servizi Secondari)**

**Package**: `com.ispw.controller.logic`

**Interfacce Role-Based** (Interface Segregation Principle):

```
GestionePagamento*
├─ GestionePagamentoPrenotazione
├─ GestionePagamentoPenalita
├─ GestionePagamentoRimborso
└─ GestionePagamentoDisdetta

GestioneFattura*
├─ GestioneFatturaPrenotazione
├─ GestioneFatturaPenalita
└─ GestioneFatturaRimborso

GestioneNotifica*
├─ GestioneNotificaRegistrazione
├─ GestioneNotificaPrenotazione
├─ GestioneNotificaPenalita
├─ GestioneNotificaDisdetta
├─ GestioneNotificaGestioneAccount
└─ GestioneNotificaConfiguraRegole

GestioneDisponibilita*
├─ GestioneDisponibilitaPrenotazione
├─ GestioneDisponibilitaDisdetta
└─ GestioneDisponibilitaGestioneRegole

GestioneManutenzione*
└─ GestioneManutenzioneConfiguraRegole
```

**Implementazioni**:
- Tutte le interfacce `GestionePagamento*` → `LogicControllerGestionePagamento`
- Tutte le interfacce `GestioneFattura*` → `LogicControllerGestioneFattura`
- Tutte le interfacce `GestioneNotifica*` → `LogicControllerGestioneNotifica`
- Tutte le interfacce `GestioneDisponibilita*` → `LogicControllerGestoreDisponibilita`
- Interfaccia `GestioneManutenzione*` → `LogicControllerGestioneManutenzione`

**Esempio: GestionePagamento**
```java
public class LogicControllerGestionePagamento 
    implements GestionePagamentoDisdetta, GestionePagamentoPrenotazione,
               GestionePagamentoPenalita, GestionePagamentoRimborso {
    
    public void richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione) {
        // Logica pagamento prenotazione
        PagamentoDAO dao = DAOFactory.getInstance().getPagamentoDAO();
        Pagamento pag = new Pagamento();
        pag.setImporto(dati.getImporto());
        pag.setTipoPagamento(dati.getTipoPagamento());
        // ...
        dao.store(pag);
    }
    
    public void eseguiRimborso(int idPrenotazione, float importo) {
        // Logica rimborso
        // ...
    }
    // ... altri metodi
}
```

**ServiceFactory**:
```java
public class ServiceFactory {
    public static GestionePagamentoDisdetta getPagamentoDisdettaService() {
        return new LogicControllerGestionePagamento();
    }
    // ... altri factory methods
}
```

**Responsabilità**:
- ✅ Operazioni secondarie di dominio
- ✅ Accesso DAO tramite DAOFactory
- ✅ No stato di istanza
- ✅ Interfacce piccole (ISP)
- ❌ No logica principale (quel ruolo è dei LogicController principali)

---

### **LAYER 4: LOGIC CONTROLLER (Logica Principale Use Case)**

**Package**: `com.ispw.controller.logic.ctrl`

**7 Controller Principali**:

1. **LogicControllerGestioneAccesso** (`CtrlAccesso`)
   - Verifica credenziali (email/password)
   - Crea SessioneUtenteBean
   - Carica dati utente
   - **Flusso**: Email normalizzata → DAO find → Confronta password → Crea sessione

2. **LogicControllerRegistrazione** (`CtrlRegistrazione`)
   - Valida dati registrazione
   - Crea UtenteFinale
   - Salva su DAO
   - Registra log
   - Invia email conferma
   - **Flusso**: Validazione → Create entity → Store → Log → Email

3. **LogicControllerPrenotazioneCampo** (`CtrlPrenotazione`)
   - Trova campi disponibili
   - Ricerca slot liberi
   - Crea prenotazione
   - Coordina pagamento, fattura, notifica
   - **Flusso**: List campi → Verifica slot → Calcola costo → Occupa slot → Pagamento → Fattura → Notifica
   - ⚠️ **PROBLEMA**: Coordina 4 servizi (Pagamento, Fattura, Notifica, Disponibilità) - potrebbe essere troppo

4. **LogicControllerDisdettaPrenotazione** (`CtrlDisdetta`)
   - Processa richiesta disdetta
   - Calcola rimborso (meno penalità)
   - Genera fattura rimborso
   - Libera slot
   - Invia email

5. **LogicControllerGestioneAccount** (`CtrlGestioneAccount`)
   - Modifica dati anagrafi
   - Cambio password
   - Carica storico account

6. **LogicControllerApplicaPenalita** (`CtrlApplicaPenalita`)
   - Applica penalità a utente
   - Genera fattura penalità
   - Richiede pagamento
   - Invia notifica

7. **LogicControllerConfiguraRegole** (`CtrlGestioneRegole`)
   - Configura tempistiche
   - Imposta penalità
   - Marca campi in manutenzione

**LogicControllerFactory** (Singleton Factory):
```java
public final class LogicControllerFactory {
    public static CtrlAccesso getAccessoController() {
        return new LogicControllerGestioneAccesso();
    }
    public static CtrlRegistrazione getRegistrazioneController() {
        return new LogicControllerRegistrazione();
    }
    public static CtrlPrenotazione getPrenotazioneController() {
        return new LogicControllerPrenotazioneCampo();
    }
    // ... altri
}
```

**Regole Logic Controller**:
- ✅ Stateless (ottengono DAO/Service via factory)
- ✅ Input/Output via Bean
- ✅ No Entity direttamente da View (conversion via helper)
- ✅ DIP: Dipendono da interfacce DAO, non impl
- ✅ Eccezioni specifiche per ogni caso d'uso (PasswordTooShortException, EmailAlreadyExistsException)
- ⚠️ **PROBLEMA**: LogicControllerPrenotazioneCampo coordina troppi servizi (potrebbe beneficiare di Saga pattern)
- ⚠️ **PROBLEMA**: Nessun mapper centralizzato per Entity→Bean (conversione manuale)

---

### **LAYER 3: NAVIGATOR (Routing)**

**Package**: `com.ispw.controller.graphic`

**Interfaccia**:
```java
public interface GraphicControllerNavigation {
    void goTo(String route, Map<String, Object> params);
    void back();
    void exit();
}
```

**Implementazioni**:
- `GUIGraphicControllerNavigation` (per GUI JavaFX)
- `CLIGraphicControllerNavigation` (per CLI Console)

**Entrambi estendono**: `AbstractGraphicControllerNavigation`

**Struttura interna**:
```java
public abstract class AbstractGraphicControllerNavigation implements GraphicControllerNavigation {
    protected Map<String, NavigableController> routes;  // route name → View
    protected Deque<String> history;                     // Stack di route
    protected String currentRoute;
    
    public void goTo(String route, Map<String, Object> params) {
        // 1) Salva current route in history
        if (currentRoute != null) {
            history.push(currentRoute);
        }
        
        // 2) Carica controller per route
        NavigableController controller = routes.get(route);
        
        // 3) Mostra la view
        controller.onShow(params);
        
        // 4) Aggiorna current
        currentRoute = route;
    }
    
    public void back() {
        if (!history.isEmpty()) {
            String prev = history.pop();
            goTo(prev, null);
        }
    }
}
```

**Route Disponibili** (in `GraphicControllerUtils`):
```
ROUTE_LOGIN, ROUTE_HOME, ROUTE_REGISTRAZIONE, ROUTE_ACCOUNT,
ROUTE_PRENOTAZIONE, ROUTE_DISDETTA, ROUTE_REGOLE, ROUTE_PENALITA,
ROUTE_LOGS, ROUTE_RICHIESTE_DISDETTA
```

**Vantaggi**:
- ✅ Disaccoppiamento: GraphicController non conosce View concrete
- ✅ Routing centralizzato
- ✅ DIP: Navigator è interfaccia
- ✅ History support (back button)
- ❌ **PROBLEMA**: Map<String, NavigableController> deve essere popolata da qualcuno (chi?)

---

### **LAYER 2: GRAPHIC CONTROLLER (Validazione UI)**

**Package**: `com.ispw.controller.graphic`

**Interfacce** (Contratti per ogni use case):
```java
public interface GraphicLoginController extends NavigableController {
    void effettuaLogin(DatiLoginBean dati);
    void logout();
    void vaiARegistrazione();
}

public interface GraphicControllerRegistrazione extends NavigableController {
    void inviaDatiRegistrazione(String nome, String cognome, String email, String password, Ruolo ruolo);
}

public interface GraphicControllerPrenotazione extends NavigableController {
    void cercaCampiDisponibili(ParametriVerificaBean parametri);
    void effettuaPrenotazione(DatiInputPrenotazioneBean dati);
}
// ... altri
```

**Classi Astratte** (Logica comune GUI/CLI):
- `AbstractGraphicLoginController`
- `AbstractGraphicControllerRegistrazione`
- `AbstractGraphicControllerPrenotazione`
- `AbstractGraphicControllerNavigation`

**Implementazioni Concrete**:

**GUI**:
- `GUIGraphicLoginController extends AbstractGraphicLoginController`
- `GUIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione`
- `GUIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione`

**CLI**:
- `CLIGraphicLoginController extends AbstractGraphicLoginController`
- `CLIGraphicControllerRegistrazione extends AbstractGraphicControllerRegistrazione`

**Flusso reale GUIGraphicControllerRegistrazione**:
```java
@Override
public void inviaDatiRegistrazione(String nome, String cognome, String email, String password, Ruolo ruolo) {
    // 1) Crea Bean dai dati grezzi
    DatiRegistrazioneBean bean = buildRegistrazioneBean(nome, cognome, email, password);
    
    // 2) Ottiene LogicController (interfaccia)
    CtrlRegistrazione logicCtrl = logicController();
    
    // 3) Delega a logica di dominio
    try {
        EsitoOperazioneBean esito = logicCtrl.registraNuovoUtente(bean);
        
        // 4) Gestisce risultato
        if (esito != null && esito.isSuccesso()) {
            goToLogin();  // Naviga via Navigator
        }
    } catch (PasswordTooShortException e) {
        showError("Password troppo corta");
    } catch (EmailAlreadyExistsException e) {
        showError("Email già registrata");
    } catch (InvalidEmailFormatException e) {
        showError("Formato email non valido");
    }
}
```

**Regole Graphic Controller**:
- ✅ No logica di dominio complessa
- ✅ Validazioni basic (null, tipo)
- ✅ Costruzione Bean da dati grezzi
- ✅ Gestione eccezioni
- ✅ Navigazione tramite Navigator (DIP)
- ⚠️ **PROBLEMA**: Alcuni GraphicController istanziano LogicController con `new` anziché factory
- ⚠️ **PROBLEMA**: Logiche comuni duplicate tra GUI/CLI (potrebbe usare template method meglio)

---

### **LAYER 1: VIEW (Presentazione)**

**Package**: `com.ispw.view`

#### **Interfaccia base**:
```java
public interface NavigableController {
    String getRouteName();
    void onShow(Map<String, Object> params);
    void onHide();
}
```

#### **GUI Views** (JavaFX + FXML):

**Struttura**:
```
GUIRegistrazioneView
├─ Estende: GenericViewGUI → GenericViewBase
├─ Iniezione controller: GUIGraphicControllerRegistrazione
├─ onShow():
│  ├─ Carica FXML: /fxml/registrazione.fxml
│  ├─ Istanzia FXMLLoader
│  ├─ Recupera RegistrazioneFXMLController
│  ├─ Chiama fx.init(graphicController)
│  └─ GuiLauncher.setRoot(root)
└─ Gestisce errori di caricamento
```

**Flusso reale GUIRegistrazioneView.onShow()**:
```java
@Override
public void onShow(Map<String, Object> params) {
    super.onShow(params);
    sessione = null;  // Nessuna sessione in registrazione
    
    try {
        // 1) Carica FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registrazione.fxml"));
        Parent root = loader.load();
        
        // 2) Ottiene controller FXML
        RegistrazioneFXMLController fx = loader.getController();
        
        // 3) Iniezione del GraphicController
        fx.init(controller);  // controller = GUIGraphicControllerRegistrazione
        
        // 4) Render della view con params
        fx.render(params);
        
        // 5) Se ci sono errori da recovery, mostra
        if (params != null && params.containsKey("error")) {
            fx.showError((String) params.get("error"));
        }
        
        // 6) Cambia stage
        GuiLauncher.setRoot(root);
        
    } catch (IOException | RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Errore caricamento schermata", e);
        // Fallback: mostra errore
        VBox fallback = GuiViewUtils.createRoot();
        fallback.getChildren().add(new Label("Errore caricamento schermata"));
        GuiLauncher.setRoot(fallback);
    }
}
```

#### **CLI Views** (Console):

**Struttura**:
```
CLIRegistrazioneView
├─ Estende: GenericViewCLI → GenericViewBase
├─ Iniezione controller: CLIGraphicControllerRegistrazione
├─ onShow():
│  ├─ Mostra menu console
│  ├─ Legge input: console.readEmail(), console.readPassword()
│  ├─ Costruisce DatiRegistrazioneBean
│  ├─ Chiama controller.inviaDatiRegistrazione(...)
│  └─ Mostra risultato
└─ Nessun FXML, input/output via console
```

**Regole View**:
- ✅ Solo logica di presentazione
- ✅ No accesso diretto a DAO
- ✅ No Entity direttamente (usa Bean)
- ✅ Dipendenza su GraphicController (tramite interfaccia)
- ✅ Gestione errori di UI (fallback, messaggi)
- ❌ No logica di navigazione (delega a Navigator)

---

### **LAYER 0: BOOTSTRAP**

**Package**: `com.ispw.bootstrap`

**AppBootstrapper.main()** - Orchestrazione:
```java
public static void main(String[] args) {
    // 1) Chiedi configurazione (backend e frontend)
    AppConfigurator configurator = new AppConfigurator();
    AppConfig config = configurator.askUserConfiguration();
    
    // 2) Inizializza backend
    if (config.persistency() == PersistencyProvider.DBMS) {
        DbmsConnectionFactory.init("jdbc:mysql://localhost:3306/centro_sportivo", "user", "pass");
        // Verifica connessione
    } else if (config.persistency() == PersistencyProvider.FILE_SYSTEM) {
        Path fsRoot = Paths.get("filesystem");
        Files.createDirectories(fsRoot);
    } else if (config.persistency() == PersistencyProvider.IN_MEMORY) {
        Path seedRoot = Paths.get("seed");
        // Carica seed JSON in memoria
    }
    
    // 3) Inizializza DAOFactory
    DAOFactory.initialize(config.persistency(), root);
    
    // 4) Inizializza FrontendControllerFactory
    FrontendControllerFactory.setFrontendProvider(config.frontend());
    
    // 5) Avvia applicazione
    FrontendControllerFactory.getInstance().startApplication();
}
```

**Responsabilità**:
- ✅ Selezione backend (una sola volta)
- ✅ Selezione frontend (una sola volta)
- ✅ Inizializzazione factory polimorfiche
- ✅ Verifica prerequisiti (DB, cartelle)

---

## 4. Flusso End-to-End Reale: REGISTRAZIONE

```
USER INTERFACE
    ↓
GUIRegistrazioneView.onShow()
├─ Carica /fxml/registrazione.fxml
├─ Mostra form: Nome, Cognome, Email, Password
└─ Attende input utente
    ↓
USER INSERISCE DATI E CLICCA "REGISTRATI"
    ↓
RegistrazioneFXMLController.onRegistrazioneButtonClick()
├─ Legge campi: nome, cognome, email, password
├─ Chiama graphicController.inviaDatiRegistrazione(...)
    ↓
GUIGraphicControllerRegistrazione.inviaDatiRegistrazione()
├─ Crea DatiRegistrazioneBean bean = buildRegistrazioneBean(...)
├─ Ottiene CtrlRegistrazione logicCtrl = logicController()
├─ Chiama EsitoOperazioneBean esito = logicCtrl.registraNuovoUtente(bean)
    ↓
LogicControllerRegistrazione.registraNuovoUtente(DatiRegistrazioneBean)
├─ Validazione 1: Dati non null → if (!isValid(datiInput)) throw InvalidRegistrationDataException
├─ Validazione 2: Password >= 6 char → if (password.length() < 6) throw PasswordTooShortException
├─ Validazione 3: Email formato valido → if (!isValidEmailFormat(email)) throw InvalidEmailFormatException
├─ Normalizzazione: String emailNorm = normalizeEmail(email)
├─ Controllo unicità: GeneralUser existing = userDAO().findByEmail(emailNorm)
│   ↓ (se esiste)
│   throw EmailAlreadyExistsException
│
├─ Creazione Entity: UtenteFinale nuovo = new UtenteFinale()
│   ├─ nuovo.setNome(datiInput.getNome())
│   ├─ nuovo.setCognome(datiInput.getCognome())
│   ├─ nuovo.setEmail(emailNorm)
│   ├─ nuovo.setPassword(datiInput.getPassword())  // NON hashata (problema!)
│   ├─ nuovo.setStatoAccount(StatoAccount.DA_CONFERMARE)
│   └─ nuovo.setRuolo(Ruolo.UTENTE)
│
├─ Persistenza: userDAO().store(nuovo)
│   ↓ (dipendendo da DAOFactory.getInstance())
│   ├─ Se DBMS: DbmsGeneralUserDAO.store() → INSERT INTO utenti VALUES (...)
│   ├─ Se FILE_SYSTEM: FileSystemGeneralUserDAO.store() → Salva in filesystem/utenti.json
│   └─ Se IN_MEMORY: MemoryGeneralUserDAO.store() → Non persiste (solo cache)
│
├─ Logging: logDAO().store(new SystemLog(...))
│   ├─ TipoOperazione: REGISTRAZIONE_ACCOUNT
│   ├─ Descrizione: "Registrazione avviata; richiesta conferma inviata"
│   └─ Salva tramite DAO
│
├─ Notifica: inviaNotificaConfermaRegistrazione(toBean(nuovo))
│   ↓
│   GestioneNotificaRegistrazione notiService = ServiceFactory.getNotificaRegistrazioneService()
│   ├─ notiService.inviaConfermaRegistrazione(UtenteBean bean)
│   └─ EmailNotificationService.sendNotification(email, subject, body)
│       └─ Jakarta Mail via SMTP → Gmail invia email
│
└─ Risposta: return new EsitoOperazioneBean(true, "Registrazione avviata...")
    ↓
GUIGraphicControllerRegistrazione cattura esito
├─ if (esito.isSuccesso()) {
│   ├─ navigator.goTo(ROUTE_LOGIN, null)  // Naviga al login
│   └─ GUIGraphicControllerNavigation.goTo() pubblica LoginView
│
├─ catch (PasswordTooShortException e) {
│   ├─ showError(e.getMessage())
│   └─ navigator.goTo(ROUTE_REGISTRAZIONE, Map.of("error", msg))  // Ripubblica form con errore
│
├─ catch (EmailAlreadyExistsException e) { /* idem */ }
├─ catch (InvalidEmailFormatException e) { /* idem */ }
└─ catch (RegistrationException e) { /* errore generico */ }
    ↓
Navigator.goTo(ROUTE_REGISTRAZIONE, params)
├─ Salva ROUTE_LOGIN in history
├─ Carica GUIRegistrazioneView dalla map routes
├─ Chiama view.onShow(params)
│   └─ Se params contiene "error" → mostra errore nel form
├─ GuiLauncher.setRoot(root) → aggiorna stage JavaFX
    ↓
USER VEDE DI NUOVO FORM REGISTRAZIONE CON ERRORE (o login se successo)
```

---

## 5. Dipendenze e Direzione del Flusso

### **Principio: Dependency Inversion**

```
             Layer 1 (View)
                 ↑
                 | dipende da (interfaccia)
                 |
         Layer 2 (GraphicController)
                 ↑
                 | dipende da (interfaccia)
                 |
      Layer 3 (Navigator)
                 ↑
                 | dipende da (interfaccia)
                 |
         Layer 4 (LogicController)
                 ↑
                 | dipende da (interfaccia)
                 |
      Layer 5 (Service)
                 ↑
                 | dipende da (interfaccia)
                 |
             Layer 6 (DAO)
                 ↑
                 | accede a
                 |
      Layer 7 (Entity)
```

### **Implementazione concreta del DIP**:

#### ✅ **Fatto BENE - DAO via Factory**:
```java
// LogicControllerRegistrazione.java
private GeneralUserDAO userDAO() {
    return DAOFactory.getInstance().getGeneralUserDAO();  // Torna interfaccia
}

// A runtime, DAOFactory.getInstance() torna:
// - DbmsGeneralUserDAO (se DBMS)
// - FileSystemGeneralUserDAO (se FILE_SYSTEM)
// - MemoryGeneralUserDAO (se IN_MEMORY)
// Il LogicController NON sa quale!
```

#### ✅ **Fatto BENE - Service via Factory**:
```java
// LogicControllerRegistrazione.java
private GestioneNotificaRegistrazione notiCtrl() {
    return ServiceFactory.getNotificaRegistrazioneService();  // Torna interfaccia
}
```

#### ✅ **Fatto BENE - GraphicController via Factory**:
```java
// View
GraphicLoginController ctrl = FrontendControllerFactory.getInstance().createLoginController();
// Torna GUIGraphicLoginController o CLIGraphicLoginController
// View non dipende da impl concreta
```

#### ✅ **Fatto BENE - Navigator via Interfaccia**:
```java
// GraphicController
private GraphicControllerNavigation navigator;
navigator.goTo(ROUTE_LOGIN, null);  // Interfaccia
// Impl concreta (GUIGraphicControllerNavigation) fornita dal costruttore
```

#### ⚠️ **Fatto MALE - GraphicController istanziati con `new`**:
```java
// Alcuni controller usano (ANTIPATTERN):
CtrlRegistrazione logicCtrl = new LogicControllerRegistrazione();

// Dovrebbe essere:
CtrlRegistrazione logicCtrl = LogicControllerFactory.getRegistrazioneController();
```

#### ❌ **Violazione DIP - Dipendenza Circolare Potenziale**:
```
GUIRegistrazioneView 
    ↓ dipende da
GUIGraphicControllerRegistrazione
    ↓ dipende da
LogicControllerFactory
    ↓ dipende da
LogicControllerRegistrazione
    ↓ dipende da
DAOFactory
    ↓ dipende da
... (torna su View?)
```

**Verdict**: Non c'è ciclo, ma l'architettura potrebbe beneficiare di **Dependency Injection container** (Spring, Guice, Dagger) invece di factory statiche.

---

## 6. Pattern Effettivamente Usati

### ✅ **1. Factory Pattern** (Reale)

**LogicControllerFactory**:
```java
public final class LogicControllerFactory {
    public static CtrlAccesso getAccessoController() {
        return new LogicControllerGestioneAccesso();
    }
    public static CtrlRegistrazione getRegistrazioneController() {
        return new LogicControllerRegistrazione();
    }
    // ...
}
```
- **Usato per**: Creare LogicController senza diretto new
- **Beneficio**: Cambio impl futura easy (intercettare logica di creazione)

**DAOFactory** (Abstract Factory):
```java
public abstract class DAOFactory {
    public static synchronized void initialize(PersistencyProvider p, Path root) {
        instance = switch (p) {
            case DBMS        -> new DbmsDAOFactory();
            case FILE_SYSTEM -> new FileSystemDAOFactory();
            case IN_MEMORY   -> new MemoryDAOFactory();
        };
    }
}
```
- **Usato per**: Selezionare famiglia di DAO (DBMS vs FileSystem vs In-Memory)
- **Beneficio**: Multi-backend senza cambiare LogicController

**FrontendControllerFactory** (Abstract Factory):
```java
public abstract class FrontendControllerFactory {
    public static FrontendControllerFactory getInstance() {
        return switch (provider) {
            case CLI -> new CLIFrontendControllerFactory();
            case GUI -> new GUIFrontendControllerFactory();
        };
    }
}
```
- **Usato per**: Selezionare famiglia GraphicController (GUI vs CLI)
- **Beneficio**: Multi-frontend senza cambiare logica

**ServiceFactory**:
```java
public class ServiceFactory {
    public static GestionePagamentoPrenotazione getPagamentoPrenotazioneService() {
        return new LogicControllerGestionePagamento();
    }
    // ...
}
```
- **Usato per**: Creare Service Controller
- **Beneficio**: Centralizzare istanziazione

---

### ✅ **2. Navigator Pattern** (Custom, Reale)

```java
public interface GraphicControllerNavigation {
    void goTo(String route, Map<String, Object> params);
    void back();
}

public abstract class AbstractGraphicControllerNavigation {
    protected Map<String, NavigableController> routes;
    protected Deque<String> history;
    protected String currentRoute;
    
    public void goTo(String route, Map<String, Object> params) {
        // Push current route
        if (currentRoute != null) history.push(currentRoute);
        
        // Load controller
        NavigableController controller = routes.get(route);
        
        // Show view
        controller.onShow(params);
        
        // Update current
        currentRoute = route;
    }
}
```

- **Usato per**: Routing tra schermate, history navigation
- **Beneficio**: Disaccoppiamento GraphicController da View concrete

---

### ✅ **3. Strategy Pattern** (Reale, Backend-driven)

```
DAOFactory
├─ DbmsDAOFactory (Strategy: DBMS)
├─ FileSystemDAOFactory (Strategy: FileSystem)
└─ MemoryDAOFactory (Strategy: In-Memory)

// Switchato a runtime
DAOFactory.initialize(PersistencyProvider.DBMS, root);
// Ora tutti i DAO.getInstance() tornano impl DBMS
```

- **Usato per**: Scegliere backend persistenza
- **Beneficio**: Cambio backend a bootstrap time

---

### ✅ **4. Facade Pattern** (Reale)

**LogicController come Facade**:
```java
// Layer grafico chiama solo questo:
EsitoOperazioneBean esito = logicController.registraNuovoUtente(bean);

// Ma internamente il LogicController:
// - Valida dati
// - Crea entity
// - Salva DAO
// - Registra log
// - Invia email
// - Genera fattura (se necessario)
// - Applica penalità (se necessario)
```

- **Usato per**: Semplificare interfaccia verso layer grafico
- **Beneficio**: GraphicController vede API semplice, non complessità interna

---

### ✅ **5. Adapter/Converter Pattern** (Reale, Entity↔Bean)

```java
// LogicControllerRegistrazione
private UtenteBean toBean(UtenteFinale entity) {
    UtenteBean bean = new UtenteBean();
    bean.setIdUtente(entity.getIdUtente());
    bean.setNome(entity.getNome());
    bean.setCognome(entity.getCognome());
    bean.setEmail(entity.getEmail());
    bean.setRuolo(entity.getRuolo());
    return bean;
}

// Inverso
private UtenteFinale toEntity(UtenteBean bean) {
    UtenteFinale entity = new UtenteFinale();
    // ...
    return entity;
}
```

- **Usato per**: Convertire Entity (dominio) ↔ Bean (DTO)
- **Beneficio**: Entity non esposte a View, decoupling
- **Problema**: Conversione manuale, no mapper centralizzato (potrebbe usare MapStruct)

---

### ❌ **Pattern NON Usati (ma potrebbero beneficiarne)**:

- **Decorator**: Nessun decorator per aggiungere comportamenti dinamici
- **Observer**: Nessun sistema di event/listener per notifiche UI
- **Command**: Nessun pattern Command per undo/redo
- **Saga**: LogicControllerPrenotazioneCampo coordina 4 servizi sequenziali (potrebbe beneficiare di Saga pattern)
- **Template Method**: Logiche comuni GraphicController duplicate (potrebbe usare TM meglio)
- **Dependency Injection**: Nessun DI container (solo factory statiche)

---

## 7. Ruolo dei Bean nella Codebase

### **Dove vengono creati:**

1. **View FXML** → `RegistrazioneFXMLController`:
   ```java
   @FXML
   private void onRegistrazioneButtonClick() {
       String nome = nomeField.getText();
       String cognome = cognomeField.getText();
       String email = emailField.getText();
       String password = passwordField.getText();
       
       // Bean NON creato qui
       graphicController.inviaDatiRegistrazione(nome, cognome, email, password);
   }
   ```

2. **GraphicController** → `GUIGraphicControllerRegistrazione`:
   ```java
   private DatiRegistrazioneBean buildRegistrazioneBean(String nome, String cognome, String email, String password) {
       DatiRegistrazioneBean bean = new DatiRegistrazioneBean();
       bean.setNome(nome);
       bean.setCognome(cognome);
       bean.setEmail(email);
       bean.setPassword(password);
       return bean;
   }
   ```

3. **LogicController** → `LogicControllerRegistrazione`:
   ```java
   public EsitoOperazioneBean registraNuovoUtente(DatiRegistrazioneBean datiInput) {
       // Riceve Bean in input
       // Crea Entity internamente
       UtenteFinale nuovo = new UtenteFinale();
       // ...
       
       // Torna Bean in output
       return new EsitoOperazioneBean(true, "Successo");
   }
   ```

### **Come vengono usati:**

| Bean | Creato da | Passato a | Ruolo |
|------|-----------|-----------|-------|
| `DatiRegistrazioneBean` | GraphicController | LogicController | Input view → logica |
| `EsitoOperazioneBean` | LogicController | GraphicController | Output logica → view |
| `UtenteBean` | LogicController (convert Entity) | View | Display utente |
| `SessioneUtenteBean` | LogicControllerAccesso | View | Stato sessione globale |
| `RiepilogoPrenotazioneBean` | LogicControllerPrenotazione | View | Summary prenotazione |
| `DatiPagamentoBean` | GraphicControllerPrenotazione | Service | Input pagamento |
| `DatiLoginBean` | GraphicControllerLogin | LogicController | Credenziali login |

### **Come disaccoppiano View e Model:**

```
View conosce:           View NON conosce:
├─ DatiXBean (DTO)      ├─ Entity (Prenotazione, Utente)
├─ EsitoXBean           ├─ DAO
├─ SessioneUtenteBean   ├─ LogicControllerImpl concreta
└─ Bean display         └─ Enum (StatoAccount, Ruolo) - solo via Bean
```

**Beneficio**: Se Entity cambia struttura, View non si rompe (cambia solo il LogicController che fa conversione).

---

## 8. Anatomia delle Responsabilità (Single Responsibility Principle)

### ✅ **Fatto BENE**:

| Classe | Responsabilità | Violazioni? |
|--------|----------------|-----------|
| `LogicControllerRegistrazione` | Validazione + creazione utente + persistenza + log + email | NO - ogni passo è coeso |
| `LogicControllerGestionePagamento` | Gestione pagamenti (prenotazione, penalità, rimborso, disdetta) | ⚠️ Si, 4 responsabilità diverse (ma raggruppate logicamente) |
| `GeneralUserDAO` | Interfaccia DAO utenti | NO - pura astrazione |
| `DbmsGeneralUserDAO` | Impl MySQL di GeneralUserDAO | NO - solo SQL |
| `FileSystemGeneralUserDAO` | Impl FileSystem di GeneralUserDAO | NO - solo JSON I/O |
| `GUIGraphicControllerRegistrazione` | Validazione UI + costruzione bean | NO - UI logic solo |
| `GUIRegistrazioneView` | Caricamento FXML + visualizzazione | NO - solo presentazione |
| `RegistrazioneFXMLController` | Binding FXML + event handling | NO - solo UI binding |

### ⚠️ **Fatto MALE**:

| Classe | Problema | Impatto |
|--------|----------|--------|
| `LogicControllerPrenotazioneCampo` | Coordina 4 servizi (Pagamento, Fattura, Notifica, Disponibilità) | Testabilità ridotta, difficile debuggare fallimenti |
| `LogicControllerHelper` | Helper statico misto (validation, normalization) | Raggruppamento per utilità, non per responsabilità |
| Nessun mapper centralizzato | Conversione Entity→Bean sparsa in ogni LogicController | Duplicazione codice, manutenzione difficile |
| `AbstractGraphicControllerRegistrazione` | Logica comune GUI/CLI ripetuta | Potrebbe usare template method meglio |

---

## 9. Problemi Architetturali Reali (Identificati dal Codice)

### 🔴 **CRITICO**

#### **1. Password NON Hashata**
```java
// LogicControllerRegistrazione.registraNuovoUtente()
nuovo.setPassword(datiInput.getPassword());  // Plain text!
```
- **Impatto**: Rischio critico sicurezza
- **Soluzione**: Hash (BCrypt, PBKDF2) prima di persistenza
- **Dove**: LogicControllerRegistrazione.registraNuovoUtente() e LogicControllerGestioneAccesso.verificaCredenziali()

---

#### **2. Coordinamento Servizi Fragile**
```java
// LogicControllerPrenotazioneCampo.nuovaPrenotazione()
1. Occupa slot (GestioneDisponibilitaPrenotazione)
2. Crea pagamento (GestionePagamentoPrenotazione)
3. Genera fattura (GestioneFatturaPrenotazione)
4. Invia notifica (GestioneNotificaPrenotazione)
```
- **Problema**: Se fattura fallisce dopo pagamento, nessun rollback
- **Soluzione**: Saga pattern per transazioni distribuite, o @Transactional se DBMS
- **Impatto**: Data inconsistency su fallimenti parziali

---

### 🟠 **MAGGIORE**

#### **3. GraphicController Istanzia LogicController con `new`**
```java
// Alcuni controller (ANTIPATTERN):
CtrlRegistrazione logicCtrl = new LogicControllerRegistrazione();

// Dovrebbe:
CtrlRegistrazione logicCtrl = LogicControllerFactory.getRegistrazioneController();
```
- **Impatto**: DIP violato, difficile testare (mock), poco estensibile
- **Soluzione**: Usare factory in tutti i GraphicController

---

#### **4. Nessun Mapper Centralizzato Entity↔Bean**
```java
// Conversione Entity → Bean sparsa:
private UtenteBean toBean(UtenteFinale entity) { /* 10 line */ }
private SessioneUtenteBean toSessioneBean(GeneralUser user) { /* 8 line */ }
private RiepilogoPrenotazioneBean toRiepilogoBean(Prenotazione p) { /* 20 line */ }
```
- **Impatto**: Duplicazione, manutenzione difficile se Entity cambia
- **Soluzione**: MapStruct, ModelMapper, o Mapper interface centralizzato

---

#### **5. Nessuna Transazionalità DB**
```java
// LogicControllerPrenotazioneCampo coordina 4 DAO call sequenziali:
1. PrenotazioneDAO.store(p)
2. PagamentoDAO.store(pag)
3. FatturaDAO.store(f)
4. LogDAO.store(log)
```
- **Problema**: Se (3) fallisce, (1) e (2) già persistiti
- **Soluzione**: @Transactional (se DBMS), o manuale rollback
- **Impatto**: Data corruption

---

#### **6. Navigator Non Inizializzato Centralmente**
```java
// Navigator deve avere map<String, NavigableController> routes
// Chi popola questa map? Non trovato nel codice
```
- **Impatto**: Routing non funziona, route non trovata
- **Soluzione**: FrontendControllerFactory dovrebbe popolare routes

---

### 🟡 **MINORE**

#### **7. Eccezioni Custom Non Gerarchiche**
```java
// Tutti extendono RegistrationException, ma:
// - PasswordTooShortException
// - EmailAlreadyExistsException
// - InvalidEmailFormatException
// Non condividono interfaccia comune per IL MESSAGGIO
```
- **Impatto**: GraphicController deve gestire ogni exception tipo
- **Soluzione**: Exception che fornisce getMessage() consistent

---

#### **8. Nessun Caching**
```java
// LogicControllerPrenotazioneCampo chiama sempre:
DAOFactory.getInstance().getCampoDAO().findAll()
```
- **Impatto**: Query ripetute per campi (lista campi cambia raramente)
- **Soluzione**: Cache decorator su DAO, o Redis

---

#### **9. Enum Limitati**
```java
public enum StatoAccount {
    ATTIVO, DA_CONFERMARE, DISATTIVATO
}
```
- **Problema**: No statuses per admin action (es. BANNATO, SOSPESO)
- **Soluzione**: Aggiungere stati se necessario

---

#### **10. Mancanza Test Integrazione**
```
src/test/java/ contiene solo:
- LogicControllerApplicaPenalitaTest
- LogicControllerGestioneAccountTest
- MockitoTest

Manca:
- Test DAO DBMS reale (H2)
- Test flussi end-to-end (login → prenotazione → pagamento)
- Test fallimenti backend polimorsfico
```
- **Impatto**: Rischi non colti in build
- **Soluzione**: TestContainers (MySQL), test end-to-end

---

## 10. Riepilogo Architetturale

### **Architettura Reale: MVC Esteso a 9 Layer**

```
View (FXML/Console)
    ↓ NavigableController
GraphicController (validazione UI)
    ↓ Navigator
LogicController (dominio)
    ↓ ServiceFactory
Service (operazioni secondarie)
    ↓ DAOFactory
DAO (astrazione persistenza)
    ↓
Entity (dominio persistente)
```

### **Forze (Cosa Funziona Bene)**:
- ✅ **Multi-backend**: DBMS, FileSystem, In-Memory selezionabili a bootstrap
- ✅ **Multi-frontend**: GUI JavaFX, CLI console selezionabili
- ✅ **DIP**: Dipendenze da interfacce, non impl
- ✅ **Testabilità**: LogicController stateless, mockabile
- ✅ **Disaccoppiamento**: View non conosce Entity (usa Bean)
- ✅ **Factory**: 4 factory polimorfiche (DAO, Service, LogicController, FrontendController)

### **Debolezze (Cosa Migliorare)**:
- ❌ **Password plain text** (CRITICO)
- ❌ **Nessuna transazionalità** su coordinamento servizi
- ❌ **GraphicController usa `new` anziché factory**
- ❌ **Nessun mapper centralizzato** Entity↔Bean
- ❌ **Navigator non inizializzato centralmente**
- ❌ **Mancano test integrazione**

### **Pattern Usati**:
- Factory (LogicControllerFactory, DAOFactory, FrontendControllerFactory, ServiceFactory)
- Abstract Factory (DAOFactory per backend polimorsfico)
- Strategy (backend selection)
- Facade (LogicController)
- Navigator (routing custom)

### **Standard Applicati**:
- ✅ SOLID (DIP, ISP, SRP - con poche violazioni)
- ✅ Clean Architecture (layer ben separati)
- ✅ TDD (test con Mockito presenti)
- ⚠️ Partial: Nessun DI container (solo factory statiche)

---

## 📊 CONCLUSIONE PER DIAGRAMMA MVC

Questo sistema **non è un MVC classico**, ma un **MVC Esteso Polimorfico**:

```
┌─────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER (View)                               │
│ ├─ GUI: JavaFX + FXML (GUIRegistrazioneView)           │
│ └─ CLI: Console Menu (CLIRegistrazioneView)            │
│ Selezionabili a bootstrap time                          │
└─────────────────────────────────────────────────────────┘
                         ↑
                    Navigator
                    (routing)
                         ↑
┌─────────────────────────────────────────────────────────┐
│ CONTROL LAYER (GraphicController)                       │
│ ├─ GUI: GUIGraphicControllerRegistrazione               │
│ ├─ CLI: CLIGraphicControllerRegistrazione               │
│ Validazione input, delegazione a logica                 │
└─────────────────────────────────────────────────────────┘
                         ↑
            LogicControllerFactory
                         ↑
┌─────────────────────────────────────────────────────────┐
│ BUSINESS LOGIC LAYER (LogicController)                  │
│ ├─ LogicControllerRegistrazione                         │
│ ├─ LogicControllerPrenotazioneCampo                     │
│ ├─ LogicControllerGestioneAccesso                       │
│ Dominio, validazione complessa, orchestrazione          │
└─────────────────────────────────────────────────────────┘
                         ↑
              ServiceFactory
                         ↑
┌─────────────────────────────────────────────────────────┐
│ SERVICE LAYER (Service)                                 │
│ ├─ GestionePagamento, Fattura, Notifica                │
│ Operazioni secondarie di dominio                        │
└─────────────────────────────────────────────────────────┘
                         ↑
               DAOFactory
         (DBMS | FileSystem | In-Memory)
                         ↑
┌─────────────────────────────────────────────────────────┐
│ PERSISTENCE LAYER (DAO)                                 │
│ ├─ GeneralUserDAO, PrenotazioneDAO, PagamentoDAO       │
│ Astrazione database, no SQL diretto da logica           │
└─────────────────────────────────────────────────────────┘
                         ↑
┌─────────────────────────────────────────────────────────┐
│ MODEL LAYER (Entity + Enum)                             │
│ ├─ GeneralUser, UtenteFinale, Gestore                   │
│ ├─ Prenotazione, Pagamento, Fattura, Penalita          │
│ ├─ Ruolo, StatoAccount, StatoPrenotazione              │
│ Dominio persistente                                     │
└─────────────────────────────────────────────────────────┘
```

Questo è il **design diagram MVC concreto** che puoi usare per la documentazione di progetto.
