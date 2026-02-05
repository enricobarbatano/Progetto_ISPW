# Contratti View ↔ GraphicController (progetto ISPW)

## Vincoli architetturali
- Le View **non devono dipendere dai Bean** del dominio/logic.
- Eccezione: la View può usare **solo** `SessioneUtenteBean`, perché serve a mostrare le funzionalità in base al ruolo.
- Le View **compilano dati grezzi** (form) e li passano al GraphicController.
- I GraphicController **adattano/aggregano** i dati grezzi e **creano i Bean** da inviare ai LogicController.
- I risultati dei LogicController tornano ai GraphicController, che li **traducono in payload semplici** (`Map<String, Object>`) per la View.
- Quando implementeremo le View **non verranno modificati i GraphicController**.

## Pattern di comunicazione
- View → GraphicController: chiamata diretta a metodi pubblici con input grezzo (String, int, float, Map).
- GraphicController → View: `navigator.goTo(route, params)`.
- View (route attiva) → `onShow(params)` per aggiornare UI con i parametri ricevuti.

## Parametri comuni
- `error`: messaggio d’errore
- `successo` / `message`: messaggio esito

## Route e payload

### login
- `route`: login
- `onShow(params)` usa solo `error` / `successo`
- Input view → controller: credenziali grezze (email, password)
- Output controller → view:
  - `error`: credenziali mancanti/non valide
  - `successo`: login ok (eventuale messaggio)

### registrazione
- `route`: registrazione
- `onShow(params)` usa solo `error` / `successo`
- Input view → controller: form grezzo (`nome`, `cognome`, `email`, `password`)
- Output controller → view:
  - `error`: validazione fallita / registrazione non riuscita
  - `successo`: registrazione riuscita

### account
- `route`: account
- `onShow(params)` usa `error` / `successo`
- Input view → controller:
  - aggiornamento: mappa con `idUtente`, `nome`, `cognome`, `email` (grezzi)
  - cambio password: `vecchiaPassword`, `nuovaPassword`
- Output controller → view:
  - `datiAccount`: mappa con `idUtente`, `nome`, `cognome`, `email`
  - `successo` / `error`

### prenotazione
- `route`: prenotazione
- Input view → controller:
  - ricerca: dati grezzi di ricerca (data, ora, durata, ecc.)
  - creazione: dati grezzi + `SessioneUtenteBean`
  - pagamento: dati grezzi + `SessioneUtenteBean`
- Output controller → view:
  - `slotDisponibili`: lista stringhe
  - `riepilogo`: mappa con `idPrenotazione`, `importoTotale`, `riepilogo`
  - `pagamento`: mappa con `successo`, `stato`, `messaggio`, `idTransazione`, `dataPagamento`
  - `successo` / `error`

### disdetta
- `route`: disdetta
- Input view → controller:
  - `idPrenotazione` + `SessioneUtenteBean`
- Output controller → view:
  - `prenotazioni`: lista stringhe
  - `idPrenotazione`: int
  - `anteprima`: mappa con `possibile`, `penale`
  - `successo` / `error`

### regole
- `route`: regole
- Input view → controller: parametri grezzi
- Output controller → view:
  - `campi`: lista stringhe
  - `idCampo`: int
  - `successo` / `error`

### penalita
- `route`: penalita
- Input view → controller: `email`, `idUtente`, `importo`, `motivazione` (grezzi)
- Output controller → view:
  - `email`
  - `successo` / `error`

## Nota su SessioneUtenteBean
- Tutte le view, **eccetto login e registrazione**, possono ricevere e usare `SessioneUtenteBean` per:
  - mostrare funzioni disponibili in base al ruolo
  - limitare/abilitare comandi della UI

## Riepilogo rapido (route → payload principali)
- login → `error`, `successo`
- registrazione → `error`, `successo`
- account → `datiAccount`, `successo`, `error`
- prenotazione → `slotDisponibili`, `riepilogo`, `pagamento`, `successo`, `error`
- disdetta → `prenotazioni`, `idPrenotazione`, `anteprima`, `successo`, `error`
- regole → `campi`, `idCampo`, `successo`, `error`
- penalita → `email`, `successo`, `error`

## Ruoli e funzionalità (linee guida view)
- `SessioneUtenteBean` guida la UI nel mostrare/abilitare funzioni.
- **Utente finale**: login, registrazione, prenotazione, disdetta, account, pagamento.
- **Gestore**: regole, penalità, gestione disponibilità, account.
- Le view devono filtrare le opzioni in base al ruolo senza toccare i controller grafici.
