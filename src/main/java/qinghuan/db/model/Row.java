package qinghuan.db.model;

import java.util.HashMap;
import java.util.Map;

public class Row {
    private Map<String, Object> data;

    public Row() {
        this.data = new HashMap<>();
    }

    public void put(String column, Object value) {
        data.put(column, value);
    }

    public Map<String, Object> getData() {
        return data;
    }
}