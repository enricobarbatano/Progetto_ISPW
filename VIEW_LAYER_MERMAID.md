# 🎨 MVC DIAGRAM - LAYER VIEW (Mermaid)

## Diagramma Gerarchico delle Classi

```mermaid
classDiagram
    %% Interface
    class GenericView {
        <<interface>>
        +onShow()
        +onShow(Map params)
        +onHide()
        +readSession(Map params)
        +readError(Map params)
        +readSuccess(Map params)
    }

    %% Abstract Base
    class GenericViewBase {
        <<abstract>>
        #sessione: SessioneUtenteBean
        #lastParams: Map
        +getSessione()
        +getLastParams()
        +getLastError()
        +getLastSuccess()
    }

    %% Specialized Abstracts
    class GenericViewGUI {
        <<abstract>>
    }

    class GenericViewCLI {
        <<abstract>>
    }

    %% Marker Interfaces
    class ViewLogin {
        <<interface>>
    }

    class ViewRegistrazione {
        <<interface>>
    }

    class ViewPrenotazione {
        <<interface>>
    }

    class ViewGestioneAccount {
        <<interface>>
    }

    class ViewDisdetta {
        <<interface>>
    }

    class ViewGestioneRegole {
        <<interface>>
    }

    class ViewLog {
        <<interface>>
    }

    class ViewPenalita {
        <<interface>>
    }

    %% Concrete GUI Views
    class GUILoginView {
        -controller: GUIGraphicLoginController
        -fxmlController: LoginFXMLController
        +onShow(Map params)
    }

    class GUIRegistrazioneView {
        -controller: GUIGraphicRegistrazioneController
        -fxmlController: RegistrazioneFXMLController
        +onShow(Map params)
    }

    class GUIPrenotazioneView {
        -controller: GUIGraphicPrenotazioneController
        -fxmlController: PrenotazioneFXMLController
        +onShow(Map params)
    }

    class GUIAccountView {
        -controller: GUIGraphicAccountController
        -fxmlController: AccountFXMLController
        +onShow(Map params)
    }

    class GUIDisdettaView {
        -controller: GUIGraphicDisdettaController
        -fxmlController: DisdettaFXMLController
        +onShow(Map params)
    }

    class GUIRegoleView {
        -controller: GUIGraphicRegoleController
        -fxmlController: RegoleFXMLController
        +onShow(Map params)
    }

    class GUILogView {
        -controller: GUIGraphicLogController
        -fxmlController: LogFXMLController
        +onShow(Map params)
    }

    class GUIPenalitaView {
        -controller: GUIGraphicPenalitaController
        -fxmlController: PenalitaFXMLController
        +onShow(Map params)
    }

    %% Concrete CLI Views
    class CLILoginView {
        -controller: CLIGraphicLoginController
        -in: Scanner
        +onShow(Map params)
    }

    class CLIRegistrazioneView {
        -controller: CLIGraphicRegistrazioneController
        -in: Scanner
        +onShow(Map params)
    }

    class CLIPrenotazioneView {
        -controller: CLIGraphicPrenotazioneController
        -in: Scanner
        +onShow(Map params)
    }

    class CLIAccountView {
        -controller: CLIGraphicAccountController
        -in: Scanner
        +onShow(Map params)
    }

    class CLIDisdettaView {
        -controller: CLIGraphicDisdettaController
        -in: Scanner
        +onShow(Map params)
    }

    class CLIRegoleView {
        -controller: CLIGraphicRegoleController
        -in: Scanner
        +onShow(Map params)
    }

    class CLILogView {
        -controller: CLIGraphicLogController
        -in: Scanner
        +onShow(Map params)
    }

    class CLIPenalitaView {
        -controller: CLIGraphicPenalitaController
        -in: Scanner
        +onShow(Map params)
    }

    %% FXML Controllers (GUI only)
    class LoginFXMLController {
        -graphicController: GUIGraphicLoginController
        +handleLogin()
        +setGraphicController()
        +initWithParams()
    }

    class RegistrazioneFXMLController {
        -graphicController: GUIGraphicRegistrazioneController
        +handleRegistrazione()
    }

    class PrenotazioneFXMLController {
        -graphicController: GUIGraphicPrenotazioneController
        +handlePrenotazione()
    }

    %% Hierarchy
    GenericView <|-- GenericViewBase
    GenericViewBase <|-- GenericViewGUI
    GenericViewBase <|-- GenericViewCLI

    GenericView <|.. ViewLogin
    GenericView <|.. ViewRegistrazione
    GenericView <|.. ViewPrenotazione
    GenericView <|.. ViewGestioneAccount
    GenericView <|.. ViewDisdetta
    GenericView <|.. ViewGestioneRegole
    GenericView <|.. ViewLog
    GenericView <|.. ViewPenalita

    %% GUI Implementations
    GenericViewGUI <|-- GUILoginView
    GenericViewGUI <|-- GUIRegistrazioneView
    GenericViewGUI <|-- GUIPrenotazioneView
    GenericViewGUI <|-- GUIAccountView
    GenericViewGUI <|-- GUIDisdettaView
    GenericViewGUI <|-- GUIRegoleView
    GenericViewGUI <|-- GUILogView
    GenericViewGUI <|-- GUIPenalitaView

    ViewLogin <|.. GUILoginView
    ViewRegistrazione <|.. GUIRegistrazioneView
    ViewPrenotazione <|.. GUIPrenotazioneView
    ViewGestioneAccount <|.. GUIAccountView
    ViewDisdetta <|.. GUIDisdettaView
    ViewGestioneRegole <|.. GUIRegoleView
    ViewLog <|.. GUILogView
    ViewPenalita <|.. GUIPenalitaView

    %% CLI Implementations
    GenericViewCLI <|-- CLILoginView
    GenericViewCLI <|-- CLIRegistrazioneView
    GenericViewCLI <|-- CLIPrenotazioneView
    GenericViewCLI <|-- CLIAccountView
    GenericViewCLI <|-- CLIDisdettaView
    GenericViewCLI <|-- CLIRegoleView
    GenericViewCLI <|-- CLILogView
    GenericViewCLI <|-- CLIPenalitaView

    ViewLogin <|.. CLILoginView
    ViewRegistrazione <|.. CLIRegistrazioneView
    ViewPrenotazione <|.. CLIPrenotazioneView
    ViewGestioneAccount <|.. CLIAccountView
    ViewDisdetta <|.. CLIDisdettaView
    ViewGestioneRegole <|.. CLIRegoleView
    ViewLog <|.. CLILogView
    ViewPenalita <|.. CLIPenalitaView

    %% FXML Dependencies (GUI only)
    GUILoginView --> LoginFXMLController
    GUIRegistrazioneView --> RegistrazioneFXMLController
    GUIPrenotazioneView --> PrenotazioneFXMLController
```

