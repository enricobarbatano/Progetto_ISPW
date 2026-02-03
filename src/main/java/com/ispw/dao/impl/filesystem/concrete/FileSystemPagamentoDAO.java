package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

/**
 * DAO FileSystem per Pagamento.
 * - Serializza una mappa <Integer, Pagamento> su disco (pagamento.ser).
 * - Tutto in cache in memoria, flush atomico su file tramite la base.
 * - Nessun SQL.
 */
public class FileSystemPagamentoDAO extends FileSystemDAO<Integer, Pagamento> implements PagamentoDAO {

    public FileSystemPagamentoDAO(Path storageDir) {
        super(storageDir, "pagamento.ser", new FileSystemDAO.JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Pagamento entity) {
        return entity.getIdPagamento();
    }

    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        // Ricerca semplice nella cache
        List<Pagamento> all = new ArrayList<>(cache.values());
        for (Pagamento p : all) {
            if (p.getIdPrenotazione() == idPrenotazione) {
                return p;
            }
        }
        return null;
    }
}
