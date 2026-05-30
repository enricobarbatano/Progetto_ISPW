# 🔗 PATTERN: Come le View Richiamano il Graphic Controller

## TL;DR - Risposta Diretta

**SÌ, usano Factory Method + Constructor Injection:**

```
FrontendControllerFactory (GUI o CLI)
    ├─ createXController() → GraphicControllerX
    └─ createXView()
        ├─ 1) Crea GraphicControllerX via createXController()
        ├─ 2) Passa al costruttore della View
        └─ 3) View la memorizza in private field
```

**Codice:**
```java
// Factory
public GUILoginView createLoginView() {
    if (loginView == null) {
        loginView = new GUILoginView(
            (GUIGraphicLoginController) createLoginController()
        );
    }
    return loginView;
}

// View
public class GUILoginView extends GenericViewGUI {
    private final GUIGraphicLoginController controller;  // memorizzato
    
    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;  // ← Constructor Injection
    }
    
    @Override
    public void onShow(Map<String, Object> params) {
        // Usa il controller passato al costruttore
        controller.metodo(dati);
    }
}
```

---

## PATTERN ARCHITETTURALE COMPLETO

### 1️⃣ **Factory Method Pattern** (creazione dei controllori)

```
┌──────────────────────────────────────────────────────┐
│ GUIFrontendControllerFactory                         │
│                                                      │
│ createLoginController() {                            │
│    if (loginController == null) {                    │
│        navigator = getNavigationController()         │
│        loginController = new GUIGraphicLoginController(navigator)
│        // Lazy singleton                             │
│    }                                                 │
│    return loginController                           │
│ }                                                    │
└──────────────────────────────────────────────────────┘
```

### 2️⃣ **Factory Method Pattern** (creazione delle view)

```
┌──────────────────────────────────────────────────────┐
│ GUIFrontendControllerFactory                         │
│                                                      │
│ createLoginView() {                                  │
│    if (loginView == null) {                          │
│        loginView = new GUILoginView(                 │
│            (GUIGraphicLoginController)               │
│            createLoginController()  ← COMPOSIZIONE  │
│        );                                            │
│    }                                                 │
│    return loginView  // Singleton                    │
│ }                                                    │
└──────────────────────────────────────────────────────┘
```

### 3️⃣ **Constructor Injection** (iniezione nel costruttore)

```
┌──────────────────────────────────────────────────────┐
│ GUILoginView                                         │
│                                                      │
│ private final GUIGraphicLoginController controller;  │
│                                                      │
│ public GUILoginView(                                 │
│     GUIGraphicLoginController controller  ← param   │
│ ) {                                                  │
│     this.controller = controller;  ← memorizzazione│
│ }                                                    │
│                                                      │
│ public void onShow(...) {                           │
│     controller.metodo(dati);  ← UTILIZZO            │
│ }                                                    │
└──────────────────────────────────────────────────────┘
```

---

## FLUSSO COMPLETO: BOOTSTRAP → VIEW → CONTROLLER

```
┌─────────────────────────────────────────────────────┐
│ 1. AppBootstrapper / AppConfigurator                │
│    appConfig.setFrontend("GUI")                     │
└────────────┬────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────┐
│ 2. Create Factory                                   │
│    factory = new GUIFrontendControllerFactory()    │
└────────────┬────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────┐
│ 3. Create Navigator                                 │
│    navigator = factory.createNavigationController()│
└────────────┬────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────┐
│ 4. Create All Views (cached in factory)             │
│                                                     │
│    GUILoginView = factory.createLoginView()        │
│    ├─ 4a) factory.createLoginController()           │
│    │      = new GUIGraphicLoginController(navigator)
│    └─ 4b) new GUILoginView(controller)              │
│                                                     │
│    GUIHomeView = factory.createHomeView()          │
│    ├─ new GUIHomeView(navigator)                    │
│    └─ (Home non ha controller use-case specifico)  │
│                                                     │
│    GUIRegistrazioneView = factory.createRegistrazioneView()
│    ├─ factory.createRegistrazioneController()       │
│    │  = new GUIGraphicControllerRegistrazione(...)  │
│    └─ new GUIRegistrazioneView(controller)          │
│                                                     │
│    ... e così per ogni view                         │
└────────────┬────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────┐
│ 5. Store views in routes registry                   │
│    routes.put(ROUTE_LOGIN, loginView)               │
│    routes.put(ROUTE_HOME, homeView)                 │
│    routes.put(ROUTE_REGISTRAZIONE, registrazioneView)
│    ...                                              │
└────────────┬────────────────────────────────────────┘
             │
             ▼ Runtime: Navigator.goTo(ROUTE_LOGIN)
┌─────────────────────────────────────────────────────┐
│ 6. Navigate & Use View                              │
│    view = routes.get(ROUTE_LOGIN)                   │
│    view.onShow(params)                              │
│    ├─ view.controller.metodo(dati)                  │
│    └─ controller elabora e torna a Navigator        │
└─────────────────────────────────────────────────────┘
```

---

## DIAGRAMMA: COME FACTORY CREA VIEW + CONTROLLER

