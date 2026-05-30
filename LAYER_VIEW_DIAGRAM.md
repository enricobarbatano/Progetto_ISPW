# 🎨 LAYER VIEW - ARCHITETTURA COMPLETA

## 1. DIAGRAMMA GERARCHICO DEL VIEW LAYER

```
┌─────────────────────────────────────────────────────────────────┐
│                     VIEW LAYER (Layer 1)                         │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              GenericView (Interface)                      │   │
│  │  - onShow(Map<String,Object> params)                      │   │
│  │  - onHide()                                               │   │
│  │  - readSession() / readError() / readSuccess()            │   │
│  └────────────────┬─────────────────────────────────────────┘   │
│                   │                                              │
│  ┌────────────────▼─────────────────────────────────────────┐   │
│  │           GenericViewBase (Abstract)                      │   │
│  │  [Implementa GenericView]                                 │   │
│  │  - sessione: SessioneUtenteBean                           │   │
│  │  - lastParams: Map<String, Object>                        │   │
│  │  - getSessione() / getLastParams()                        │   │
│  │  - getLastError() / getLastSuccess()                      │   │
│  └────────────────┬─────────────────────────────────────────┘   │
│                   │                                              │
│        ┌──────────┴──────────┐                                  │
│        │                     │                                  │
│  ┌─────▼────────────────┐ ┌─▼──────────────────────┐           │
│  │  GenericViewGUI      │ │  GenericViewCLI        │           │
│  │  (Abstract)          │ │  (Abstract)            │           │
│  │ [Specializzazione    │ │ [Specializzazione      │           │
│  │  per frontend GUI]   │ │  per frontend CLI]     │           │
│  └─────┬────────────────┘ └─┬──────────────────────┘           │
│        │                     │                                  │
│        │                     │                                  │
│  ╔═════▼════════════════╗ ╔═▼═══════════════════════════════╗  │
│  ║ XView Interface      ║ ║ XView Interface                 ║  │
│  ║ (Marker per use case)║ ║ (Marker per use case)           ║  │
│  ║                      ║ ║                                 ║  │
│  ║ • ViewLogin          ║ ║ • ViewLogin                     ║  │
│  ║ • ViewRegistrazione  ║ ║ • ViewRegistrazione             ║  │
│  ║ • ViewPrenotazione   ║ ║ • ViewPrenotazione              ║  │
│  ║ • ViewGestioneAcct   ║ ║ • ViewGestioneAcct              ║  │
│  ║ • ViewDisdetta       ║ ║ • ViewDisdetta                  ║  │
│  ║ • ViewRegole         ║ ║ • ViewRegole                    ║  │
│  ║ • ViewLog            ║ ║ • ViewLog                       ║  │
│  ║ • ViewPenalita       ║ ║ • ViewPenalita                  ║  │
│  ║                      ║ ║                                 ║  │
│  ║ extends GenericView  ║ ║ extends GenericView             ║  │
│  ╚═════▲════════════════╝ ╚═▲═══════════════════════════════╝  │
│        │                     │                                  │
│        │                     │                                  │
│  ┌─────┴──────────────────┐  │                                 │
│  │ GUIXView (Concrete)    │  │  ┌──────────────────────────┐   │
│  │                        │  │  │ CLIXView (Concrete)     │   │
│  │ • GUILoginView         │  │  │                         │   │
│  │ • GUIRegistrazioneView │  │  │ • CLILoginView          │   │
│  │ • GUIPrenotazioneView  │  │  │ • CLIRegistrazioneView  │   │
│  │ • GUIAccountView       │  │  │ • CLIPrenotazioneView   │   │
│  │ • GUIDisdettaView      │  │  │ • CLIAccountView        │   │
│  │ • GUIRegoleView        │  │  │ • CLIDisdettaView       │   │
│  │ • GUILogView           │  │  │ • CLIRegoleView         │   │
│  │ • GUIPenalitaView      │  │  │ • CLILogView            │   │
│  │                        │  │  │ • CLIPenalitaView       │   │
│  │ EXTENDS GenericViewGUI │  │  │                         │   │
│  │ IMPLEMENTS ViewX       │  │  │ EXTENDS GenericViewCLI  │   │
│  │ IMPLEMENTS NavController│  │  │ IMPLEMENTS ViewX       │   │
│  │                        │  │  │ IMPLEMENTS NavController│   │
│  └────────────────────────┘  │  └──────────────────────────┘   │
│                               │                                  │
│  Runtime Instantiation:        │  Runtime Instantiation:        │
│  ┌──────────────────────────┐ │  ┌──────────────────────────┐   │
│  │ GUILoginView --CARICA--> │ │  │ CLILoginView             │   │
│  │ /fxml/login.fxml         │ │  │ + Scanner input          │   │
│  │        |                 │ │  │ + System.out output      │   │
│  │        v                 │ │  └──────────────────────────┘   │
│  │ LoginFXMLController      │ │                                  │
│  │ (managed by FXML Loader) │ │                                  │
│  │                          │ │                                  │
│  │ NON direttamente         │ │                                  │
│  │ istanziato via new()     │ │                                  │
│  └──────────────────────────┘ │                                  │
│                                │                                  │
└────────────────────────────────┴──────────────────────────────────┘
```

