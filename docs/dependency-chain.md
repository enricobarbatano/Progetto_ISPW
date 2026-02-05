# Catena delle dipendenze (ISPW)

## Obiettivo
Questa catena evidenzia il **disaccoppiamento** tra UI, controller grafici, logica applicativa e persistenza, rispettando i contratti in [docs/graphic-controller-contracts.md](graphic-controller-contracts.md).

## Catena principale (alto → basso)
1. **View (CLI/GUI)**
   - Dipende solo dai **GraphicController** e da **SessioneUtenteBean** (eccezione prevista).
   - Passa **dati grezzi** (String/int/Map) e riceve **payload semplici**.

2. **GraphicController (CLI/GUI)**
   - Adatta i dati grezzi → **Bean**.
   - Dipende solo dai **LogicController** necessari al proprio caso d’uso.
   - Restituisce alla View solo **Map/Stringhe** tramite `navigator.goTo(...)`.

3. **LogicController**
   - Orchestrano i casi d’uso (prenotazione, regole, account, disdetta, penalità, accesso).
   - Dipendono solo da **DAO interfaces** (tramite factory) e da altri controller logici “secondari” (es. pagamento/fattura/notifica), mantenendo la logica applicativa centralizzata.

4. **DAO Interfaces**
   - Contratti di accesso ai dati (nessuna dipendenza verso UI o controller grafici).

5. **DAO Implementazioni (DBMS/FileSystem/InMemory)**
   - Solo persistenza e mapping entity.

6. **Entity / Model**
   - Oggetti di dominio e stati del sistema.

## Dipendenze consentite (riassunto)
- View → GraphicController
- GraphicController → LogicController
- LogicController → DAO interfaces (+ altri LogicController di supporto)
- DAO interface → DAO implementation → storage

## Dipendenze vietate (riassunto)
- View → Bean di dominio (eccetto `SessioneUtenteBean`)
- View → DAO / LogicController
- GraphicController → DAO

## Note su disaccoppiamento
- I **controller grafici astratti** non dipendono più dai LogicController: le dipendenze sono nei **GUI/CLI GraphicController** concreti.
- Ogni controller grafico dipende **solo** dal logic controller del proprio caso d’uso (es. prenotazione → `LogicControllerPrenotazioneCampo`).
- I LogicController restano **stateless**: nessuno stato persistente tra chiamate.

## Esempio sintetico (prenotazione)
View → GUIGraphicControllerPrenotazione → LogicControllerPrenotazioneCampo → DAO interfaces → DAO impl → DB

## Conferma contratti
Questa catena preserva responsabilità separate e minimizza le dipendenze trasversali, mantenendo alto il disaccoppiamento.
