package qinghuan.db.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Schema {
    private String tableName;
    private Map<String, ColumnDefinition> columns;
    private String primaryKey;

    public Schema(String tableName) {
        this.tableName = tableName;
        this.columns = new HashMap<>();
    }

    public void addColumn(String name, String type, boolean notNull, boolean isPrimaryKey) {
        columns.put(name, new ColumnDefinition(name, type, notNull));
        if (isPrimaryKey) {
            primaryKey = name;
        }
    }

    public boolean validateRow(Row row) {
        for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
            String colName = entry.getKey();
            ColumnDefinition colDef = entry.getValue();
            if (!row.getData().containsKey(colName) && colDef.isNotNull()) {
                return false;
            }
            Object value = row.getData().get(colName);
            if (value != null && !isValidType(value, colDef.getType())) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidType(Object value, String type) {
        return switch (type) {
            case "int" -> value instanceof Integer;
            case "string" -> value instanceof String;
            case "double" -> value instanceof Double;
            default -> false;
        };
    }
}
