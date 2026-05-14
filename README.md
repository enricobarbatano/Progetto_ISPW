sportBooking

Applicazione Java per il progetto ISPW con supporto a CLI e GUI JavaFX (FXML + CSS). 
All’avvio permette di scegliere interfaccia (CLI/GUI), modalità (DEMO/STANDARD) e persistenza (IN_MEMORY / FILE_SYSTEM / DBMS MySQL via JDBC).

AMBIENTE DI RIFERIMENTO (TESTATO)
- OS: Windows 11 (amd64)
- Java: Eclipse Temurin JDK 17.0.17
- Maven: Apache Maven 3.9.11

AVVIO (SVILUPPO) — COMANDO UFFICIALE
Dalla root del progetto:
  mvn clean javafx:run

Durante l’avvio l’app chiede:
- Seleziona Interfaccia: 1) CLI  2) GUI
- Seleziona Modalita: 1) DEMO (in-memory, no persistenza)  2) STANDARD (con persistenza)
- Seleziona Persistenza: 1) FILE_SYSTEM  2) DBMS  (eventualmente anche IN_MEMORY se previsto)

PERSISTENZA
1) IN_MEMORY
Esecuzione in memoria. Se previsto, può caricare dati iniziali da una cartella “seed”.

2) FILE_SYSTEM
Persistenza su filesystem tramite una root directory locale.

3) DBMS (MySQL)
Richiede un server MySQL raggiungibile (es. localhost:3306), database/schema esistente (es. centro_sportivo) e un utente con permessi (es. ispw_user).
Esempio URL JDBC:
  jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome

TEST
Test unitari:
  mvn test
Test + eventuali integration test:
  mvn verify

STRUTTURA PROGETTO 
- src/main/java        codice sorgente
- src/main/resources   risorse
  - fxml/              viste JavaFX
  - css/               stylesheet (es. app.css)
- src/test/java        test

BRANCHING / WORKFLOW
- project: branch base/pulito (sviluppo normale)
- business: branch con estensioni e deployment/packaging (JPMS/jlink, zip runtime, script .bat, inclusione driver DBMS, ecc.)
In questo branch project NON sono documentati i dettagli di deployment; per packaging e distribuzione fare riferimento al branch business.

GITIGNORE (CONSIGLIATO)
Ignorare artefatti locali e file generati, ad esempio:
- target/
- *.zip
- *.drawio.drawio.xml
- build_installer.cmd
- ISPW_Project.docx

AUTORE
Enrico Barbatano

LICENZA
Uso didattico/universitario.
