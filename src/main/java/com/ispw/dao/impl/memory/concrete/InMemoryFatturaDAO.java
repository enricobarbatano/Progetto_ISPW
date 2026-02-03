package com.ispw.dao.impl.memory.concrete;

import com.ispw.dao.impl.memory.In_MemoryDAO;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

import java.util.Comparator;

/**
 * DAO InMemory per Fattura.
 * - Store condiviso per classe (super(true)).
 * - Auto-assegna id se mancante (id=0) con "max+1".
 * - findLastByUtente: non supportato (manca idUtente nell'entity).
 */
public class InMemoryFatturaDAO extends In_MemoryDAO<Integer, Fattura> implements FatturaDAO {

    public InMemoryFatturaDAO() {
        super(true); // store condiviso per la classe DAO
    }

    @Override
    protected Integer getId(Fattura entity) {
        return entity.getIdFattura();
    }

    @Override
    public void store(Fattura entity) {
        // Auto-assegna l'ID se 0 (accademico): max+1 sullo store corrente
        if (entity.getIdFattura() == 0) {
            int next = store.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max().orElse(0) + 1;
            entity.setIdFattura(next);
        }
        super.store(entity);
    }

    @Override
    public Fattura findLastByUtente(int idUtente) {
        // Non supportabile senza idUtente nell'entity o senza join su Prenotazione.
        // Spiega chiaramente il motivo (utile nei test).
        throw new UnsupportedOperationException(
                "findLastByUtente non supportato in InMemoryFatturaDAO: Fattura non contiene idUtente.");
    }
}
