package qinghuan.db.storage;

import qinghuan.db.model.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LogManager {
    private static final String LOG_PATH = "src/main/resources/db/logs/transaction.log";

    // 确保 logs 目录和 transaction.log 文件存在
    private void ensureLogFileExists() throws IOException {
        Path logDir = Paths.get("src/main/resources/db/logs");
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir); // 创建 logs 目录
        }
        Path logFile = Paths.get(LOG_PATH);
        if (!Files.exists(logFile)) {
            Files.createFile(logFile); // 创建 transaction.log 文件
        }
    }

    public void startTransaction() throws IOException {
        ensureLogFileExists(); // 确保文件存在
        Files.writeString(Paths.get(LOG_PATH), "BEGIN\n", StandardOpenOption.APPEND);
    }

    public void logOperation(String tableName, Table table) throws IOException {
        ensureLogFileExists(); // 确保文件存在
        Files.writeString(Paths.get(LOG_PATH), "OP " + tableName + "\n", StandardOpenOption.APPEND);
    }

    public void commit() throws IOException {
        ensureLogFileExists(); // 确保文件存在
        Files.writeString(Paths.get(LOG_PATH), "COMMIT\n", StandardOpenOption.APPEND);
    }

    public void rollback() throws IOException {
        ensureLogFileExists(); // 确保文件存在
        Files.writeString(Paths.get(LOG_PATH), "ROLLBACK\n", StandardOpenOption.APPEND);
    }
}