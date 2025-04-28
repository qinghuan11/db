package qinghuan.db.model;

import qinghuan.db.index.IndexManager;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Getter
@Setter
public class Table {
    private Schema schema;
    private List<Row> rows;
    private IndexManager indexManager;
    private ReentrantLock lock;

    public Table(Schema schema) {
        this.schema = schema;
        this.rows = new ArrayList<>();
        this.indexManager = new IndexManager();
        this.lock = new ReentrantLock();
    }

    public void insert(Row row) {
        lock.lock();
        try {
            if (!schema.validateRow(row)) {
                throw new IllegalArgumentException("Invalid row for schema");
            }
            String pk = schema.getPrimaryKey();
            if (pk != null && indexManager.contains(pk, row.getData().get(pk))) {
                throw new IllegalArgumentException("Duplicate primary key");
            }
            rows.add(row);
            if (pk != null) {
                indexManager.addIndex(pk, row.getData().get(pk), rows.size() - 1);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Row> select(Map<String, Object> conditions) {
        lock.lock();
        try {
            String pk = schema.getPrimaryKey();
            if (pk != null && conditions.containsKey(pk)) {
                Object pkValue = conditions.get(pk);
                List<Integer> rowIndices = indexManager.search(pk, pkValue);
                List<Row> result = new ArrayList<>();
                for (Integer index : rowIndices) {
                    if (index < rows.size() && matchesConditions(rows.get(index), conditions)) {
                        result.add(rows.get(index));
                    }
                }
                return result;
            }
            return rows.stream()
                    .filter(row -> matchesConditions(row, conditions))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public void update(Map<String, Object> conditions, Row newRow) {
        lock.lock();
        try {
            if (!schema.validateRow(newRow)) {
                throw new IllegalArgumentException("Invalid row for update");
            }
            for (int i = 0; i < rows.size(); i++) {
                if (matchesConditions(rows.get(i), conditions)) {
                    rows.set(i, newRow);
                    updateIndexes(i, newRow);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void delete(Map<String, Object> conditions) {
        lock.lock();
        try {
            rows.removeIf(row -> matchesConditions(row, conditions));
            indexManager.rebuildIndexes(rows, schema.getPrimaryKey());
        } finally {
            lock.unlock();
        }
    }

    private boolean matchesConditions(Row row, Map<String, Object> conditions) {
        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            if (!row.getData().getOrDefault(condition.getKey(), null).equals(condition.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void updateIndexes(int rowIndex, Row newRow) {
        String pk = schema.getPrimaryKey();
        if (pk != null) {
            indexManager.updateIndex(pk, newRow.getData().get(pk), rowIndex);
        }
    }
}