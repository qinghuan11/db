package qinghuan.db.query;

import qinghuan.db.core.Database;
import qinghuan.db.model.Row;
import qinghuan.db.model.Schema;
import qinghuan.db.model.ColumnDefinition;

import java.io.IOException;
import java.util.List;

public class QueryExecutor {
    private Database database;

    public QueryExecutor(Database database) {
        this.database = database;
    }

    public List<Row> execute(Query query) throws IOException {
        switch (query.getType()) {
            case "SELECT":
                return database.select(query.getTableName(), query.getConditions());
            case "INSERT":
                database.insert(query.getTableName(), query.getRow());
                return null;
            case "UPDATE":
                database.update(query.getTableName(), query.getConditions(), query.getRow());
                return null;
            case "DELETE":
                database.delete(query.getTableName(), query.getConditions());
                return null;
            case "CREATE":
                Schema schema = parseSchema(query.getTableName(), (String) query.getData().get("columns"));
                database.createTable(schema);
                return null;
            case "BEGIN":
                database.beginTransaction();
                return null;
            case "COMMIT":
                database.commitTransaction();
                return null;
            case "ROLLBACK":
                database.rollbackTransaction();
                return null;
            case "CREATE_USER":
                database.createUser(
                        (String) query.getData().get("username"),
                        (String) query.getData().get("password"),
                        (String) query.getData().get("role")
                );
                return null;
            case "GRANT":
                database.grantPermission(
                        (String) query.getData().get("username"),
                        (String) query.getData().get("tableName"),
                        (String) query.getData().get("operation")
                );
                return null;
            default:
                throw new IllegalArgumentException("Unknown query type: " + query.getType());
        }
    }

    private Schema parseSchema(String tableName, String columnsDef) {
        Schema schema = new Schema(tableName);
        String[] columns = columnsDef.split(",\\s*");
        for (String col : columns) {
            String[] parts = col.split("\\s+");
            String colName = parts[0];
            String type = parts[1];
            boolean notNull = parts.length > 2 && parts[2].equals("NOT_NULL");
            boolean primaryKey = parts.length > 2 && parts[2].equals("PRIMARY_KEY");
            schema.addColumn(colName, type, notNull, primaryKey);
        }
        return schema;
    }
}
