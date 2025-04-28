package qinghuan.db.auth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private Map<String, User> users;
    private static final String USERS_FILE = "src/main/resources/db/users.json";
    private static final String PERMISSIONS_FILE = "src/main/resources/db/permissions.json";

    public AuthManager() throws IOException {
        users = new HashMap<>();
        ensureDirectoryExists();
        loadUsers();
        if (!users.containsKey("admin")) {
            createUser("admin", "admin123", Role.ADMIN, null);
        }
    }

    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            return user;
        }
        return null;
    }

    // 新增方法：仅检查用户是否存在
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public void createUser(String username, String password, Role role, User creator) throws IOException {
        if (creator != null && !creator.hasPermission("*", "MANAGE_USER")) {
            throw new SecurityException("No permission to manage users");
        }
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User(username, hashPassword(password), role);
        users.put(username, user);
        saveUsers();
    }

    // 其他方法保持不变...
    public void grantPermission(String username, String tableName, String operation, User creator) throws IOException {
        if (!creator.hasPermission("*", "MANAGE_USER")) {
            throw new SecurityException("No permission to manage permissions");
        }
        User user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.addPermission(new Permission(tableName, operation));
        savePermissions();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private void ensureDirectoryExists() throws IOException {
        Path dbPath = Paths.get("src/main/resources/db");
        if (!Files.exists(dbPath)) {
            Files.createDirectories(dbPath);
        }
        Path usersFilePath = Paths.get(USERS_FILE);
        if (!Files.exists(usersFilePath)) {
            Files.writeString(usersFilePath, "[]");
        }
        Path permissionsFilePath = Paths.get(PERMISSIONS_FILE);
        if (!Files.exists(permissionsFilePath)) {
            Files.writeString(permissionsFilePath, "[]");
        }
    }

    private void loadUsers() throws IOException {
        if (Files.exists(Paths.get(USERS_FILE))) {
            String content = Files.readString(Paths.get(USERS_FILE));
            JSONArray usersJson = new JSONArray(content);
            for (int i = 0; i < usersJson.length(); i++) {
                JSONObject userJson = usersJson.getJSONObject(i);
                User user = new User(
                        userJson.getString("username"),
                        userJson.getString("passwordHash"),
                        Role.valueOf(userJson.getString("role"))
                );
                users.put(user.getUsername(), user);
            }
        }
        loadPermissions();
    }

    private void loadPermissions() throws IOException {
        if (Files.exists(Paths.get(PERMISSIONS_FILE))) {
            String content = Files.readString(Paths.get(PERMISSIONS_FILE));
            JSONArray permsJson = new JSONArray(content);
            for (int i = 0; i < permsJson.length(); i++) {
                JSONObject permJson = permsJson.getJSONObject(i);
                String username = permJson.getString("username");
                User user = users.get(username);
                if (user != null) {
                    JSONArray permissions = permJson.getJSONArray("permissions");
                    for (int j = 0; j < permissions.length(); j++) {
                        JSONObject p = permissions.getJSONObject(j);
                        user.addPermission(new Permission(p.getString("tableName"), p.getString("operation")));
                    }
                }
            }
        }
    }

    private void saveUsers() throws IOException {
        JSONArray usersJson = new JSONArray();
        for (User user : users.values()) {
            JSONObject userJson = new JSONObject();
            userJson.put("username", user.getUsername());
            userJson.put("passwordHash", user.getPasswordHash());
            userJson.put("role", user.getRole().name());
            usersJson.put(userJson);
        }
        Files.writeString(Paths.get(USERS_FILE), usersJson.toString());
    }

    private void savePermissions() throws IOException {
        JSONArray permsJson = new JSONArray();
        for (User user : users.values()) {
            JSONObject permJson = new JSONObject();
            permJson.put("username", user.getUsername());
            JSONArray permissions = new JSONArray();
            for (Permission p : user.getPermissions()) {
                JSONObject pJson = new JSONObject();
                pJson.put("tableName", p.getTableName());
                pJson.put("operation", p.getOperation());
                permissions.put(pJson);
            }
            permJson.put("permissions", permissions);
            permsJson.put(permJson);
        }
        Files.writeString(Paths.get(PERMISSIONS_FILE), permsJson.toString());
    }
}