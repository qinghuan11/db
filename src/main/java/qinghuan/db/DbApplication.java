package qinghuan.db;

import qinghuan.db.auth.User;
import qinghuan.db.core.Database;
import qinghuan.db.query.QueryParser;
import qinghuan.db.query.QueryExecutor;
import qinghuan.db.storage.StorageManager;

import java.io.IOException;

public class DbApplication {

    public static void main(String[] args) throws Exception {
        // 清理之前的数据
        StorageManager storageManager = new StorageManager();
        storageManager.clearAllData();

        // 初始化数据库
        Database db = new Database();
        QueryParser parser = new QueryParser();
        QueryExecutor executor = new QueryExecutor(db);

        // 登录管理员
        db.login("admin", "admin123");

        // 检查并创建用户（避免重复创建）
        if (!db.userExists("alice")) {
            executor.execute(parser.parse("CREATE USER alice WITH PASSWORD 'alice123' ROLE USER"));
        }
        if (!db.userExists("bob")) {
            executor.execute(parser.parse("CREATE USER bob WITH PASSWORD 'bob123' ROLE GUEST"));
        }

        // 授予权限
        executor.execute(parser.parse("GRANT INSERT ON students TO alice"));

        // 创建表
        executor.execute(parser.parse("CREATE TABLE students (id int PRIMARY_KEY, name string NOT_NULL, grade double)"));

        // 登录 Alice 并插入数据
        db.login("alice", "alice123");
        executor.execute(parser.parse("INSERT INTO students (id, name, grade) VALUES (1, Alice, 95.5)"));

        // 事务操作
        executor.execute(parser.parse("BEGIN"));
        executor.execute(parser.parse("INSERT INTO students (id, name, grade) VALUES (2, Bob, 88.0)"));
        executor.execute(parser.parse("COMMIT"));

        // 登录 Bob（GUEST）尝试插入（应失败）
        db.login("bob", "bob123");
        try {
            executor.execute(parser.parse("INSERT INTO students (id, name, grade) VALUES (3, Charlie, 90.0)"));
        } catch (SecurityException e) {
            System.out.println("Bob insert failed: " + e.getMessage());
        }

        // Bob 查询数据（应成功）
        executor.execute(parser.parse("SELECT * FROM students WHERE id = 1"))
                .forEach(row -> System.out.println(row.getData()));
    }
}
