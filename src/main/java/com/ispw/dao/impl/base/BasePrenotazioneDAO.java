package com.ispw.dao.impl.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Fattura;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.entity.Prenotazione;
import com.ispw.model.enums.StatoPrenotazione;

/**
 * Base concrete Prenotazione DAO implementing cache-first behavior.
 * Acts as the IN_MEMORY provider when instantiated directly.
 *
 * A2 strategy: compose ALWAYS.
 * Il DAO "padre" compone Prenotazione chiedendo Campo, Pagamento e Fattura
 * ai rispettivi DAO tramite DAOFactory e interfacce.
 *
 * NOTE:
 * Per evitare cicli di composizione, CampoDAO non deve comporre Prenotazioni.
 */
public class BasePrenotazioneDAO implements PrenotazioneDAO {

    /**
     * Ordine cronologico crescente:
     * data ASC, oraInizio ASC, id ASC.
     */
    private static final Comparator<Prenotazione> ORDER_BY_DATA_ORA_ID =
            Comparator.comparing(Prenotazione::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Prenotazione::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Prenotazione::getIdPrenotazione);

    protected final Map<Integer, Prenotazione> cache = new ConcurrentHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final boolean persistent;

    public BasePrenotazioneDAO() {
        this(false);
    }

    protected BasePrenotazioneDAO(boolean persistent) {
        this.persistent = persistent;
    }

    // -----------------------
    // Protected raw operations
    // Subclasses override these to provide DB/FS I/O.
    // -----------------------

    @SuppressWarnings("java:S1172")
    protected Prenotazione rawLoad(Integer id) {
        return null;
    }

    protected void rawStore(Prenotazione entity) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected void rawDelete(Integer id) {
        // no-op: base in-memory implementation
    }

    @SuppressWarnings("java:S1172")
    protected List<Prenotazione> rawFindByUtente(int idUtente) {
        return List.of();
    }

