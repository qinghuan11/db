package qinghuan.db.auth;

import java.util.HashMap;
import java.util.Map;

public enum Role {
    ADMIN, USER, GUEST;

    private static final Map<Role, Map<String, Boolean>> rolePermissions = new HashMap<>();

    static {
        Map<String, Boolean> adminPerms = new HashMap<>();
        adminPerms.put("CREATE_TABLE", true);
        adminPerms.put("DROP_TABLE", true);
        adminPerms.put("SELECT", true);
        adminPerms.put("INSERT", true);
        adminPerms.put("UPDATE", true);
        adminPerms.put("DELETE", true);
        adminPerms.put("MANAGE_USER", true);
        rolePermissions.put(ADMIN, adminPerms);

        Map<String, Boolean> userPerms = new HashMap<>();
        userPerms.put("SELECT", true);
        userPerms.put("INSERT", true);
        userPerms.put("UPDATE", true);
        userPerms.put("DELETE", true);
        rolePermissions.put(USER, userPerms);

        Map<String, Boolean> guestPerms = new HashMap<>();
        guestPerms.put("SELECT", true);
        rolePermissions.put(GUEST, guestPerms);
    }

    public boolean hasPermission(String tableName, String operation) {
        return rolePermissions.get(this).getOrDefault(operation, false);
    }
}
