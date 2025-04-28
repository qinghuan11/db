package qinghuan.db.query;

import qinghuan.db.model.Row;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    public Query parse(String query) {
        query = query.trim();
        if (query.startsWith("SELECT")) {
            return parseSelect(query);
        } else if (query.startsWith("INSERT")) {
            return parseInsert(query);
        } else if (query.startsWith("UPDATE")) {
            return parseUpdate(query);
        } else if (query.startsWith("DELETE")) {
            return parseDelete(query);
        } else if (query.startsWith("CREATE TABLE")) {
            return parseCreateTable(query);
        } else if (query.startsWith("BEGIN") || query.startsWith("COMMIT") || query.startsWith("ROLLBACK")) {
            return new Query(query, null, null, null);
        } else if (query.startsWith("CREATE USER")) {
            return parseCreateUser(query);
        } else if (query.startsWith("GRANT")) {
            return parseGrant(query);
        }
        throw new IllegalArgumentException("Unsupported query: " + query);
    }

    private Query parseSelect(String query) {
        Pattern pattern = Pattern.compile("SELECT \\* FROM (\\w+) WHERE (\\w+) = ([\\w\\d.]+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            String tableName = matcher.group(1);
            Map<String, Object> conditions = new HashMap<>();
            conditions.put(matcher.group(2), parseValue(matcher.group(3)));
            return new Query("SELECT", tableName, conditions, null);
        }
        throw new IllegalArgumentException("Invalid SELECT query");
    }

    private Query parseInsert(String query) {
        Pattern pattern = Pattern.compile("INSERT INTO (\\w+) \\((.+)\\) VALUES \\((.+)\\)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            String tableName = matcher.group(1);
            String[] columns = matcher.group(2).split(",\\s*");
            String[] values = matcher.group(3).split(",\\s*");
            Row row = new Row();
            for (int i = 0; i < columns.length; i++) {
                row.put(columns[i], parseValue(values[i]));
            }
            return new Query("INSERT", tableName, null, row);
        }
        throw new IllegalArgumentException("Invalid INSERT query");
    }

    private Query parseUpdate(String query) {
        Pattern pattern = Pattern.compile("UPDATE (\\w+) SET (.+) WHERE (\\w+) = ([\\w\\d.]+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            String tableName = matcher.group(1);
            String[] setParts = matcher.group(2).split(",\\s*");
            Row row = new Row();
            for (String part : setParts) {
                String[] kv = part.split("=");
                row.put(kv[0].trim(), parseValue(kv[1].trim()));
            }
            Map<String, Object> conditions = new HashMap<>();
            conditions.put(matcher.group(3), parseValue(matcher.group(4)));
            return new Query("UPDATE", tableName, conditions, row);
        }
        throw new IllegalArgumentException("Invalid UPDATE query");
    }

    private Query parseDelete(String query) {
        Pattern pattern = Pattern.compile("DELETE FROM (\\w+) WHERE (\\w+) = ([\\w\\d.]+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            String tableName = matcher.group(1);
            Map<String, Object> conditions = new HashMap<>();
            conditions.put(matcher.group(2), parseValue(matcher.group(3)));
            return new Query("DELETE", tableName, conditions, null);
        }
        throw new IllegalArgumentException("Invalid DELETE query");
    }

    private Query parseCreateTable(String query) {
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+) \\((.+)\\)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            String tableName = matcher.group(1);
            Map<String, Object> data = new HashMap<>();
            data.put("columns", matcher.group(2));
            return new Query("CREATE", tableName, null, null, data);
        }
        throw new IllegalArgumentException("Invalid CREATE TABLE query");
    }

    private Query parseCreateUser(String query) {
        Pattern pattern = Pattern.compile("CREATE USER (\\w+) WITH PASSWORD '(.+)' ROLE (\\w+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", matcher.group(1));
            data.put("password", matcher.group(2));
            data.put("role", matcher.group(3));
            return new Query("CREATE_USER", null, null, null, data);
        }
        throw new IllegalArgumentException("Invalid CREATE USER query");
    }

    private Query parseGrant(String query) {
        Pattern pattern = Pattern.compile("GRANT (\\w+) ON (\\w+) TO (\\w+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.matches()) {
            Map<String, Object> data = new HashMap<>();
            data.put("operation", matcher.group(1));
            data.put("tableName", matcher.group(2));
            data.put("username", matcher.group(3));
            return new Query("GRANT", null, null, null, data);
        }
        throw new IllegalArgumentException("Invalid GRANT query");
    }

    private Object parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                return value;
            }
        }
    }
}