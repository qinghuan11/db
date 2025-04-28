package qinghuan.db.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class User {
    private String username;
    private String passwordHash;
    private Role role;
    private Set<Permission> permissions = new HashSet<>();

    // 自定义构造函数，仅包含 username, passwordHash, role
    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.permissions = new HashSet<>(); // 默认初始化为空集合
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public boolean hasPermission(String tableName, String operation) {
        return permissions.contains(new Permission(tableName, operation)) ||
                permissions.contains(new Permission("*", operation)) ||
                role.hasPermission(tableName, operation);
    }
}