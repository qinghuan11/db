# 📚 轻量级DB 项目 - 轻量级数据库实现 🚀

欢迎使用 **DB** 项目！这是一个基于 Java 的轻量级数据库实现，集成了用户认证、权限管理、事务支持和监控功能。非常适合学习数据库原理或开发小型应用！💡

## ✨ 项目功能

- **用户认证与权限管理** 🔒\
  支持用户角色（ADMIN、USER、GUEST）和权限控制，确保数据安全。

- **表操作** 📊\
  支持基本的表创建、插入、查询、更新和删除操作。

- **事务支持** 🔄\
  提供事务管理（BEGIN、COMMIT、ROLLBACK），保证数据一致性。

- **索引优化** ⚡\
  使用 B+ 树索引，提升查询性能。

- **监控与可视化** 📈\
  集成 Prometheus 和 Grafana，实时监控应用性能。

- **Redis 缓存** 🗄️\
  使用 Redis 缓存查询结果，加速数据访问。

## 🛠️ 安装步骤

### 1. 克隆项目

```bash
git clone https://github.com/qinghuan11/db.git
cd db
```

### 2. 安装依赖

确保已安装 Java 21 和 Maven，然后运行：

```bash
./mvnw install
```

### 3. 配置环境

- 确保 Docker 和 Docker Compose 已安装，用于运行 Redis、Prometheus 和 Grafana。
- 检查 `application.yml` 文件，确保 Redis 和其他服务配置正确。

### 4. 启动服务

使用 Docker Compose 启动所有服务：

```bash
docker-compose up -d
```

### 5. 运行应用

启动 Spring Boot 应用：

```bash
./mvnw spring-boot:run
```

🎉 应用将在 `http://localhost:8080` 运行！

## 📖 使用方法

### 1. 登录

使用默认管理员账户登录：

- 用户名：`admin`
- 密码：`admin123`

通过 API 登录：

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
```

### 2. 创建表

使用 SQL 语句创建表：

```sql
CREATE TABLE students (id int PRIMARY_KEY, name string NOT_NULL, grade double)
```

### 3. 插入数据

插入数据到表：

```sql
INSERT INTO students (id, name, grade) VALUES (1, "Alice", 95.5)
```

### 4. 查询数据

查询表中的数据：

```sql
SELECT * FROM students WHERE id = 1
```

### 5. 监控

- 访问 Prometheus：`http://localhost:9090`
- 访问 Grafana：`http://localhost:3000`（默认账户：admin/admin）

## 🧑‍💻 贡献

欢迎为项目贡献代码！请遵循以下步骤：

- Fork 本仓库。
- 创建你的功能分支：

  ```bash
  git checkout -b feature/新功能
  ```
- 提交更改：

  ```bash
  git commit -m "添加新功能 🎉"
  ```
- 推送到远程分支：

  ```bash
  git push origin feature/新功能
  ```
- 创建 Pull Request。



---


