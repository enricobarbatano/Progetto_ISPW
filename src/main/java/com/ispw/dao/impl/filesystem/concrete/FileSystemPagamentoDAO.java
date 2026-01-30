package com.ispw.dao.impl.filesystem.concrete;

import java.nio.file.Path;

import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;

public class FileSystemPagamentoDAO extends FileSystemDAO<Integer, Pagamento> implements PagamentoDAO {

    public FileSystemPagamentoDAO(Path storageDir) {
        super(storageDir, "pagamento.ser", new JavaBinaryMapCodec<>());
    }

    @Override
    protected Integer getId(Pagamento entity) {
        // TODO: return entity.getIdPagamento();
        throw new UnsupportedOperationException("TODO: Pagamento.getIdPagamento()");
    }

    @Override
    public Pagamento findByPrenotazione(int idPrenotazione) {
        throw new UnsupportedOperationException("TODO: findByPrenotazione()");
    }
}
