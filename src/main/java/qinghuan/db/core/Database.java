package qinghuan.db.core;

import qinghuan.db.auth.AuthManager;
import qinghuan.db.auth.Role;
import qinghuan.db.auth.User;
import qinghuan.db.model.Schema;
import qinghuan.db.model.Table;
import qinghuan.db.model.Row;
import qinghuan.db.storage.StorageManager;
import qinghuan.db.transaction.TransactionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private Map<String, Table> tables;
    private StorageManager storageManager;
    private TransactionManager transactionManager;
    private AuthManager authManager;
    private User currentUser;

    public Database() throws IOException {
        this.tables = new HashMap<>();
        this.storageManager = new StorageManager();
        this.transactionManager = new TransactionManager();
        this.authManager = new AuthManager();
    }

    public User login(String username, String password) {
        currentUser = authManager.authenticate(username, password);
        if (currentUser == null) {
            throw new SecurityException("Invalid username or password");
        }
        return currentUser;
    }

    // 新增方法：检查用户是否存在
    public User authenticate(String username) {
        return authManager.authenticate(username, "dummy_password"); // 密码不重要，仅用于检查用户是否存在
    }
    // 修改方法：仅检查用户是否存在，不验证密码
    public boolean userExists(String username) {
        return authManager.userExists(username);
    }
    // 其他方法保持不变...
    public void createUser(String username, String password, String role) throws IOException {
        checkPermission("*", "MANAGE_USER");
        authManager.createUser(username, password, Role.valueOf(role), currentUser);
    }

    public void grantPermission(String username, String tableName, String operation) throws IOException {
        checkPermission("*", "MANAGE_USER");
        authManager.grantPermission(username, tableName, operation, currentUser);
    }

    public void createTable(Schema schema) throws IOException {
        checkPermission("*", "CREATE_TABLE");
        Table table = new Table(schema);
        tables.put(schema.getTableName(), table);
        storageManager.saveMetadata(getSchemas());
    }

    public void insert(String tableName, Row row) throws IOException {
        checkPermission(tableName, "INSERT");
        Table table = tables.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }
        table.insert(row);
        transactionManager.logOperation(tableName, table);
        storageManager.saveTable(table);
    }

    public List<Row> select(String tableName, Map<String, Object> conditions) {
        checkPermission(tableName, "SELECT");
        Table table = tables.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }
        return table.select(conditions);
    }

    public void update(String tableName, Map<String, Object> conditions, Row newRow) throws IOException {
        checkPermission(tableName, "UPDATE");
        Table table = tables.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }
        table.update(conditions, newRow);
        transactionManager.logOperation(tableName, table);
        storageManager.saveTable(table);
    }

    public void delete(String tableName, Map<String, Object> conditions) throws IOException {
        checkPermission(tableName, "DELETE");
        Table table = tables.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }
        table.delete(conditions);
        transactionManager.logOperation(tableName, table);
        storageManager.saveTable(table);
    }

    public void beginTransaction() throws IOException {
        checkPermission("*", "SELECT");
        transactionManager.begin();
    }

    public void commitTransaction() throws IOException {
        checkPermission("*", "SELECT");
        transactionManager.commit(tables);
    }

    public void rollbackTransaction() throws IOException {
        checkPermission("*", "SELECT");
        transactionManager.rollback(tables);
    }

    private void checkPermission(String tableName, String operation) {
        if (currentUser == null) {
            throw new SecurityException("No user logged in");
        }
        if (!currentUser.hasPermission(tableName, operation)) {
            throw new SecurityException("User " + currentUser.getUsername() + " lacks permission for " + operation + " on " + tableName);
        }
    }

    private Map<String, Schema> getSchemas() {
        Map<String, Schema> schemas = new HashMap<>();
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            schemas.put(entry.getKey(), entry.getValue().getSchema());
        }
        return schemas;
    }
}
