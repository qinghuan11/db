package qinghuan.db.controller;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import qinghuan.db.model.Row;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired
    private RedisTemplate<String, List<Row>> redisTemplate;

    private Connection getConnection() throws SQLException {
        String dbPath = "jdbc:sqlite:/app/src/main/resources/db/data.db";
        if (!new java.io.File("/app/src/main/resources/db/data.db").exists()) {
            dbPath = "jdbc:sqlite:src/main/resources/db/data.db";
        }
        Connection conn = DriverManager.getConnection(dbPath);
        conn.setAutoCommit(true);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return conn;
    }

    @GetMapping("/{tableName}")
    public List<Row> getRows(
            @PathVariable String tableName,
            @RequestParam Map<String, Object> conditions) throws SQLException {
        String cacheKey = tableName + ":" + conditions.toString();
        List<Row> cachedRows = redisTemplate.opsForValue().get(cacheKey);
        if (cachedRows != null) {
            return cachedRows;
        }

        List<Row> rows = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditionsList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                conditionsList.add(entry.getKey() + " = ?");
            }
            sql.append(String.join(" AND ", conditionsList));
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                stmt.setObject(index++, entry.getValue());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Row row = new Row();
                    row.put("id", rs.getInt("id"));
                    row.put("name", rs.getString("name"));
                    row.put("grade", rs.getDouble("grade"));
                    rows.add(row);
                }
            }
        }

        redisTemplate.opsForValue().set(cacheKey, rows, 10, TimeUnit.MINUTES);
        return rows;
    }

    @PostMapping("/{tableName}/import")
    public ResponseEntity<String> importData(
            @PathVariable String tableName,
            @RequestParam("file") MultipartFile file) throws SQLException, IOException, CsvValidationException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        try (Connection conn = getConnection();
             CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = csvReader.readNext();
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                String sql = "INSERT INTO " + tableName + " (id, name, grade) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(row[0]));
                    stmt.setString(2, row[1]);
                    stmt.setDouble(3, Double.parseDouble(row[2]));
                    stmt.executeUpdate();
                }
            }
            return ResponseEntity.ok("Data imported successfully");
        }
    }

    @GetMapping("/{tableName}/export")
    public ResponseEntity<byte[]> exportData(@PathVariable String tableName) throws SQLException, IOException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(baos))) {
            csvWriter.writeAll(rs, true);
            csvWriter.flush();
            byte[] bytes = baos.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tableName + ".csv")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        }
    }
}