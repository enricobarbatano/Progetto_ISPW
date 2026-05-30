# 🎨 MVC DIAGRAM - View-Controller Injection Pattern

## 1. DIAGRAMMA MVC COMPLETO CON INJECTION

```mermaid
graph TD
    subgraph MVC["🏗️ ARCHITETTURA MVC (7 LAYER + BOOTSTRAP)"]
        subgraph Layer0["⚙️ LAYER 0: BOOTSTRAP"]
            B["AppBootstrapper"]
            BC["AppConfigurator"]
            B -->|seleziona frontend| BC
        end

        subgraph Layer1["🎨 LAYER 1: VIEW (Presentazione)"]
            V0["GenericView (interface)"]
            V1["GenericViewBase (abstract)"]
            V2["GenericViewGUI / GenericViewCLI"]
            V3["GUIXView / CLIXView (concrete)"]
            FXML["FXML Controllers (GUI only)"]
            
            V0 -.->|implements| V1
            V1 -->|extends| V2
            V2 -->|extends| V3
            V3 -->|carica FXML| FXML
        end

        subgraph Factory["🏭 FRONTEND CONTROLLER FACTORY"]
            F["GUIFrontendControllerFactory<br/>CLIFrontendControllerFactory"]
            FC1["createLoginController()"]
            FC2["createXController()"]
            FV1["createLoginView()"]
            FV2["createXView()"]
            
            F -->|method| FC1
            F -->|method| FC2
            F -->|method| FV1
            F -->|method| FV2
        end

        subgraph Layer2["📱 LAYER 2: GRAPHIC CONTROLLER (UI Logic)"]
            GC0["GraphicControllerNavigation"]
            GC1["GUIGraphicXController<br/>CLIGraphicXController"]
            
            GC0 -.->|navigation| GC1
        end

        subgraph Layer3["🧭 LAYER 3: NAVIGATOR (Routing)"]
            N["Navigator"]
            N -->|stores views| V3
            N -->|stores controllers| GC1
        end

        subgraph Layer4["⚡ LAYER 4-5: LOGIC CONTROLLER"]
            LC["LogicControllerX<br/>(business logic)"]
        end

        subgraph Layer6["💾 LAYER 6: DAO"]
            D["DAOFactory + DAO implementations"]
        end

        subgraph Layer7["📊 LAYER 7: MODEL"]
            M["Entity + Enums"]
        end

        %% Connessioni Layer 0 -> Factory
        B -->|instantiate| F
        
        %% Connessioni Factory -> Layer 1 + Layer 2
        FV1 -->|creates| V3
        FC1 -->|creates| GC1
        FV1 -.->|dependency injection| GC1
        FV2 -.->|dependency injection| GC1
        
        %% Connessioni Layer 1 -> Layer 2
        V3 -->|has field| GC1
        
        %% Connessioni Layer 2 -> Layer 3
        GC1 -->|navigate| N
        
        %% Connessioni Layer 3 -> Layer 4
        N -->|via navigator| LC
        
        %% Connessioni Layer 4 -> Layer 6
        LC -->|call| D
        
        %% Connessioni Layer 6 -> Layer 7
        D -->|persist| M
    end

    style Layer0 fill:#f0f0f0,stroke:#333,stroke-width:2px
    style Layer1 fill:#e1f5ff,stroke:#0277bd,stroke-width:2px
    style Factory fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style Layer2 fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style Layer3 fill:#fff3e0,stroke:#e65100,stroke-width:2px
    style Layer4 fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    style Layer6 fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    style Layer7 fill:#f1f8e9,stroke:#558b2f,stroke-width:2px
```

---

## 2. DIAGRAMMA DETTAGLIATO: COME FACTORY INIETTA IL CONTROLLER

```mermaid
graph LR
    subgraph Factory["🏭 FrontendControllerFactory"]
        subgraph Create["Fase 1: Creazione"]
            FC["createXController()"]
            GC["new GUIGraphicXController<br/>(Navigator)"]
            FC -->|new| GC
        end
        
        subgraph Inject["Fase 2: Injection"]
            FV["createXView()"]
            V["new GUIXView<br/>(controller)"]
            FV -->|new| V
        end
        
        subgraph Cache["Fase 3: Cache"]
            CV["xView field<br/>(SINGLETON)"]
            V -->|store| CV
        end
    end

    subgraph View["📱 GUIXView"]
        VC["private final<br/>GUIGraphicXController"]
        VU["public void onShow()"]
        VC -.->|referenced by| VU
    end

    subgraph Usage["🎬 Runtime Usage"]
        Nav["Navigator.goTo()"]
        VShow["view.onShow(params)"]
        Call["controller.metodo()"]
    end

    GC -->|iniettato nel<br/>costruttore| V
    V -->|memorizzato in| VC
    
    Nav -->|retrieves| View
    View -->|invokes| VShow
    VShow -->|calls| Call

    style Factory fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style View fill:#e1f5ff,stroke:#0277bd,stroke-width:2px
    style Usage fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```

