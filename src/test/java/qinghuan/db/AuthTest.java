package qinghuan.db;

import qinghuan.db.auth.AuthManager;
import qinghuan.db.auth.Role;
import qinghuan.db.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {
    private AuthManager authManager;

    @BeforeEach
    void setUp() throws IOException {
        authManager = new AuthManager();
    }

    @Test
    void testCreateUser() throws IOException {
        authManager.createUser("testuser", "password123", Role.USER, authManager.authenticate("admin", "admin123"));
        User user = authManager.authenticate("testuser", "password123");
        assertNotNull(user);
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testPermissionCheck() throws IOException {
        User admin = authManager.authenticate("admin", "admin123");
        assertTrue(admin.hasPermission("students", "CREATE_TABLE"));
        User guest = authManager.authenticate("admin", "admin123");
        authManager.createUser("guestuser", "guest123", Role.GUEST, admin);
        assertFalse(guest.hasPermission("students", "INSERT"));
    }
}