---

## Diagramma di Istanziazione a Runtime

```mermaid
sequenceDiagram
    participant Bootstrap as AppBootstrapper
    participant Factory as ViewFactory
    participant Navigator as Navigator
    participant View as GUILoginView
    participant FXML as FXMLLoader
    participant Controller as LoginFXMLController
    participant GraphicCtrl as GUIGraphicLoginController

    Bootstrap->>Factory: createView(ViewType.LOGIN)
    Factory->>View: new GUILoginView(graphicController)
    Factory-->>Bootstrap: view (SINGLETON)
    
    Note over Bootstrap: View creata una sola volta a bootstrap
    
    Bootstrap->>Navigator: registerView(LOGIN, view)

    participant User as User
    
    User->>Navigator: navigate(ViewType.LOGIN, params)
    Navigator->>View: onShow(params)
    
    activate View
        View->>FXML: FXMLLoader.load("/fxml/login.fxml")
        activate FXML
            FXML->>Controller: new LoginFXMLController() [automatico]
            FXML-->>View: root + controller
        deactivate FXML
        
        View->>Controller: setGraphicController(graphicCtrl)
        View->>Controller: initWithParams(params)
        View->>View: GuiLauncher.setRoot(root)
    deactivate View
    
    User->>Controller: click button login
    Controller->>GraphicCtrl: login(email, password)
    
    Note over GraphicCtrl: GraphicController elabora e chiama Logic
    
    GraphicCtrl-->>Navigator: resultat + nextViewType
    Navigator->>Navigator: navigate(ViewType.HOME, resultParams)
```

---

## Diagramma Pacchetto (Directorio)

```mermaid
graph TD
    A["com.ispw.view"] --> B["interfaces/"]
    A --> C["common/"]
    A --> D["gui/"]
    A --> E["cli/"]
    A --> F["shared/"]

    B --> B1["GenericView.java"]
    B --> B2["ViewLogin.java"]
    B --> B3["ViewRegistrazione.java"]
    B --> B4["ViewPrenotazione.java"]
    B --> B5["ViewGestioneAccount.java"]
    B --> B6["ViewDisdetta.java"]
    B --> B7["ViewGestioneRegole.java"]
    B --> B8["ViewLog.java"]
    B --> B9["ViewPenalita.java"]

    C --> C1["GenericViewBase.java"]

    D --> D1["GenericViewGUI.java"]
    D --> D2["GUILoginView.java"]
    D --> D3["GUIRegistrazioneView.java"]
    D --> D4["GUIPrenotazioneView.java"]
    D --> D5["GUIAccountView.java"]
    D --> D6["GUIDisdettaView.java"]
    D --> D7["GUIRegoleView.java"]
    D --> D8["GUILogView.java"]
    D --> D9["GUIPenalitaView.java"]
    D --> D10["GuiLauncher.java"]
    D --> D11["GuiViewUtils.java"]
    D --> D12["fxml/"]
    
    D12 --> D12A["login.fxml"]
    D12 --> D12B["LoginFXMLController.java"]
    D12 --> D12C["registrazione.fxml"]
    D12 --> D12D["RegistrazioneFXMLController.java"]
    D12 --> D12E["...altri FXML e Controller..."]

    E --> E1["GenericViewCLI.java"]
    E --> E2["CLILoginView.java"]
    E --> E3["CLIRegistrazioneView.java"]
    E --> E4["CLIPrenotazioneView.java"]
    E --> E5["CLIAccountView.java"]
    E --> E6["CLIDisdettaView.java"]
    E --> E7["CLIRegoleView.java"]
    E --> E8["CLILogView.java"]
    E --> E9["CLIPenalitaView.java"]
    E --> E10["CliViewUtils.java"]
```

