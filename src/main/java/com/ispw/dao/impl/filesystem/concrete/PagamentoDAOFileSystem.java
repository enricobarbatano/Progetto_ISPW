package com.ispw.dao.impl.filesystem.concrete;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.ispw.dao.exception.DaoException;
import com.ispw.dao.impl.base.BasePagamentoDAO;
import com.ispw.dao.impl.filesystem.FileSystemDAO;
import com.ispw.model.entity.Pagamento;

/**
 * FileSystem Pagamento DAO implemented as subclass of BasePagamentoDAO.
 * Implements raw I/O reading/writing a map serialized on disk.
 */
public class PagamentoDAOFileSystem extends BasePagamentoDAO {

    private final Path filePath;
    private final FileSystemDAO.JavaBinaryMapCodec<Integer, Pagamento> codec = new FileSystemDAO.JavaBinaryMapCodec<>();

    public PagamentoDAOFileSystem(Path storageDir) {
        super(true); // persistent
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new DaoException("Impossibile creare directory storage: " + storageDir, e);
        }
        this.filePath = storageDir.resolve("pagamento.ser");
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Pagamento> readAll() {
        try {
            Optional<Map<Integer, Pagamento>> maybe = codec.read(filePath);
            return maybe.orElseGet(ConcurrentHashMap::new);
        } catch (DaoException e) {
            throw e;
        }
    }

    @Override
    protected Pagamento rawLoad(Integer id) {
        if (id == null) return null;
        Map<Integer, Pagamento> data = readAll();
        return data.get(id);
    }

    @Override
    protected void rawStore(Pagamento entity) {
        if (entity == null) return;
        Map<Integer, Pagamento> data = readAll();

        if (entity.getIdPagamento() == 0) {
            int next = data.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            entity.setIdPagamento(next);
        }
        data.put(entity.getIdPagamento(), entity);

        // atomic write: tmp -> move
        try {
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            codec.write(tmp, data);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DaoException("Errore scrittura su file: " + filePath, e);
        }
    }

    @Override
    protected void rawDelete(Integer id) {
        if (id == null) return;
        Map<Integer, Pagamento> data = readAll();
        data.remove(id);
        try {
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            codec.write(tmp, data);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DaoException("Errore scrittura su file: " + filePath, e);
        }
    }

    @Override
    protected Pagamento rawFindByPrenotazione(int idPrenotazione) {
        Map<Integer, Pagamento> data = readAll();
        return data.values().stream()
                .filter(p -> p != null && p.getIdPrenotazione() == idPrenotazione)
                .sorted((a, b) -> {
                    if (a.getDataPagamento() == null && b.getDataPagamento() == null) {
                        return Integer.compare(b.getIdPagamento(), a.getIdPagamento());
                    }
                    if (a.getDataPagamento() == null) return 1;
                    if (b.getDataPagamento() == null) return -1;
                    int cmp = b.getDataPagamento().compareTo(a.getDataPagamento());
                    if (cmp != 0) return cmp;
                    return Integer.compare(b.getIdPagamento(), a.getIdPagamento());
                })
                .findFirst()
                .orElse(null);
    }
}
