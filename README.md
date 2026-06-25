# SportBooking – Progetto ISPW

## Descrizione

**SportBooking** è un'applicazione Java sviluppata per il progetto di **Ingegneria del Software e Progettazione Web (ISPW)**.  
Il sistema consente la gestione di un centro sportivo, offrendo funzionalità per la registrazione degli utenti, la consultazione della disponibilità dei campi, la prenotazione degli slot e la richiesta di disdetta delle prenotazioni.

L'applicazione supporta sia interfaccia **CLI** sia interfaccia **GUI JavaFX**, permettendo all'avvio di scegliere:

- il tipo di interfaccia utente: CLI o GUI;
- la modalità di esecuzione: DEMO o STANDARD;
- il provider di persistenza: IN_MEMORY, FILE_SYSTEM o DBMS MySQL.

Il progetto applica principi di progettazione software come separazione delle responsabilità, uso di pattern architetturali e gestione modulare della persistenza.

---

## Funzionalità principali

- Registrazione di nuovi utenti.
- Login e gestione account.
- Visualizzazione della disponibilità dei campi sportivi.
- Prenotazione di slot temporali.
- Richiesta di disdetta di una prenotazione.
- Gestione delle richieste di disdetta da parte del gestore.
- Applicazione di penalità agli utenti.
- Supporto a diverse modalità di persistenza.
- Interfaccia CLI e GUI JavaFX.

---

## Architettura e design

Il sistema è stato progettato seguendo una struttura multilivello, separando la logica applicativa, il layer di presentazione e il layer di persistenza.

Nel progetto sono stati utilizzati principalmente i seguenti pattern:

- **MVC (Model-View-Controller)**, per organizzare la struttura applicativa e separare modello, vista e controller.
- **DAO (Data Access Object)**, per isolare la logica applicativa dai dettagli di accesso ai dati.
- **Abstract Factory**, per selezionare in fase di bootstrap la famiglia di componenti coerente con il frontend e con il provider di persistenza scelti.
- **Singleton**, per garantire una configurazione coerente delle factory principali durante l'esecuzione dell'applicazione.

La documentazione tecnica include SRS, user stories, requisiti funzionali, diagrammi BCE/VOPC, diagrammi MVC, activity diagram, sequence diagram, state diagram, testing, gestione delle eccezioni e persistenza.

---

## Tecnologie utilizzate

- Java 17
- Maven
- JavaFX
- FXML
- CSS
- JDBC
- MySQL
- Jackson
- Jakarta Mail
- JUnit 5
- Mockito
- H2 Database per test
- Lombok
- SonarCloud

---

## Gestione delle dipendenze

Questo progetto non utilizza un file `requirements.txt`, perché non è un progetto Python.

Le dipendenze sono gestite tramite **Maven** nel file:

```text
pom.xml
```

Per questo motivo, non è necessario installare manualmente le librerie una per una. Maven scarica automaticamente le dipendenze necessarie durante la compilazione, il testing o l'avvio del progetto.

---

## Ambiente di riferimento

Il progetto è stato testato nel seguente ambiente:

```text
OS: Windows 11 amd64
Java: Eclipse Temurin JDK 17.0.17
Maven: Apache Maven 3.9.11
```

Requisiti minimi:

- JDK 17 o superiore;
- Maven installato e configurato nel PATH;
- MySQL installato e avviato solo se si utilizza la modalità DBMS.

---

## Installazione

Clonare il repository:

```bash
git clone https://github.com/enricobarbatano/Progetto_ISPW.git
```

Entrare nella cartella del progetto:

```bash
cd Progetto_ISPW
```

Verificare che Java e Maven siano disponibili:

```bash
java -version
mvn -version
```

Compilare il progetto:

```bash
mvn clean compile
```

---

## Avvio dell'applicazione

Il comando ufficiale per avviare il progetto in ambiente di sviluppo è:

```bash
mvn clean javafx:run
```

Durante l'avvio, l'applicazione richiede di selezionare:

```text
Seleziona Interfaccia:
1) CLI
2) GUI

Seleziona Modalità:
1) DEMO
2) STANDARD

Seleziona Persistenza:
1) FILE_SYSTEM
2) DBMS
```

La modalità DEMO utilizza dati in memoria e non richiede configurazione esterna.  
La modalità STANDARD permette invece di utilizzare persistenza su filesystem o database MySQL.

---

## Modalità di persistenza

### IN_MEMORY