---

## 2. PATTERN DI ISTANZIAZIONE A RUNTIME

### 🔵 PATTERN GUI (JavaFX + FXML)

```
┌─────────────────────────────────────────────────────────┐
│ Bootstrap/AppBootstrapper                               │
│                                                         │
│ 1) Legge configurazione: frontend="GUI"                │
│ 2) Instanzia GUIGraphicControllerFactory               │
│ 3) Factory crea: GUIGraphicLoginController, ...        │
│ 4) Instanzia ViewFactory per GUI                       │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ ViewFactory.createView(ViewType.LOGIN)                 │
│                                                         │
│ new GUILoginView(graphicController)  ← SINGLETON VIEW  │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼ Navigator.navigate(ViewType.LOGIN, params)
┌─────────────────────────────────────────────────────────┐
│ GUILoginView.onShow(Map<String, Object> params)        │
│                                                         │
│ 1) Carica /fxml/login.fxml tramite FXMLLoader          │
│ 2) FXMLLoader instanzia automaticamente                │
│    LoginFXMLController (managed by FXML runtime)       │
│ 3) setController(loginFXMLController)                  │
│ 4) Inizializza controller FXML con graphic controller  │
│ 5) GuiLauncher.setRoot(parent)  ← aggiorna stage      │
│ 6) LoginFXMLController gestisce eventi utente          │
└─────────────────────────────────────────────────────────┘
```

### 🔴 PATTERN CLI (Console)

```
┌─────────────────────────────────────────────────────────┐
│ Bootstrap/AppBootstrapper                               │
│                                                         │
│ 1) Legge configurazione: frontend="CLI"                │
│ 2) Instanzia CLIGraphicControllerFactory               │
│ 3) Factory crea: CLIGraphicLoginController, ...        │
│ 4) Instanzia ViewFactory per CLI                       │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ ViewFactory.createView(ViewType.LOGIN)                 │
│                                                         │
│ new CLILoginView(graphicController)  ← SINGLETON VIEW  │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼ Navigator.navigate(ViewType.LOGIN, params)
┌─────────────────────────────────────────────────────────┐
│ CLILoginView.onShow(Map<String, Object> params)        │
│                                                         │
│ 1) Stampa menu/prompt su System.out                    │
│ 2) Legge input da Scanner (System.in)                  │
│ 3) Raccoglie dati in variabili locali                  │
│ 4) Chiama controller.metodo(dati)                      │
│ 5) Navigator ritorna con i risultati                   │
│ 6) CLILoginView.onShow() della schermata successiva    │
└─────────────────────────────────────────────────────────┘
```

---

## 3. TABELLA COMPARATIVA: GUI vs CLI

| Aspetto | **GUI (JavaFX)** | **CLI (Console)** |
|---------|------------------|-------------------|
| **Base class** | `GenericViewGUI` | `GenericViewCLI` |
| **Layout** | FXML file (`/fxml/X.fxml`) | System.out.println() |
| **Input** | FXML Controller + JavaFX events | Scanner + nextLine() |
| **Output** | Stage/Scene update | Console output |
| **FXML Controller** | Sì, managed by FXML Loader | No (non necessario) |
| **Istanziazione** | `new GUIXView()` + FXMLLoader | `new CLIXView()` |
| **Threading** | JavaFX Application Thread | Console/Blocking |

---

## 4. STRUTTURA CONCRETA DEL CODICE

### Directory `/view/`