```
factory.createLoginView()
    │
    ├─ Step 1: Check cache
    │  if (loginView == null)
    │
    ├─ Step 2: Create Controller
    │  controller = createLoginController()
    │  └─ new GUIGraphicLoginController(navigator)
    │
    ├─ Step 3: Create View + Inject Controller
    │  loginView = new GUILoginView(controller)
    │  └─ GUILoginView.constructor(controller)
    │     └─ this.controller = controller
    │
    └─ Step 4: Return Cached View
       return loginView  (next time, returns cached)
```

---

## PATTERN IN AZIONE: ESEMPI REALI

### ✅ CORRETTO: Factory + Constructor Injection

**GUIFrontendControllerFactory.java:**
```java
public GUILoginView createLoginView() {
    if (loginView == null) {
        // 1. Crea il controller
        GUIGraphicLoginController ctrl = 
            (GUIGraphicLoginController) createLoginController();
        
        // 2. Passa al costruttore della view
        loginView = new GUILoginView(ctrl);
        
        // 3. Cache: prossima volta ritorna stesso oggetto
    }
    return loginView;
}

public GUIRegistrazioneView createRegistrazioneView() {
    if (registrazioneView == null) {
        registrazioneView = new GUIRegistrazioneView(
            (GUIGraphicControllerRegistrazione) createRegistrazioneController()
        );
    }
    return registrazioneView;
}
```

**GUILoginView.java:**
```java
public class GUILoginView extends GenericViewGUI 
        implements ViewLogin, NavigableController {
    
    private final GUIGraphicLoginController controller;
    
    // Constructor Injection: riceve il controller dal di fuori
    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
    }
    
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        
        // Usa il controller che è stato passato al costruttore
        // Non lo crea, non lo recupera con Service Locator
        controller.login(email, password);
    }
}
```

**CLILoginView.java:**
```java
public class CLILoginView extends GenericViewCLI 
        implements ViewLogin, NavigableController {
    
    private final CLIGraphicLoginController controller;
    
    public CLILoginView(CLIGraphicLoginController controller) {
        this.controller = controller;
    }
    
    @Override
    public void onShow(Map<String, Object> params) {
        super.onShow(params);
        
        System.out.print("Email: ");
        String email = in.nextLine();
        
        // Usa il controller passato al costruttore
        controller.login(email, password);
    }
}
```

---

## ❌ SCONSIGLIATO: Service Locator Pattern

```java
// ❌ NO! Non fare così:
public class GUILoginView {
    
    @Override
    public void onShow(Map<String, Object> params) {
        // Recuperare il controller dinamicamente?
        // Problema: loose binding, difficile testare, difficile tracciare dipendenze
        GUIGraphicLoginController controller = 
            ServiceLocator.getInstance().getController(GUIGraphicLoginController.class);
        controller.login(email, password);
    }
}
```

**Perché è male:**
- ❌ Hidden dependency: non vedi che la view dipende dal controller nel costruttore
- ❌ Difficile testare: come metti un mock?
- ❌ Tight coupling al ServiceLocator
- ❌ Non segui DIP (Dependency Inversion Principle)

---

## ✅ CORRETTO: Dependency Injection

```java
public class GUILoginView {
    private final GUIGraphicLoginController controller;
    
    // La dipendenza è VISIBILE nel costruttore
    public GUILoginView(GUIGraphicLoginController controller) {
        this.controller = controller;
    }
    
    // Facile testare:
    // @Test
    // public void test() {
    //     MockController mock = new MockController();
    //     GUILoginView view = new GUILoginView(mock);
    //     view.onShow(...);
    //     verify(mock).login(...);
    // }
}
```

**Perché è bene:**
- ✅ Dipendenza ESPLICITA nel costruttore
- ✅ Facile da testare (basta passare un mock)
- ✅ Segui DIP
- ✅ Codice leggibile e prevedibile

---

## TABELLA RIASSUNTIVA: COME OGNI VIEW OTTIENE IL CONTROLLER

| View | Controller | Metodo Iniezione | Factory Method |
|------|-----------|-----------------|-----------------|
| GUILoginView | GUIGraphicLoginController | Constructor | `createLoginView()` |
| GUIRegistrazioneView | GUIGraphicControllerRegistrazione | Constructor | `createRegistrazioneView()` |
| GUIPrenotazioneView | GUIGraphicControllerPrenotazione | Constructor | `createPrenotazioneView()` |
| GUIAccountView | GUIGraphicControllerAccount | Constructor | `createAccountView()` |
| GUIDisdettaView | GUIGraphicControllerDisdetta | Constructor | `createDisdettaView()` |
| GUIRegoleView | GUIGraphicControllerRegole | Constructor | `createRegoleView()` |
| GUILogView | GUIGraphicControllerLog | Constructor | `createLogView()` |
| GUIPenalitaView | GUIGraphicControllerPenalita | Constructor | `createPenalitaView()` |
| **CLILoginView** | **CLIGraphicLoginController** | **Constructor** | **`createLoginView()`** |
| **CLIRegistrazioneView** | **CLIGraphicControllerRegistrazione** | **Constructor** | **`createRegistrazioneView()`** |
| **...** | **...** | **Constructor** | **...** |
| GUIHomeView | NavigationController | Constructor | `createHomeView()` |
| CLIHomeView | NavigationController | Constructor | `createHomeView()` |