    @SuppressWarnings("java:S1172")
    protected List<Prenotazione> rawFindByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        return List.of();
    }

    /**
     * Raw hook per query "prenotazioni associate a un campo".
     * I provider persistenti lo implementano accedendo al proprio supporto:
     * - FileSystem: prenotazioni.json
     * - DBMS: tabella prenotazioni
     */
    @SuppressWarnings("java:S1172")
    protected List<Prenotazione> rawFindByCampo(int idCampo) {
        return List.of();
    }

    @SuppressWarnings("java:S1172")
    protected void rawUpdateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        // no-op: base in-memory implementation
    }

    // -----------------------
    // Public DAO API
    // -----------------------

    @Override
    public Prenotazione load(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }

        /*
         * Step 1:
         * cache-first. Prima si prova sempre a leggere dalla cache.
         */
        Prenotazione cached;
        lock.readLock().lock();
        try {
            cached = cache.get(id);
        } finally {
            lock.readLock().unlock();
        }

        /*
         * Step 2:
         * se la prenotazione è già in cache, viene composta e restituita.
         */
        if (cached != null) {
            composeIfNeeded(cached);
            return cached;
        }

        /*
         * Step 3:
         * se il DAO è persistente, il caricamento viene delegato al rawLoad
         * implementato dal DAO concreto DBMS/FS.
         */
        Prenotazione prenotazione;
        if (persistent) {
            prenotazione = rawLoad(id);
        } else {
            return null;
        }

        if (prenotazione == null) {
            return null;
        }

        /*
         * Step 4:
         * la prenotazione caricata dal provider persistente viene composta.
         */
        composeIfNeeded(prenotazione);

        /*
         * Step 5:
         * la prenotazione composta viene salvata in cache.
         */
        lock.writeLock().lock();
        try {
            cache.put(prenotazione.getIdPrenotazione(), prenotazione);
        } finally {
            lock.writeLock().unlock();
        }

        return prenotazione;
    }

    @Override
    public void store(Prenotazione entity) {
        if (entity == null) {
            return;
        }

        /*
         * Caso nuovo:
         * se id == 0, bisogna generare un id o lasciarlo generare al provider persistente.
         */
        if (entity.getIdPrenotazione() == 0) {
            storeNew(entity);
            return;
        }

        /*
         * Caso esistente:
         * prima compone, poi aggiorna cache e persistenza se necessario.
         */
        composeIfNeeded(entity);

        lock.writeLock().lock();
        try {
            cache.put(entity.getIdPrenotazione(), entity);
        } finally {
            lock.writeLock().unlock();
        }

        if (persistent) {
            rawStore(entity);
        }
    }

    private void storeNew(Prenotazione entity) {
        if (persistent) {
            /*
             * Provider persistente:
             * rawStore dovrebbe assegnare id, ad esempio con generated key DB
             * oppure con max+1 su file system.
             */
            rawStore(entity);

            int id = entity.getIdPrenotazione();

            if (id <= 0) {
                /*
                 * Fallback difensivo:
                 * se il provider non assegna id, viene assegnato usando la cache.
                 */
                lock.writeLock().lock();
                try {
                    int next = cache.keySet().stream()
                            .mapToInt(Integer::intValue)
                            .max()
                            .orElse(0) + 1;

                    entity.setIdPrenotazione(next);
                    cache.put(next, entity);
                } finally {
                    lock.writeLock().unlock();
                }

                return;
            }

            /*
             * Dopo l'id assegnato dal provider, la entity viene composta e messa in cache.
             */
            composeIfNeeded(entity);

            lock.writeLock().lock();
            try {
                cache.put(id, entity);
            } finally {
                lock.writeLock().unlock();
            }

            return;
        }

        /*
         * Provider in-memory:
         * genera id localmente usando la cache.
         */
        lock.writeLock().lock();
        try {
            int next = cache.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0) + 1;

            entity.setIdPrenotazione(next);
            composeIfNeeded(entity);
            cache.put(next, entity);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            return;
        }

        /*
         * Rimuove prima dalla cache.
         */
        lock.writeLock().lock();
        try {
            cache.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

        /*
         * Se persistente, propaga la delete al provider concreto.
         */
        if (persistent) {
            rawDelete(id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        /*
         * Controllo leggero su cache.
         */
        lock.readLock().lock();
        try {
            if (cache.containsKey(id)) {
                return true;
            }
        } finally {
            lock.readLock().unlock();
        }

        /*
         * Fallback raw solo se persistente.
         * Non forza composizione, per mantenere exists leggero.
         */
        if (persistent) {
            Prenotazione prenotazione = rawLoad(id);

            if (prenotazione != null) {
                lock.writeLock().lock();
                try {
                    cache.put(prenotazione.getIdPrenotazione(), prenotazione);
                } finally {
                    lock.writeLock().unlock();
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public Prenotazione create(Integer id) {
        Prenotazione prenotazione = new Prenotazione();

        if (id != null && id > 0) {
            prenotazione.setIdPrenotazione(id);
        }

        return prenotazione;
    }

    @Override
    public Prenotazione findById(int idPrenotazione) {
        return load(idPrenotazione);
    }

    @Override
    public List<Prenotazione> findByUtente(int idUtente) {
        final List<Prenotazione> result;

        if (persistent) {
            result = rawFindByUtente(idUtente);
        } else {
            result = findByUtenteFromCache(idUtente);
        }

        return prepareResultList(result);
    }

    @Override
    public List<Prenotazione> findByUtenteAndStato(int idUtente, StatoPrenotazione stato) {
        final List<Prenotazione> result;

        if (persistent) {
            result = rawFindByUtenteAndStato(idUtente, stato);
        } else {
            result = findByUtenteAndStatoFromCache(idUtente, stato);
        }

        return prepareResultList(result);
    }

    @Override
    public List<Prenotazione> findByCampo(int idCampo) {
        if (idCampo <= 0) {
            return new ArrayList<>();
        }

        final List<Prenotazione> result;

        if (persistent) {
            result = rawFindByCampo(idCampo);
        } else {
            result = findByCampoFromCache(idCampo);
        }

        return prepareResultList(result);
    }

    @Override
    public List<Prenotazione> findAttiveByCampo(int idCampo) {
        return findByCampo(idCampo).stream()
                .filter(p -> p != null && p.getStato() != StatoPrenotazione.ANNULLATA)
                .toList();
    }

    @Override
    public void updateStato(int idPrenotazione, StatoPrenotazione nuovoStato) {
        /*
         * S3824:
         * usa computeIfPresent al posto di get + if + put.
         * La logica resta identica: aggiorna solo se presente in cache.
         */
        lock.writeLock().lock();
        try {
            cache.computeIfPresent(idPrenotazione, (id, prenotazione) -> {
                prenotazione.setStato(nuovoStato);
                return prenotazione;
            });
        } finally {
            lock.writeLock().unlock();
        }

        /*
         * Se persistente, propaga la modifica al provider concreto.
         */
        if (persistent) {
            rawUpdateStato(idPrenotazione, nuovoStato);
        }
    }

    /**
     * Compatibilità: pulisce la cache, usato dai test.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -----------------------
    // Cache filter helpers
    // -----------------------

    private List<Prenotazione> findByUtenteFromCache(int idUtente) {
        List<Prenotazione> out = new ArrayList<>();

        lock.readLock().lock();
        try {
            for (Prenotazione prenotazione : cache.values()) {
                if (prenotazione != null && prenotazione.getIdUtente() == idUtente) {
                    out.add(prenotazione);
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return out;
    }

    private List<Prenotazione> findByUtenteAndStatoFromCache(int idUtente, StatoPrenotazione stato) {
        List<Prenotazione> out = new ArrayList<>();

        lock.readLock().lock();
        try {
            for (Prenotazione prenotazione : cache.values()) {
                if (prenotazione != null
                        && prenotazione.getIdUtente() == idUtente
                        && prenotazione.getStato() == stato) {
                    out.add(prenotazione);
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return out;
    }

    private List<Prenotazione> findByCampoFromCache(int idCampo) {
        List<Prenotazione> out = new ArrayList<>();

        lock.readLock().lock();
        try {
            for (Prenotazione prenotazione : cache.values()) {
                if (prenotazione != null && prenotazione.getIdCampo() == idCampo) {
                    out.add(prenotazione);
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return out;
    }

    private List<Prenotazione> prepareResultList(List<Prenotazione> result) {
        List<Prenotazione> safeResult = result != null ? result : new ArrayList<>();

        safeResult.sort(ORDER_BY_DATA_ORA_ID);

        /*
         * A2: compose ALL.
         * Prima pre-carica i campi associati, poi compone ogni prenotazione.
         */
        prewarmCampoCache(safeResult);

        for (Prenotazione prenotazione : safeResult) {
            composeIfNeeded(prenotazione);
        }

        cacheResultList(safeResult);

        return safeResult;
    }

    private void cacheResultList(List<Prenotazione> result) {
        lock.writeLock().lock();
        try {
            for (Prenotazione prenotazione : result) {
                if (prenotazione != null) {
                    cache.put(prenotazione.getIdPrenotazione(), prenotazione);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -----------------------
    // A2 Composition logic
    // -----------------------

    /**
     * Composizione A2:
     * compone Campo, Pagamento e Fattura usando DAOFactory + interfacce.
     *
     * IMPORTANT:
     * per evitare cicli di composizione, CampoDAO non deve comporre Prenotazioni.
     */
    protected void composeIfNeeded(Prenotazione prenotazione) {
        if (prenotazione == null) {
            return;
        }

        composeCampo(prenotazione);
        composePagamento(prenotazione);
        composeFattura(prenotazione);
    }

    /**
     * Compone il Campo tramite CampoDAO.
     */
    private void composeCampo(Prenotazione prenotazione) {
        if (prenotazione.getCampo() != null || prenotazione.getIdCampo() <= 0) {
            return;
        }

        CampoDAO campoDAO = DAOFactory.getInstance().getCampoDAO();
        Campo campo = campoDAO.load(prenotazione.getIdCampo());

        if (campo != null) {
            prenotazione.setCampo(campo);
        }
    }

    /**
     * Compone il Pagamento tramite PagamentoDAO.
     */
    private void composePagamento(Prenotazione prenotazione) {
        if (prenotazione.getPagamento() != null || prenotazione.getIdPrenotazione() <= 0) {
            return;
        }

        PagamentoDAO pagamentoDAO = DAOFactory.getInstance().getPagamentoDAO();
        Pagamento pagamento = pagamentoDAO.findByPrenotazione(prenotazione.getIdPrenotazione());

        if (pagamento != null) {
            prenotazione.setPagamento(pagamento);
        }
    }

    /**
     * Compone la Fattura tramite FatturaDAO.
     */
    private void composeFattura(Prenotazione prenotazione) {
        if (prenotazione.getFattura() != null) {
            return;
        }

        /*
         * L'interfaccia FatturaDAO disponibile espone findLastByUtente.
         * Quindi la composizione usa il metodo pubblico dell'interfaccia.
         */
        FatturaDAO fatturaDAO = DAOFactory.getInstance().getFatturaDAO();
        Fattura fattura = fatturaDAO.findLastByUtente(prenotazione.getIdUtente());

        if (fattura != null) {
            prenotazione.setFattura(fattura);
        }
    }

    /**
     * Pre-carica i campi associati alle prenotazioni.
     */
    private void prewarmCampoCache(List<Prenotazione> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        CampoDAO campoDAO = DAOFactory.getInstance().getCampoDAO();
        Set<Integer> ids = collectCampoIds(list);

        for (Integer idCampo : ids) {
            campoDAO.load(idCampo);
        }
    }

    /**
     * Raccoglie gli id campo distinti dalle prenotazioni.
     */
    private Set<Integer> collectCampoIds(List<Prenotazione> list) {
        Set<Integer> ids = new HashSet<>();

        for (Prenotazione prenotazione : list) {
            if (prenotazione != null && prenotazione.getIdCampo() > 0) {
                ids.add(prenotazione.getIdCampo());
            }
        }

        return ids;
    }
}