```
view/
├── interfaces/
│   ├── GenericView.java                   ← CONTRATTO BASE
│   ├── ViewLogin.java                     ← MARKER (extends GenericView)
│   ├── ViewRegistrazione.java             ← MARKER (extends GenericView)
│   ├── ViewPrenotazione.java              ← MARKER (extends GenericView)
│   ├── ViewGestioneAccount.java           ← MARKER (extends GenericView)
│   ├── ViewDisdettaPrenotazione.java      ← MARKER (extends GenericView)
│   ├── ViewGestioneRegole.java            ← MARKER (extends GenericView)
│   ├── ViewLog.java                       ← MARKER (extends GenericView)
│   └── ViewGestionePenalita.java          ← MARKER (extends GenericView)
│
├── common/
│   └── GenericViewBase.java               ← IMPL DI GENERICVIEW
│
├── gui/
│   ├── GenericViewGUI.java                ← SPECIALIZZAZIONE GUI
│   ├── GUILoginView.java                  ← CONCRETA (login)
│   ├── GUIRegistrazioneView.java          ← CONCRETA
│   ├── GUIPrenotazioneView.java           ← CONCRETA
│   ├── GUIAccountView.java                ← CONCRETA
│   ├── GUIDisdettaView.java               ← CONCRETA
│   ├── GUIRegoleView.java                 ← CONCRETA
│   ├── GUILogView.java                    ← CONCRETA
│   ├── GUIPenalitaView.java               ← CONCRETA
│   ├── GuiLauncher.java                   ← UTILITY (stage management)
│   ├── GuiViewUtils.java                  ← UTILITY COMUNI GUI
│   │
│   └── fxml/
│       ├── login.fxml                     ← LAYOUT XML
│       ├── LoginFXMLController.java       ← MANAGED BY FXML (NON istanziato manualmente)
│       ├── registrazione.fxml
│       ├── RegistrazioneFXMLController.java
│       ├── prenotazione.fxml
│       ├── PrenotazioneFXMLController.java
│       ├── account.fxml
│       ├── AccountFXMLController.java
│       ├── disdetta.fxml
│       ├── DisdettaFXMLController.java
│       ├── regole.fxml
│       ├── RegoleFXMLController.java
│       ├── log.fxml
│       ├── LogFXMLController.java
│       ├── penalita.fxml
│       └── PenalitaFXMLController.java
│
├── cli/
│   ├── GenericViewCLI.java                ← SPECIALIZZAZIONE CLI
│   ├── CLILoginView.java                  ← CONCRETA (login)
│   ├── CLIRegistrazioneView.java          ← CONCRETA
│   ├── CLIPrenotazioneView.java           ← CONCRETA
│   ├── CLIAccountView.java                ← CONCRETA
│   ├── CLIDisdettaView.java               ← CONCRETA
│   ├── CLIRegoleView.java                 ← CONCRETA
│   ├── CLILogView.java                    ← CONCRETA
│   ├── CLIPenalitaView.java               ← CONCRETA
│   ├── CliViewUtils.java                  ← UTILITY COMUNI CLI
│   │
│   └── [console helpers]
│       ├── ConsoleLoginView.java          ← Helper per layout login
│       ├── ConsolePagamentoView.java      ← Helper per layout pagamento
│       └── ...
│
└── shared/
    └── [componenti comuni GUI/CLI, se esistono]
```

---

## 5. DETTAGLI DELL'ISTANZIAZIONE

### A) **Istanziazione a Bootstrap Time** (Selezione Backend/Frontend)

```java
// AppBootstrapper.java (pseudocode)

// 1. Seleziona FRONTEND
String frontend = config.getFrontend();  // "GUI" o "CLI"

// 2. Istanzia Factory appropriata
if (frontend.equals("GUI")) {
    viewFactory = new GUIViewFactory();  // crea GUIXView
} else if (frontend.equals("CLI")) {
    viewFactory = new CLIViewFactory();  // crea CLIXView
}

// 3. Istanzia tutte le view (SINGLETON, non ricreate)
Map<ViewType, GenericView> views = new HashMap<>();
views.put(ViewType.LOGIN, viewFactory.createView(ViewType.LOGIN));
views.put(ViewType.HOME, viewFactory.createView(ViewType.HOME));
// ...

// NON MODIFICABILE A RUNTIME
// Una volta scelta GUI, NON puoi passare a CLI senza riavviare
```

### B) **Instanziazione di GUIXView + FXML Controller**

```java
// GUILoginView (example)

public class GUILoginView extends GenericViewGUI 
        implements ViewLogin, NavigableController {
    
    private final GUIGraphicLoginController controller;
    private LoginFXMLController fxmlController;  // managed by FXML Loader
    
    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
        // NON carica FXML qui! Carica solo a onShow()
    }
    
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        
        try {
            // 1. CARICA FXML e istanzia LoginFXMLController
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();  // ← FXML Loader crea LoginFXMLController
            
            // 2. RECUPERA il controller FXML (creato automaticamente)
            this.fxmlController = loader.getController();
            
            // 3. INIZIALIZZA il controller FXML
            fxmlController.setGraphicController(controller);
            fxmlController.initWithParams(params);
            
            // 4. AGGIORNA stage
            GuiLauncher.setRoot(root);
            
        } catch (IOException e) {
            // gestione errore
        }
    }
}
```

### C) **Istanziazione di CLIXView (NO FXML)**