---

## Diagramma MVC Completo (con View Layer)

```mermaid
graph TD
    subgraph Layer0["⚙️ LAYER 0: BOOTSTRAP"]
        B0["AppBootstrapper"]
        B1["AppConfigurator"]
        B2["DbmsInitializer / FileSystemInitializer"]
    end

    subgraph Layer1["🎨 LAYER 1: VIEW (PRESENTAZIONE)"]
        V0["GenericView (interface)"]
        V1["GenericViewBase (abstract)"]
        V2["GenericViewGUI (abstract)"]
        V3["GenericViewCLI (abstract)"]
        V4["XView (marker interfaces)"]
        V5["GUIXView (concrete)"]
        V6["CLIXView (concrete)"]
        V7["FXML Controllers (GUI only)"]
    end

    subgraph Layer2["📱 LAYER 2: GRAPHIC CONTROLLER"]
        G0["GraphicControllerNavigation"]
        G1["AbstractGraphicController"]
        G2["GUIGraphicXController"]
        G3["CLIGraphicXController"]
    end

    subgraph Layer3["🧭 LAYER 3: NAVIGATOR (ROUTING)"]
        N0["Navigator"]
        N1["ViewFactory"]
    end

    subgraph Layer4["⚡ LAYER 4-5: LOGIC CONTROLLER"]
        L0["LogicControllerFactory"]
        L1["LogicControllerX (business logic)"]
    end

    subgraph Layer6["💾 LAYER 6: DAO"]
        D0["DAOFactory"]
        D1["DAO interfaces"]
        D2["DAO implementations"]
    end

    subgraph Layer7["📊 LAYER 7: MODEL (ENTITY)"]
        M0["Entity classes"]
        M1["Enums"]
    end

    %% Layer 0 -> Layer 1
    B0 -->|bootstrap| N1
    N1 -->|instanzia| V5
    N1 -->|instanzia| V6

    %% Layer 1 structure
    V0 -.-> V1
    V1 --> V2
    V1 --> V3
    V2 --> V5
    V3 --> V6
    V4 -.-> V5
    V4 -.-> V6
    V5 --> V7

    %% Layer 1 -> Layer 2
    V5 -->|has| G2
    V6 -->|has| G3

    %% Layer 2 -> Layer 3
    G2 --> N0
    G3 --> N0

    %% Layer 3 -> Layer 4
    N0 --> L0
    L0 --> L1

    %% Layer 4 -> Layer 6
    L1 --> D0
    D0 --> D2

    %% Layer 6 -> Layer 7
    D2 --> M0
    D2 --> M1

    style Layer0 fill:#f0f0f0
    style Layer1 fill:#e1f5ff
    style Layer2 fill:#f3e5f5
    style Layer3 fill:#fff3e0
    style Layer4 fill:#e8f5e9
    style Layer6 fill:#fce4ec
    style Layer7 fill:#f1f8e9
```

---

## Flusso di Istanziazione a Runtime (Timeline)

```mermaid
timeline
    title Istanziazione View Layer a Runtime

    section Bootstrap Time (Fisso)
        Backend Selection: frontend = "GUI" o "CLI"
        ViewFactory Creation: new GUIViewFactory() o CLIViewFactory()
        View Instantiation: new GUILoginView() + new GUIRegistrazioneView() + ...
        Navigator Setup: registry.put(ViewType.LOGIN, gUILoginView)
        
    section Runtime - Navigazione
        User Action: click button / console input
        Navigator Call: navigator.navigate(ViewType.LOGIN, params)
        View Show: view.onShow(params)
        
    section GUI Specific
        FXML Load: FXMLLoader.load("/fxml/login.fxml")
        FXML Controller: LoginFXMLController istanziato da FXML Loader
        Stage Update: GuiLauncher.setRoot(root)
        
    section CLI Specific
        Console Output: System.out.println("menu")
        User Input: scanner.nextLine()
        Controller Call: controller.metodo(data)
```