---

## 3. DIAGRAMMA DI DIPENDENZA: DEPENDENCY GRAPH

```mermaid
graph TB
    subgraph Bootstrap["🟢 Bootstrap Time (Fisso)"]
        Config["Config:<br/>frontend=GUI"]
        Factory["Factory:<br/>GUIFrontendControllerFactory"]
        Nav["Navigator"]
        
        Config -->|creates| Factory
        Factory -->|creates| Nav
    end

    subgraph Controllers["🟠 Controllers (Creati una sola volta)"]
        NavCtrl["NavigationController"]
        LoginCtrl["LoginController"]
        RegistrazioneCtrl["RegistrazioneController"]
        PrenotazioneCtrl["PrenotazioneController"]
        
        Factory -->|createNavigationController| NavCtrl
        Factory -->|createLoginController| LoginCtrl
        Factory -->|createRegistrazioneController| RegistrazioneCtrl
        Factory -->|createPrenotazioneController| PrenotazioneCtrl
    end

    subgraph Views["🔵 Views (Iniettate con Controller)"]
        LoginView["LoginView(LoginCtrl)"]
        RegistrazioneView["RegistrazioneView(RegistrazioneCtrl)"]
        PrenotazioneView["PrenotazioneView(PrenotazioneCtrl)"]
        HomeView["HomeView(NavCtrl)"]
        
        Factory -->|createLoginView| LoginView
        LoginCtrl -.->|injected into| LoginView
        
        Factory -->|createRegistrazioneView| RegistrazioneView
        RegistrazioneCtrl -.->|injected into| RegistrazioneView
        
        Factory -->|createPrenotazioneView| PrenotazioneView
        PrenotazioneCtrl -.->|injected into| PrenotazioneView
        
        Factory -->|createHomeView| HomeView
        NavCtrl -.->|injected into| HomeView
    end

    subgraph Routes["🟣 Routes Registry (in Navigator)"]
        R["routes:<br/>LOGIN → LoginView<br/>REGISTRAZIONE → RegistrazioneView<br/>..."]
        Nav -->|register| R
        Views -->|stored in| R
    end

    subgraph Runtime["🔴 Runtime (Navigazione)"]
        NavGo["Navigator.goTo(ROUTE_LOGIN)"]
        ViewRetrieve["view = routes.get(LOGIN)"]
        ViewShow["view.onShow(params)"]
        ControllerCall["controller.login(...)"]
        
        NavGo -->|lookup| ViewRetrieve
        ViewRetrieve -->|retrieves| LoginView
        ViewShow -->|calls| ControllerCall
    end

    style Bootstrap fill:#c8e6c9,stroke:#2e7d32,stroke-width:2px
    style Controllers fill:#ffccbc,stroke:#d84315,stroke-width:2px
    style Views fill:#b3e5fc,stroke:#0277bd,stroke-width:2px
    style Routes fill:#e1bee7,stroke:#6a1b9a,stroke-width:2px
    style Runtime fill:#ffe0b2,stroke:#e65100,stroke-width:2px
```

---

## 4. DIAGRAMMA DI SEQUENZA: ISTANZIAZIONE E UTILIZZO

```mermaid
sequenceDiagram
    participant Bootstrap as AppBootstrapper
    participant Factory as GUIFrontendControllerFactory
    participant Navigator as Navigator
    participant Ctrl as GUIGraphicLoginController
    participant View as GUILoginView
    participant FXML as LoginFXMLController
    participant NavCtrl as GraphicControllerNavigation

    Note over Bootstrap,FXML: BOOTSTRAP TIME (Inizializzazione)
    
    Bootstrap->>Factory: new GUIFrontendControllerFactory()
    activate Factory
        Factory->>NavCtrl: createNavigationController()
        activate NavCtrl
            NavCtrl->>NavCtrl: new GUIGraphicControllerNavigation()
        deactivate NavCtrl
    deactivate Factory

    Bootstrap->>Factory: createLoginController()
    activate Factory
        Factory->>Ctrl: new GUIGraphicLoginController(navigator)
        activate Ctrl
        deactivate Ctrl
    deactivate Factory

    Bootstrap->>Factory: createLoginView()
    activate Factory
        Factory->>Factory: createLoginController() [cached]
        Factory->>View: new GUILoginView(controller)
        activate View
            Note over View: private final GUIGraphicLoginController controller
        deactivate View
    deactivate Factory

    Bootstrap->>Navigator: registerView("LOGIN", loginView)
    activate Navigator
        Navigator->>Navigator: routes.put("LOGIN", loginView)
    deactivate Navigator

    Note over Bootstrap,FXML: RUNTIME (Navigazione)
    
    Bootstrap->>NavCtrl: goTo("LOGIN")
    activate NavCtrl
        NavCtrl->>Navigator: navigate(ROUTE_LOGIN, params)
        activate Navigator
            Navigator->>View: view = routes.get("LOGIN")
            Navigator->>View: view.onShow(params)
        deactivate Navigator
        
        activate View
            View->>FXML: FXMLLoader.load("/fxml/login.fxml")
            activate FXML
                FXML->>FXML: new LoginFXMLController() [auto]
            deactivate FXML
            
            View->>FXML: setGraphicController(this.controller)
            View->>FXML: initWithParams(params)
            
            Note over View: User interacts with FXML UI
        deactivate View
        
        FXML->>Ctrl: handleLoginButton()
        activate Ctrl
            Ctrl->>Ctrl: login(email, password)
            Ctrl->>NavCtrl: ritorna a Navigator
        deactivate Ctrl
        
        NavCtrl->>Navigator: goTo("HOME", resultParams)
        activate Navigator
            Navigator->>View: homeView.onShow(params)
        deactivate Navigator
    deactivate NavCtrl
```

