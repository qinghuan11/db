package qinghuan.db.index;

import qinghuan.db.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexManager {
    private Map<String, BPlusTree<Integer>> indexes;

    public IndexManager() {
        this.indexes = new HashMap<>();
    }

    public void addIndex(String column, Object value, int rowIndex) {
        // 确保值是Integer类型
        if (!(value instanceof Integer)) {
            throw new IllegalArgumentException("Index key must be an Integer for column: " + column);
        }
        BPlusTree<Integer> tree = indexes.computeIfAbsent(column, k -> new BPlusTree<>(3));
        tree.insert((Integer) value, rowIndex);
    }

    public void updateIndex(String column, Object value, int rowIndex) {
        addIndex(column, value, rowIndex);
    }

    public boolean contains(String column, Object value) {
        if (!(value instanceof Integer)) {
            return false;
        }
        BPlusTree<Integer> tree = indexes.get(column);
        if (tree == null) {
            return false;
        }
        List<Integer> result = tree.search((Integer) value);
        return !result.isEmpty();
    }

    public void rebuildIndexes(List<Row> rows, String primaryKey) {
        if (primaryKey != null) {
            BPlusTree<Integer> tree = new BPlusTree<>(3);
            for (int i = 0; i < rows.size(); i++) {
                Object key = rows.get(i).getData().get(primaryKey);
                if (!(key instanceof Integer)) {
                    throw new IllegalArgumentException("Primary key must be an Integer for column: " + primaryKey);
                }
                tree.insert((Integer) key, i);
            }
            indexes.put(primaryKey, tree);
        }
    }

    public List<Integer> search(String column, Object value) {
        if (!(value instanceof Integer)) {
            return new ArrayList<>();
        }
        BPlusTree<Integer> tree = indexes.get(column);
        if (tree == null) {
            return new ArrayList<>();
        }
        return tree.search((Integer) value);
    }
}