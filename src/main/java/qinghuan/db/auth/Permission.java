package qinghuan.db.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class Permission {
    private String tableName;
    private String operation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return (tableName.equals("*") || that.tableName.equals("*") || tableName.equals(that.tableName)) &&
                operation.equals(that.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation);
    }
}