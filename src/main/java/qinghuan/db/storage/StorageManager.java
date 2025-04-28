package qinghuan.db.storage;

import qinghuan.db.model.Schema;
import qinghuan.db.model.Table;
import qinghuan.db.model.Row;
import qinghuan.db.model.ColumnDefinition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private static final String DB_PATH = "src/main/resources/db/";
    private static final String METADATA_FILE = DB_PATH + "metadata.json";

    public void saveTable(Table table) throws IOException {
        JSONObject tableJson = new JSONObject();
        tableJson.put("schema", schemaToJson(table.getSchema()));
        tableJson.put("data", rowsToJson(table.getRows()));
        Files.writeString(Paths.get(DB_PATH + "tables/" + table.getSchema().getTableName() + ".json"), tableJson.toString());
    }

    public Table loadTable(String tableName) throws IOException {
        String content = Files.readString(Paths.get(DB_PATH + "tables/" + tableName + ".json"));
        JSONObject tableJson = new JSONObject(content);
        Schema schema = jsonToSchema(tableJson.getJSONObject("schema"));
        List<Row> rows = jsonToRows(tableJson.getJSONArray("data"));
        Table table = new Table(schema);
        rows.forEach(table::insert);
        return table;
    }

    public void saveMetadata(Map<String, Schema> schemas) throws IOException {
        JSONObject metadata = new JSONObject();
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            metadata.put(entry.getKey(), schemaToJson(entry.getValue()));
        }
        Files.writeString(Paths.get(METADATA_FILE), metadata.toString());
    }

    private JSONObject schemaToJson(Schema schema) {
        JSONObject schemaJson = new JSONObject();
        schemaJson.put("tableName", schema.getTableName());
        schemaJson.put("primaryKey", schema.getPrimaryKey() != null ? schema.getPrimaryKey() : "");
        JSONObject columnsJson = new JSONObject();
        for (Map.Entry<String, ColumnDefinition> entry : schema.getColumns().entrySet()) {
            JSONObject colJson = new JSONObject();
            colJson.put("type", entry.getValue().getType());
            colJson.put("notNull", entry.getValue().isNotNull());
            columnsJson.put(entry.getKey(), colJson);
        }
        schemaJson.put("columns", columnsJson);
        return schemaJson;
    }

    private Schema jsonToSchema(JSONObject schemaJson) {
        Schema schema = new Schema(schemaJson.getString("tableName"));
        JSONObject columns = schemaJson.getJSONObject("columns");
        String primaryKey = schemaJson.getString("primaryKey");
        for (String colName : columns.keySet()) {
            JSONObject colJson = columns.getJSONObject(colName);
            boolean isPrimaryKey = colName.equals(primaryKey);
            schema.addColumn(colName, colJson.getString("type"), colJson.getBoolean("notNull"), isPrimaryKey);
        }
        return schema;
    }

    private JSONArray rowsToJson(List<Row> rows) {
        JSONArray rowsJson = new JSONArray();
        for (Row row : rows) {
            rowsJson.put(new JSONObject(row.getData()));
        }
        return rowsJson;
    }

    private List<Row> jsonToRows(JSONArray rowsJson) {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < rowsJson.length(); i++) {
            JSONObject rowJson = rowsJson.getJSONObject(i);
            Row row = new Row();
            for (String key : rowJson.keySet()) {
                row.put(key, rowJson.get(key));
            }
            rows.add(row);
        }
        return rows;
    }

    public void clearAllData() {
        try {
            // 删除元数据文件
            Path metadataPath = Paths.get(METADATA_FILE);
            if (Files.exists(metadataPath)) {
                Files.delete(metadataPath);
            }

            // 删除 tables 目录下的所有表文件
            Path tablesDirPath = Paths.get(DB_PATH + "tables/");
            if (Files.exists(tablesDirPath)) {
                File tablesDir = tablesDirPath.toFile();
                File[] tableFiles = tablesDir.listFiles();
                if (tableFiles != null) {
                    for (File file : tableFiles) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                }
            }

            // 确保目录存在
            Files.createDirectories(Paths.get(DB_PATH));
            Files.createDirectories(Paths.get(DB_PATH + "tables/"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear database data: " + e.getMessage(), e);
        }
    }
}