---

## 5. DIAGRAMMA UML: VIEW ← CONTROLLER RELATIONSHIP

```mermaid
classDiagram
    class GenericView {
        <<interface>>
        +onShow(Map params)
        +onHide()
    }

    class GenericViewBase {
        <<abstract>>
        #sessione: SessioneUtenteBean
        #lastParams: Map
    }

    class GenericViewGUI {
        <<abstract>>
    }

    class GUILoginView {
        -controller: GUIGraphicLoginController
        +GUILoginView(controller)
        +onShow(Map params)
    }

    class GUIGraphicLoginController {
        -navigator: GraphicControllerNavigation
        +login(email, password)
        +logout()
    }

    class GUIFrontendControllerFactory {
        -loginController: GUIGraphicLoginController
        -loginView: GUILoginView
        +createLoginController() GUIGraphicLoginController
        +createLoginView() GUILoginView
    }

    GenericView <|-- GenericViewBase
    GenericViewBase <|-- GenericViewGUI
    GenericViewGUI <|-- GUILoginView

    %% Composition: View HAS-A Controller
    GUILoginView "1" *-- "1" GUIGraphicLoginController : has

    %% Factory creates both
    GUIFrontendControllerFactory ..> GUIGraphicLoginController : creates
    GUIFrontendControllerFactory ..> GUILoginView : creates
    GUIFrontendControllerFactory --> GUIGraphicLoginController : injects into

    style GenericView fill:#e1f5ff,stroke:#0277bd
    style GenericViewBase fill:#e1f5ff,stroke:#0277bd
    style GenericViewGUI fill:#e1f5ff,stroke:#0277bd
    style GUILoginView fill:#b3e5fc,stroke:#0277bd,stroke-width:2px
    style GUIGraphicLoginController fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style GUIFrontendControllerFactory fill:#fff9c4,stroke:#f57f17,stroke-width:2px
```

---

## 6. DIAGRAMMA PACKAGE: LAYOUT FILE SYSTEM

```mermaid
graph TB
    subgraph ViewPackage["📦 com.ispw.view"]
        subgraph Interfaces["interfaces/"]
            II["GenericView.java"]
            VI["ViewXX.java<br/>(marker interfaces)"]
        end
        
        subgraph Common["common/"]
            IC["GenericViewBase.java"]
        end
        
        subgraph GUI["gui/"]
            GI["GenericViewGUI.java"]
            GV["GUIXView.java<br/>(concreti)"]
            GF["fxml/<br/>FXML + Controllers"]
        end
        
        subgraph CLI["cli/"]
            CI["GenericViewCLI.java"]
            CV["CLIXView.java<br/>(concreti)"]
        end
    end

    subgraph ControllerPackage["📦 com.ispw.controller.graphic"]
        subgraph Interfaces2["interfaces/"]
            CI["GraphicControllerXX.java<br/>(interfaces)"]
        end
        
        subgraph GUIControllers["gui/"]
            GC["GUIGraphicXController.java<br/>(concreti)"]
        end
        
        subgraph CLIControllers["cli/"]
            CC["CLIGraphicXController.java<br/>(concreti)"]
        end
        
        subgraph Factory["factory/"]
            GUF["GUIFrontendControllerFactory.java"]
            CLIF["CLIFrontendControllerFactory.java"]
        end
    end

    subgraph MVC["📊 MVC MAPPING"]
        M["MODEL:<br/>Entity + DAO"]
        V["VIEW:<br/>GenericViewGUI/CLI<br/>+ Concrete Views"]
        C["CONTROLLER:<br/>GraphicController<br/>(+ Logic Controller)"]
    end

    %% Connections
    II -.-> VI
    IC -.-> GV
    IC -.-> CV
    
    GUF -->|creates| GC
    GUF -->|creates| GV
    GC -.->|injected| GV
    
    CLIF -->|creates| CC
    CLIF -->|creates| CV
    CC -.->|injected| CV
    
    %% MVC Mapping
    ViewPackage -->|is| V
    ControllerPackage -->|is| C

    style ViewPackage fill:#e1f5ff,stroke:#0277bd,stroke-width:2px
    style ControllerPackage fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style MVC fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style Factory fill:#fff59d,stroke:#f57f17,stroke-width:2px
```

