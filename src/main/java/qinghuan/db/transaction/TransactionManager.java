package qinghuan.db.transaction;

import qinghuan.db.storage.LogManager;
import qinghuan.db.model.Table;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransactionManager {
    private Map<String, Table> snapshot;
    private LogManager logManager;
    private boolean inTransaction;

    public TransactionManager() {
        this.snapshot = new HashMap<>();
        this.logManager = new LogManager();
        this.inTransaction = false;
    }

    public void begin() throws IOException {
        if (inTransaction) {
            throw new IllegalStateException("Transaction already in progress");
        }
        inTransaction = true;
        snapshot.clear();
        logManager.startTransaction();
    }

    public void commit(Map<String, Table> tables) throws IOException {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        logManager.commit();
        snapshot.clear();
        inTransaction = false;
    }

    public void rollback(Map<String, Table> tables) throws IOException {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        for (Map.Entry<String, Table> entry : snapshot.entrySet()) {
            tables.put(entry.getKey(), entry.getValue());
        }
        logManager.rollback();
        snapshot.clear();
        inTransaction = false;
    }

    public void logOperation(String tableName, Table table) throws IOException {
        if (inTransaction) {
            snapshot.putIfAbsent(tableName, table);
            logManager.logOperation(tableName, table);
        }
    }
}
