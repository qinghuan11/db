package qinghuan.db;

import qinghuan.db.model.Schema;
import qinghuan.db.model.Table;
import qinghuan.db.model.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TableTest {
    private Table table;
    private Schema schema;

    @BeforeEach
    void setUp() {
        schema = new Schema("test");
        schema.addColumn("id", "int", true, true);
        schema.addColumn("name", "string", true, false);
        table = new Table(schema);
    }

    @Test
    void testInsert() {
        Row row = new Row();
        row.put("id", 1);
        row.put("name", "Alice");
        table.insert(row);
        assertEquals(1, table.getRows().size());
    }

    @Test
    void testDuplicatePrimaryKey() {
        Row row1 = new Row();
        row1.put("id", 1);
        row1.put("name", "Alice");
        table.insert(row1);

        Row row2 = new Row();
        row2.put("id", 1);
        row2.put("name", "Bob");
        assertThrows(IllegalArgumentException.class, () -> table.insert(row2));
    }

    @Test
    void testSelectWithIndex() {
        Row row1 = new Row();
        row1.put("id", 1);
        row1.put("name", "Alice");
        table.insert(row1);

        Row row2 = new Row();
        row2.put("id", 2);
        row2.put("name", "Bob");
        table.insert(row2);

        Map<String, Object> conditions = new HashMap<>();
        conditions.put("id", 1);
        List<Row> result = table.select(conditions);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getData().get("name"));
    }
}