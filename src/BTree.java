//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//
//public class BTree<K extends Comparable<? super K>, V> implements RangeMap<K, V> {
//    private final int T;
//    private int size = 0;
//    private final int maxChildren, maxKeys;
//
//    private class NodeWithKey {
//        public Node node;
//        public int index;
//
//        public NodeWithKey(Node node, int index) {
//            this.node = node;
//            this.index = index;
//        }
//    }
//
//    private class Node {
//        public int n;
//        public boolean leaf;
//        public Node parent = null;
//        public ArrayList<K> keys;
//        public ArrayList<V> values;
//        public ArrayList<Node> children;
//
//        public Node() {
//            n = 0;
//            keys = new ArrayList<>();
//            values = new ArrayList<>();
//            children = new ArrayList<>();
//            leaf = true;
//        }
//    }
//
//    private Node root;
//
//    public BTree(int t) {
//        T = t;
//        maxChildren = 2 * t;
//        maxKeys = 2 * t - 1;
//        root = new Node();
//    }
//
//    @Override
//    public int size() {
//        return size;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return size == 0;
//    }
//
//    @Override
//    public void add(K key, V value) {
//        if (root.n == maxKeys) {
//            Node r = root;
//            Node newNode = new Node();
//            root = newNode;
//            r.parent = newNode;
//            root.leaf = false;
//            root.n = 0;
//            root.children.add(r);
//            splitChild(newNode, 0);
//            insertNonfull(newNode, key, value);
//        } else {
//            insertNonfull(root, key, value);
//        }
//        size++;
//    }
//
//    private void insertNonfull(Node root, K key, V value) {
//        int i = root.n - 1;
//        if (root.leaf) {
//            int j = 0;
//            for (K x : root.keys) {
//                if (key.compareTo(x) >= 0) {
//                    j++;
//                } else {
//                    break;
//                }
//            }
//            root.keys.add(j, key);
//            root.values.add(j, value);
//            root.n++;
//            /*
//            while (i >= 0 && key.compareTo(root.keys.get(i)) < 0){
//                root.keys.set(i + 1, root.keys.get(i));
//                root.values.set(i + 1, root.values.get(i));
//                i--;
//            }
//            root.keys.set(i + 1, key);
//            root.values.set(i + 1, value);
//            root.n++;
//            */
//        } else {
//            while (i >= 0 && key.compareTo(root.keys.get(i)) < 0) {
//                i--;
//            }
//            i++;
//            if (root.children.get(i).n == maxKeys) {
//                splitChild(root, i);
//                if (key.compareTo(root.keys.get(i)) > 0) {
//                    i++;
//                }
//            }
//            insertNonfull(root.children.get(i), key, value);
//        }
//    }
//
//    private void splitChild(Node node, int i) {
//        Node node1 = new Node();
//        Node child = node.children.get(i);
//        node1.leaf = child.leaf;
//        node1.n = T - 1;
//        node1.parent = node;
//        for (int j = 0; j < T - 1; j++) {
//            node1.keys.add(child.keys.get(j + T));
//            node1.values.add(child.values.get(j + T));
//        }
//        if (!child.leaf) {
//            for (int j = 0; j < T; j++) {
//                node1.children.add(child.children.get(j + T));
//            }
//        }
//        child.n = T - 1;
//        node.children.add(i + 1, node1);
//        node.keys.add(i, child.keys.get(T - 1));
//        node.values.add(i, child.values.get(T - 1));
//        node.n++;
//    }
//
//    @Override
//    public boolean contains(K key) {
//        NodeWithKey findings = search(root, key);
//        return findings != null;
//    }
//
//    @Override
//    public V lookup(K key) {
//        NodeWithKey findings = search(root, key);
//        if (findings == null) {
//            return null;
//        }
//        return findings.node.values.get(findings.index);
//    }
//
//    private NodeWithKey search(Node node, K key) {
//        int i = 0;
//        while (i < node.n && key.compareTo(node.keys.get(i)) > 0) {
//            i++;
//        }
//        if (i < node.n && key.compareTo(node.keys.get(i)) == 0) {
//            return new NodeWithKey(node, i);
//        } else if (node.leaf) {
//            return null;
//        } else {
//            return search(node.children.get(i), key);
//        }
//    }
//
//    @Override
//    public List<V> lookupRange(K from, K to) {
//        NodeWithKey currentLeft = lookForFirst(from, root);
//        LinkedList<V> collection = new LinkedList<>();
//        if (currentLeft != null){
//            lookDownRange(to, currentLeft.node, currentLeft.index, collection);
//        }
//        return collection;
//    }
//
//    private NodeWithKey lookForFirst(K from, Node node) {
//        int i = 0;
//        while (i < node.n && node.keys.get(i).compareTo(from) < 0) {
//            i++;
//        }
//        if (i < node.n && node.keys.get(i).compareTo(from) > 0) {
//            return lookForFirst(from, node.children.get(i));
//        } else if (i < node.n && node.keys.get(i).compareTo(from) == 0) {
//            return new NodeWithKey(node, i);
//        } else if (i == node.n && node.keys.get(i - 1).compareTo(from) < 0) {
//            return lookForFirst(from, node.children.get(i));
//        } else {
//            return null;
//        }
//    }
//
//    private void lookDownRange(K to, Node node, int index, LinkedList<V> values) {
//        int i = index;
//        while (i < node.n && node.keys.get(i).compareTo(to) <= 0) {
//            values.add(node.values.get(i));
//            if (node.children.size() > i)
//                lookDownRange(to, node.children.get(i + 1), 0, values);
//            i++;
//        }
//    }
//
//    public void update(K key, V value) {
//        NodeWithKey data = search(root, key);
//        if (data == null) {
//            return;
//        }
//        data.node.values.set(data.index, value);
//    }
//
//    @Override
//    public Object remove(K key) {
//        return null;
//    }
//}
