package qinghuan.db.query;

import qinghuan.db.model.Row;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
public class Query {
    private String type;
    private String tableName;
    private Map<String, Object> conditions;
    private Row row;
    private Map<String, Object> data;

    public Query(String type, String tableName, Map<String, Object> conditions, Row row) {
        this(type, tableName, conditions, row, null);
    }
}
