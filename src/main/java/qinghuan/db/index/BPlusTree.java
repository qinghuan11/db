package qinghuan.db.index;

import java.util.ArrayList;
import java.util.List;

public class BPlusTree<V> {
    // B+树节点，使用Integer作为键类型
    private class Node {
        List<Integer> keys;          // 存储键
        List<V> values;              // 存储值（叶子节点使用）
        List<Node> children;         // 存储子节点（非叶子节点使用）
        boolean isLeaf;              // 是否为叶子节点

        Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
            this.keys = new ArrayList<>();
            this.values = new ArrayList<>();
            this.children = new ArrayList<>();
        }
    }

    private Node root;
    private final int order; // B+树的阶数

    public BPlusTree(int order) {
        this.order = order;
        this.root = new Node(true);
    }

    public void insert(Integer key, V value) {
        insert(root, key, value);
    }

    private void insert(Node node, Integer key, V value) {
        if (node.isLeaf) {
            // 找到插入位置
            int pos = 0;
            while (pos < node.keys.size() && node.keys.get(pos).compareTo(key) < 0) {
                pos++;
            }
            node.keys.add(pos, key);
            node.values.add(pos, value);
        } else {
            // 非叶子节点，简化为直接插入到第一个子节点
            if (node.children.isEmpty()) {
                node.children.add(new Node(true));
            }
            insert(node.children.get(0), key, value);
        }
    }

    public List<V> search(Integer key) {
        return search(root, key);
    }

    private List<V> search(Node node, Integer key) {
        int pos = 0;
        while (pos < node.keys.size() && node.keys.get(pos).compareTo(key) <= 0) {
            pos++;
        }
        if (node.isLeaf) {
            List<V> result = new ArrayList<>();
            if (pos > 0 && pos <= node.keys.size() && node.keys.get(pos - 1).compareTo(key) == 0) {
                result.add(node.values.get(pos - 1));
            }
            return result;
        } else {
            if (pos == 0) pos = 1;
            return search(node.children.get(pos - 1), key);
        }
    }
}