La modalità **IN_MEMORY** mantiene i dati solo in memoria durante l'esecuzione dell'applicazione.  
È pensata principalmente per demo, test rapidi e sviluppo senza configurazione esterna.

### FILE_SYSTEM

La modalità **FILE_SYSTEM** salva i dati su file JSON locali.  
Questa soluzione permette di mantenere una persistenza semplice e facilmente ispezionabile.

Esempi di file gestiti:

```text
campi.json
prenotazioni.json
pagamenti.json
fatture.json
```

### DBMS MySQL

La modalità **DBMS** utilizza un database MySQL tramite JDBC.

È necessario avere:

- un server MySQL attivo;
- uno schema/database già creato;
- un utente con permessi adeguati;
- configurazione JDBC coerente con l'ambiente locale.

Esempio di URL JDBC:

```text
jdbc:mysql://localhost:3306/centro_sportivo?useSSL=false&serverTimezone=Europe/Rome
```

---

## Test

Per eseguire i test unitari:

```bash
mvn test
```

Per eseguire test e verifiche complete del progetto:

```bash
mvn verify
```

I test coprono principalmente controller logici, validazione degli input, casi di errore e interazione tra componenti tramite Mockito.

---

## Build e packaging

Per generare il package Maven:

```bash
mvn clean package
```

Il `pom.xml` include anche configurazioni per:

- compilazione Java 17;
- esecuzione JavaFX tramite Maven;
- test con JUnit 5;
- integration test tramite Failsafe;
- creazione di un fat JAR tramite Maven Shade Plugin;
- supporto a packaging tramite jpackage.

Per dettagli più avanzati di deployment e packaging, fare riferimento al branch `business`, se presente.

---

## Struttura del progetto

```text
.
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       ├── fxml/
│   │       └── css/
│   └── test/
│       └── java/
├── ISPW_Project.docx
├── README.md
└── LICENSE
```

### Descrizione delle cartelle principali

- `src/main/java`  
  Contiene il codice sorgente dell'applicazione.

- `src/main/resources`  
  Contiene le risorse dell'applicazione, come file FXML e CSS.

- `src/test/java`  
  Contiene i test unitari e di integrazione.

- `pom.xml`  
  File Maven per la gestione di dipendenze, plugin, build e test.

- `ISPW_Project.docx`  
  Documentazione tecnica del progetto.

---

## Documentazione

La documentazione tecnica del progetto è disponibile nel file:

```text
ISPW_Project.docx
```

Il documento include:

- Software Requirements Specification;
- user stories;
- functional requirements;
- use case;
- storyboard;
- VOPC/BCE diagram;
- design MVC;
- design pattern;
- activity diagram;
- sequence diagram;
- state diagram;
- testing;
- gestione delle eccezioni;
- persistenza;
- informazioni su SonarCloud.

---

## Qualità del codice

Il progetto è stato analizzato tramite SonarCloud per monitorare qualità, manutenibilità e code smell.

Report SonarCloud:

```text
https://sonarcloud.io/summary/new_code?id=enricobarbatano_Progetto_ISPW
```

Eventuali duplicazioni controllate nel layer DAO sono state mantenute consapevolmente per preservare la separazione tra provider di persistenza differenti, evitando accoppiamento eccessivo tra implementazioni DBMS e FileSystem.

---

## Demo

Video dimostrativo del progetto:

```text
https://youtu.be/CC-ZvJL1i8k?si=k9JEX4PZU3ckWnBc
```

Versione Google Drive:

```text
https://drive.google.com/file/d/1-k7ZenJfgM_cIhZukmkUf81lBWBZq6AD/view?usp=sharing
```

---

## Branching e workflow

Il repository può prevedere due branch principali:

- `project`  
  Branch base e pulito, dedicato allo sviluppo principale dell'applicazione.

- `business`  
  Branch con estensioni relative a deployment, packaging, runtime zip, script batch, jlink/jpackage e configurazioni aggiuntive.

Il branch `project` documenta principalmente lo sviluppo e l'esecuzione dell'applicazione in ambiente di sviluppo.

---

## File da ignorare

Si consiglia di ignorare artefatti generati, file temporanei e file locali tramite `.gitignore`, ad esempio:

```gitignore
target/
*.zip
*.drawio.drawio.xml
build_installer.cmd
```

---

## Licenza

Questo progetto è distribuito sotto licenza MIT.  
Consulta il file `LICENSE` per maggiori dettagli.

---

## Autore

**Enrico Barbatano**