```java
// CLILoginView (example)

public class CLILoginView extends GenericViewCLI 
        implements ViewLogin, NavigableController {
    
    private final Scanner in = new Scanner(System.in);
    private final CLIGraphicLoginController controller;
    
    public CLILoginView(CLIGraphicLoginController controller) {
        this.controller = controller;
    }
    
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        
        // Stampa menu
        System.out.println("=== LOGIN ===");
        System.out.print("Email: ");
        String email = in.nextLine();
        
        System.out.print("Password: ");
        String password = in.nextLine();
        
        // Chiama controller
        controller.login(email, password);
        // Navigator gestisce il risultato e naviga alla prossima view
    }
}
```

---

## 6. DIFFERENZE CRUCIALI RISPETTO A MVC "CLASSICO"

| Aspetto | **MVC Classico** | **Questo Progetto (ISPW)** |
|---------|------------------|--------------------------|
| **View singola** | Una vista (JSP, Blade, etc) | Due viste (GUI + CLI) |
| **FXML Controller** | Part of View? | Sì, ma instanziato da FXML Loader |
| **Selezione a runtime** | Spesso sì | NO! Scelto a bootstrap, fisso |
| **Serializzazione** | Bean per request/response | DTO Beans + Map<String, Object> |

---

## 7. LEGENDA SIMBOLI NEL DIAGRAMMA

- **Interface** `XInterface` = contratto
- **Abstract class** `[Classe Astratta]` = logica comune
- **Concrete class** `ClasseConcreta` = implementazione finale
- **→** = implementa/estende
- **--CARICA-->** = caricamento a runtime
- **SINGLETON** = istanziato una volta a bootstrap

---

## 8. RESPONSABILITÀ DELLA VIEW (SRP)

✅ **DEVE FARE:**
- Carica layout (FXML per GUI, stampa per CLI)
- Raccoglie input utente
- Mostra risultati (success, error)
- Chiama il GraphicController
- Implementa NavigableController (getRouteName, onShow, onHide)

❌ **NON DEVE FARE:**
- Logica di dominio
- Accesso a DAO
- Creazione di Bean (se non guidata da controller)
- Conversione Entity ↔ DTO
- Gestione del routing (delegato a Navigator)
- Condizionali complessi sulla business logic

---

## 9. STRUTTURA DELL'INTERFACCIA MARKER (XView)

```java
// Esempio: ViewLogin.java
package com.ispw.view.interfaces;

public interface ViewLogin extends GenericView {
    
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: marker interface per view di login.
    // A2) IO: eredita il contratto di GenericView.
    
    // SEZIONE LOGICA
    // Legenda logica: nessun metodo aggiuntivo.
    
    // Questa interface serve SOLO per il type checking a compile-time.
    // Le implementazioni concrete (GUILoginView, CLILoginView)
    // ereditano il contratto funzionale da GenericView.
}
```

Quindi: **XView è una marker interface, NON aggiunge metodi**.

---

## 10. FLUSSO COMPLETO DI NAVIGAZIONE

```
┌─────────────────────────────────────────────────────────┐
│ 1. Navigator.navigate(ViewType.LOGIN, params)          │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 2. ViewFactory.getView(ViewType.LOGIN)                  │
│    → Ritorna GUILoginView o CLILoginView (pre-cached)  │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 3. view.onShow(params)                                  │
│    - GUI: carica FXML + LoginFXMLController             │
│    - CLI: stampa menu e legge input                     │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 4. User interaction                                     │
│    - GUI: click su button → LoginFXMLController         │
│    - CLI: digita scelta → CLILoginView legge input      │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 5. view.controller.metodo(dati)                         │
│    Chiama GraphicLoginController                        │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 6. GraphicController elabora e chiama LogicController  │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 7. LogicController chiama DAO e ritorna EsitoBean      │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 8. GraphicController ritorna a Navigator con risultato │
│    (EsitoBean + params)                                 │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│ 9. Navigator.navigate(ViewType.HOME, resultParams)     │
│    Ciclo ricomincia...                                  │
└─────────────────────────────────────────────────────────┘
```

---

## 11. SUMMARY: CHECKLIST PER IL DIAGRAMMA MVC

✅ **MODEL**: Entity + DAO + Enums
✅ **VIEW (Layer 1)**:
   - GenericView (interface)
   - GenericViewBase (abstract base)
   - GenericViewGUI + GenericViewCLI (specialized bases)
   - XView interfaces (marker, una per use case)
   - GUIXView + CLIXView (concrete implementations)
   - FXML Controllers (per GUI, managed by FXML Loader)
✅ **CONTROLLER (Layer 2-5)**:
   - GraphicController (UI logic)
   - LogicController (business logic)
   - Factories (polimorfiche)

✅ **ISTANZIAZIONE A RUNTIME**:
   - Selezione a **bootstrap time** (non a runtime)
   - GUIXView: instantiate once + FXML load on onShow
   - CLIXView: instantiate once + console I/O on onShow
   - FXML Controllers: managed automatically by FXML Loader (non manuale)
