package qinghuan.db.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ColumnDefinition {
    private String name;
    private String type;
    private boolean notNull;
}