---

## 7. DIAGRAMMA FINALE: MVC + INJECTION PATTERN

```mermaid
graph TB
    subgraph MVC_Full["🏗️ ARCHITETTURA MVC CON INJECTION PATTERN"]
        
        subgraph Bootstrap["⚙️ BOOTSTRAP"]
            B["AppBootstrapper"]
        end
        
        subgraph Factory["🏭 FACTORY"]
            F["FrontendControllerFactory<br/>(GUI o CLI)"]
        end
        
        subgraph M["📊 MODEL"]
            ME["Entity + DAO"]
        end
        
        subgraph C["🎮 CONTROLLER"]
            LC["LogicController"]
            GC["GraphicController<br/>← INIETTATO NELLA VIEW"]
        end
        
        subgraph V["🎨 VIEW"]
            VB["GenericViewBase"]
            VS["GenericViewGUI/CLI"]
            VC["GUIXView / CLIXView<br/>+ private final<br/>GUIGraphicXController"]
        end
        
        subgraph Nav["🧭 NAVIGATOR"]
            N["Navigator<br/>(routes registry)"]
        end
        
        B -->|1) Crea| F
        F -->|2) Crea| GC
        F -->|3) Crea + Inietta| VC
        GC -.->|iniettato nel<br/>costruttore| VC
        
        VC -->|estende| VS
        VS -->|estende| VB
        
        VC -->|chiama| GC
        GC -->|delega a| LC
        LC -->|accede a| ME
        
        F -->|registra| N
        VC -->|stored in| N
        N -->|retrieves| VC
        
    end

    style Bootstrap fill:#f0f0f0,stroke:#333
    style Factory fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style M fill:#f1f8e9,stroke:#558b2f,stroke-width:2px
    style C fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style V fill:#e1f5ff,stroke:#0277bd,stroke-width:2px
    style Nav fill:#fff3e0,stroke:#e65100,stroke-width:2px
```

---

## 8. LEGENDA PER IL DIAGRAMMA MVC UFFICIALE

Quando includi questo nel tuo diagramma MVC finale:

### **Notazione:**

```
┌─ [Controller] ─────┐
│                    │
│ • Ricevuto        │
│   via Constructor │
│   Injection       │
│                    │
│ • Memorizzato     │
│   in private      │
│   final field     │
│                    │
└─ [Factory] ────────┘

┌─ [View] ───────────┐
│                    │
│ • private final    │
│   GraphicController│
│                    │
│ • Usato in onShow()│
│                    │
└────────────────────┘
```

### **Metti nel Diagramma:**

1. **View Layer** (mostra GenericViewGUI/CLI → GUIXView)
2. **Controller Layer** (mostra GraphicController)
3. **Factory** (mostra come crea + connette)
4. **Freccia di injection**: `View ← Controller` con etichetta `Constructor Injection`
5. **Freccia di caching**: `Factory → ViewRegistry (in Navigator)`

---

## SUMMARY: COSA RAPPRESENTARE NEL MVC

```
╔════════════════════════════════════════════════════╗
║ BOOTSTRAP                                          ║
║ AppBootstrapper → FrontendControllerFactory        ║
╚────────────────┬─────────────────────────────────────╝
                 │
    ┌────────────▼─────────────┐
    │ Factory Method           │
    │ (Component Creation)     │
    │                          │
    │ 1) createController()    │
    │ 2) createView()          │
    │ 3) Inietta Controller    │
    │    nel View Constructor  │
    └──────────┬───────────────┘
               │
    ┌──────────▼──────────┐
    │ VIEW ← CONTROLLER   │
    │ (Constructor        │
    │  Injection)         │
    │                     │
    │ View ha private     │
    │ final Controller    │
    └─────────────────────┘

[M] ← [C] → [V]
     ↓
  [Factory crea e connette]
```

Rappresenta il fatto che la View **non crea** il Controller, ma lo **riceve** già fatto dalla Factory!
