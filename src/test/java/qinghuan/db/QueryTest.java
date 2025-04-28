package qinghuan.db;

import qinghuan.db.core.Database;
import qinghuan.db.query.Query;
import qinghuan.db.query.QueryParser;
import qinghuan.db.query.QueryExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {
    private Database database;
    private QueryParser parser;
    private QueryExecutor executor;

    @BeforeEach
    void setUp() throws IOException {
        database = new Database();
        parser = new QueryParser();
        executor = new QueryExecutor(database);
        database.login("admin", "admin123");
    }

    @Test
    void testSelectQuery() throws IOException {
        executor.execute(parser.parse("CREATE TABLE test (id int PRIMARY_KEY, name string NOT_NULL)"));
        executor.execute(parser.parse("INSERT INTO test (id, name) VALUES (1, Alice)"));
        Query query = parser.parse("SELECT * FROM test WHERE id = 1");
        assertEquals("SELECT", query.getType());
    }
}