---

## PSEUDOCODE: INIT COMPLETA

```java
// AppBootstrapper.java
public class AppBootstrapper {
    public static void main(String[] args) {
        // 1. Leggi config
        String frontend = config.getFrontend();  // "GUI" o "CLI"
        String backend = config.getBackend();    // "DBMS", "FILESYSTEM", etc.
        
        // 2. Crea factory frontend
        FrontendControllerFactory factory;
        if (frontend.equals("GUI")) {
            factory = new GUIFrontendControllerFactory();
        } else {
            factory = new CLIFrontendControllerFactory();
        }
        
        // 3. La factory crea:
        //    - Tutti i controller (via createXController())
        //    - Tutte le view (via createXView())
        //    - E le connette con Constructor Injection
        
        // 4. La factory registra tutte le view nel Navigator
        GraphicControllerNavigation nav = factory.createNavigationController();
        nav.registerView("LOGIN", factory.createLoginView());
        nav.registerView("HOME", factory.createHomeView());
        nav.registerView("REGISTRAZIONE", factory.createRegistrazioneView());
        // ...
        
        // 5. Start application
        factory.startApplication();  // va a ROUTE_LOGIN
    }
}

// Runtime: Navigator richiama le view
// navigator.goTo("LOGIN")
//   → view = viewRegistry.get("LOGIN")  // GUILoginView o CLILoginView
//   → view.onShow(params)
//     → view.controller.metodo(data)  // Usa il controller iniettato
```

---

## FLUSSO SCHEMATICO: DEPENDENCY GRAPH

```
Bootstrap
    │
    ├─ Config.getFrontend() = "GUI"
    │
    ├─ Factory = new GUIFrontendControllerFactory()
    │
    ├─ Navigator = factory.createNavigationController()
    │   └─ new GUIGraphicControllerNavigation(...)
    │
    ├─ LoginController = factory.createLoginController()
    │   └─ new GUIGraphicLoginController(Navigator)
    │
    ├─ LoginView = factory.createLoginView()
    │   ├─ createLoginController()  ← riutilizza il cache
    │   └─ new GUILoginView(LoginController)  ← Injection
    │
    ├─ RegistrazioneController = factory.createRegistrazioneController()
    │   └─ new GUIGraphicControllerRegistrazione(Navigator)
    │
    ├─ RegistrazioneView = factory.createRegistrazioneView()
    │   ├─ createRegistrazioneController()
    │   └─ new GUIRegistrazioneView(RegistrazioneController)  ← Injection
    │
    └─ ...registra tutte le view nel Navigator
       Navigator.registerView("LOGIN", LoginView)
       Navigator.registerView("REGISTRAZIONE", RegistrazioneView)
       ...
       
    ▼ start()
    
    Navigator.goTo("LOGIN")
        │
        ├─ view = viewRegistry.get("LOGIN")
        │
        └─ view.onShow(params)
            │
            └─ view.controller.login(email, password)
                │
                ├─ Elabora dati
                ├─ Chiama LogicController
                ├─ Ritorna a Navigator
                │
                └─ Navigator.goTo("HOME", resultParams)
```

---

## SUMMARY

### ✅ COME ATTUALMENTE IMPLEMENTATO (CORRETTO)

1. **Factory Method**: `GUIFrontendControllerFactory` + `CLIFrontendControllerFactory`
2. **Creazione View**: `createXView()` dentro la factory
3. **Creazione Controller**: `createXController()` dentro la factory
4. **Connessione**: Constructor Injection nel costruttore della view
5. **Singleton**: Ogni view/controller creato una sola volta e cacheato

### 🔑 KEY POINTS

- View **NON crea** il controller
- View **NON usa** Service Locator
- View **riceve** il controller nel costruttore
- Factory **crea** controller e view e le **connette**
- Pattern: **Factory Method + Constructor Injection**

### 📐 DIAGRAMMA UML

```
┌──────────────────────────┐
│ FrontendControllerFactory│
│ (Abstract)               │
│                          │
│ +createLoginController() │
│ +createLoginView()       │
│ +createXController()     │
│ +createXView()           │
└────────────┬─────────────┘
             │
      ┌──────┴──────┐
      │             │
   ┌──▼──────────┐ ┌─▼───────────┐
   │GUI          │ │CLI          │
   │Factory      │ │Factory      │
   └─────────────┘ └─────────────┘
        │               │
        ├─ createLoginView() ┐
        │  ├─ new GUIGraphicLoginController()
        │  └─ new GUILoginView(ctrl) ← INJECTION
        │
        └─ createRegistrazioneView()
           ├─ new GUIGraphicControllerRegistrazione()
           └─ new GUIRegistrazioneView(ctrl) ← INJECTION
```
