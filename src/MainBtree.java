import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class MainBtree {
    /*
     * Viktor Kovalev
     * vi.kovalev@innopolis.university
     *
     * Time Complexity of whole algorithm - O(n * log(n))
     */
    public static void main(String[] args) {

        // Creates BTree with degree 3. Read n.
        BTree<String, Integer> bTree = new BTree<>(3);
        Scanner in = new Scanner(System.in);
        int N = in.nextInt();

        // Loop for commands.
        for (int i = 0; i < N; i++) {

            // Read current command.
            String date = in.next(), command = in.next();
            if (command.equals("DEPOSIT") || command.equals("WITHDRAW")) {

                // Read amount of transaction.
                int amount = in.nextInt();
                // Check for containing the same date in the BTree.
                if (bTree.contains(date)) {

                    // Update value for current date.
                    if (command.equals("WITHDRAW")) {
                        bTree.update(date, bTree.lookup(date) - amount);
                    } else {
                        bTree.update(date, bTree.lookup(date) + amount);
                    }
                } else {

                    // Add new date with value in the BTree.
                    if (command.equals("WITHDRAW")) {
                        bTree.add(date, -amount);
                    } else {
                        bTree.add(date, amount);
                    }
                }
            } else {

                // Read dates for range.
                String from = in.next(), to = in.next();
                to = in.next();

                // Call to method for values from range.
                LinkedList<Integer> trans = (LinkedList<Integer>) bTree.lookupRange(from, to);
                int range = 0;
                // Sum all values from range.
                for (Integer x : trans) {
                    range += x;
                }
                // Print sum.
                System.out.println(range);
            }
        }
    }

    /**
     * Interface for BTree.
     *
     * @param <K> - Key.
     * @param <V> - Value.
     */
    public interface RangeMap<K, V> {
        /**
         * @return Size.
         */
        int size();

        /**
         * @return true - empty; false - not empty.
         */
        boolean isEmpty();

        /**
         * Insert new item into the map.
         *
         * @param key   Key.
         * @param value Value.
         */
        void add(K key, V value);

        /**
         * Check if a key is present.
         *
         * @param key Key
         * @return true - present; false - not present.
         */
        boolean contains(K key);

        /**
         * Lookup a value by the key.
         *
         * @param key Key
         * @return Value of Key.
         */
        V lookup(K key);

        /**
         * Lookup values for a range of keys.
         *
         * @param from First key from range.
         * @param to   Last key from range.
         * @return List of all values from this range.
         */
        List<V> lookupRange(K from, K to);

        /**
         * Remove an item from a map.
         *
         * @param key Key.
         * @return Deleted node.
         */
        Object remove(K key);
    }

    /**
     * B-Tree with implemented RangeMap.
     *
     * @param <K> Comparable type of Keys.
     * @param <V> Value.
     */
    public static class BTree<K extends Comparable<? super K>, V> implements RangeMap<K, V> {
        // T - degree of B-Tree.
        private final int T;
        private int size = 0;
        // Numbers of maximum possible children and keys for each node.
        private final int maxChildren, maxKeys;

        /**
         * Class which contains Node and index to the key from this node.
         * <br/>Uses for some private methods of B-Tree.
         */
        private class NodeWithKey {
            public Node node;
            public int index;

            public NodeWithKey(Node node, int index) {
                this.node = node;
                this.index = index;
            }
        }

        /**
         * Node of B-Tree.
         */
        private class Node {
            // Number of keys.
            public int n = 0;
            // Mark for leaf.
            public boolean leaf = true;
            // Link to parent.
            public Node parent = null;
            // Lists of keys, values, and children.
            public ArrayList<K> keys;
            public ArrayList<V> values;
            public ArrayList<Node> children;
            // Index of node in the parent of this node.
            public int index = 0;

            public Node() {
                keys = new ArrayList<>(maxKeys);
                values = new ArrayList<>(maxKeys);
                children = new ArrayList<>(maxChildren);

                // Fill all lists for full by nulls.
                for (int i = 0; i < maxKeys; i++) {
                    keys.add(null);
                    values.add(null);
                    children.add(null);
                }
                children.add(null);
            }
        }

        // Root node.
        private Node root;

        public BTree(int t) {
            T = t;
            // Calculations of maximums keys and children.
            maxChildren = 2 * t;
            maxKeys = 2 * t - 1;
            root = new Node();
        }

        /**
         * Size.
         *
         * @return Number of keys in the B-Tree.
         */
        @Override
        public int size() {
            return size;
        }

        /**
         * @return true - empty; false - not empty.
         */
        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Insert new key with value into the B-Tree.
         * <br/>Time complexity - O(T * (log(n) with base T))
         *
         * @param key   Key.
         * @param value Value.
         */
        @Override
        public void add(K key, V value) {
            // Check for fullness of root.
            if (root.n == maxKeys) {
                // Create new root.
                Node r = root;
                Node newNode = new Node();
                root = newNode;
                root.leaf = false;
                root.n = 0;
                // Add previous root to the children of new root.
                root.children.set(0, r);
                r.parent = newNode;
                r.index = 0;
                // Separate current root into two nodes.
                splitChild(newNode, 0);
                // Insert in the new root
                insertNonFull(newNode, key, value);
            } else {
                // Just insert key in the root node.
                insertNonFull(root, key, value);
            }
            size++;
        }

        /**
         * Insert new key into non-full node.
         * <br/>Time complexity - O()
         * @param node Node for insertion.
         * @param key Value of key.
         * @param value Value.
         */
        private void insertNonFull(Node node, K key, V value) {
            int i = node.n - 1;
            if (node.leaf) {
                // Find right place for new key and move other elements.
                while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) {
                    node.keys.set(i + 1, node.keys.get(i));
                    node.values.set(i + 1, node.values.get(i));
                    i--;
                }
                // Set new key.
                node.keys.set(i + 1, key);
                node.values.set(i + 1, value);
                node.n++;
            } else {
                // Find child for insertion new key into.
                while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) {
                    i--;
                }
                i++;
                // Split this child if need and insert key into this child.
                if (i <= node.n && node.children.get(i).n == maxKeys) {
                    splitChild(node, i);
                    if (key.compareTo(node.keys.get(i)) > 0) {
                        i++;
                    }
                }
                insertNonFull(node.children.get(i), key, value);
            }
        }

        /**
         * Split one full node into two nodes.
         * @param node Node for splitting.
         * @param i Index.
         */
        private void splitChild(Node node, int i) {
            // Create new node for splitting keys and children.
            Node newChild = new Node();
            Node child = node.children.get(i);
            newChild.leaf = child.leaf;
            newChild.n = T - 1;
            newChild.parent = node;
            // Transfer keys and values
            for (int j = 0; j < T - 1; j++) {
                newChild.keys.set(j, child.keys.get(j + T));
                newChild.values.set(j, child.values.get(j + T));
            }
            // Transfer children if node has.
            if (!child.leaf) {
                for (int j = T - 1; j >= 0; j--) {
                    newChild.children.set(j, child.children.get(j + T));
                    newChild.children.get(j).index = j;
                    newChild.children.get(j).parent = newChild;
                }
            }
            // Find place in the parent node for new node.
            child.n = T - 1;
            for (int j = node.n; j >= i + 1; j--) {
                node.children.set(j + 1, node.children.get(j));
                node.children.get(j + 1).index = j + 1;
            }
            // Insert new node.
            node.children.set(i + 1, newChild);
            newChild.index = i + 1;
            // Move keys for new key.
            for (int j = node.n - 1; j >= i; j--) {
                node.keys.set(j + 1, node.keys.get(j));
                node.values.set(j + 1, node.values.get(j));
            }
            // Insert middle key into parent node.
            node.keys.set(i, child.keys.get(T - 1));
            node.values.set(i, child.values.get(T - 1));
            node.n++;
        }

        /**
         * Check for key.
         * @param key Key
         * @return true - presented; false - not presented.
         */
        @Override
        public boolean contains(K key) {
            // Search.
            NodeWithKey findings = search(root, key);
            return findings != null;
        }

        /**
         * Return values of presented key.
         * <br/>Time complexity - O(log(n) with base T)
         *
         * @param key Key.
         * @return Values of key.
         */
        @Override
        public V lookup(K key) {
            // Search for node.
            NodeWithKey findings = search(root, key);
            if (findings == null) {
                return null;
            }
            // Return value.
            return findings.node.values.get(findings.index);
        }

        /**
         * Search for node with needed key.
         * @param node Current node.
         * @param key Needed key.
         * @return Class with node and index of key in this node.
         */
        private NodeWithKey search(Node node, K key) {
            // If node is null then B-Tree has not needed key.
            if (node == null) {
                return null;
            }
            // Skip all keys which is less than needed.
            int i = 0;
            while (i < node.n && node.keys.get(i) != null && key.compareTo(node.keys.get(i)) > 0) {
                i++;
            }
            if (i < node.n && node.keys.get(i) == null) i = Math.max(0, i - 1);

            // If it is needed key then return it.
            if (i < node.n && node.keys.get(i) != null && key.compareTo(node.keys.get(i)) == 0) {
                return new NodeWithKey(node, i);
            } else if (node.leaf) {
                // If current node has not key, and it is leaf, then return null.
                return null;
            } else {
                // Go to the child.
                return search(node.children.get(i), key);
            }
        }

        /**
         * Look for values from range.
         * @param from First key from range.
         * @param to   Last key from range.
         * @return List of values.
         */
        @Override
        public List<V> lookupRange(K from, K to) {
            // Find first node from range.
            NodeWithKey currentLeft = lookForFirst(from, root);

            // List for values which will be returned.
            LinkedList<V> collection = new LinkedList<>();
            if (currentLeft != null) {

                // Go to all values from range.
                lookForRange(from, to, currentLeft.node, currentLeft.index, collection, false);
            }

            // Return these values.
            return collection;
        }

        /**
         * Search for the left node with from key which is first in range.
         * <br/>
         * Time complexity of the worst case : O(log(tree.size) with base T)
         *
         * @param from Key which is first in the range of search.
         * @param node From which Node we should go.
         * @return Class with node and index of needed key from this node.
         */
        private NodeWithKey lookForFirst(K from, Node node) {
            // Skip all keys which smaller than needed key.
            int i = 0;
            while (i < node.n && node.keys.get(i).compareTo(from) < 0) {
                i++;
            }
            // If find key then return it.
            if (i < node.n && node.keys.get(i).compareTo(from) == 0) {
                return new NodeWithKey(node, i);
            }
            // If node has children then go to the child with keys greater than last checked key in current node.
            if (!node.leaf) {
                return lookForFirst(from, node.children.get(i));
            }

            // If it is leaf, then return first greater key.
            if (i < node.n && node.keys.get(i).compareTo(from) > 0) {
                return new NodeWithKey(node, i);
            }

            // Go to up for first greater key.
            while (node.parent != null) {
                i = node.index;
                node = node.parent;
                if (i < node.n) {
                    return new NodeWithKey(node, i);
                }
            }

            // Return null if can not find key greater or equal to from key.
            return null;
        }

        /**
         * Go from first needed key to the last key from range.
         * <br/>Time complexity of the worst case : O(log(tree.size) with base T)
         *
         * @param from First key from range.
         * @param to Last key from range.
         * @param node Current node.
         * @param index Index of current key.
         * @param values List for all needed values.
         * @param fromParent Mark for children.
         */
        private void lookForRange(K from, K to, Node node, int index, LinkedList<V> values, boolean fromParent) {

            // Go to the left child if we should.
            if (fromParent && node.children.get(index) != null) {
                lookForRange(from, to, node.children.get(index), 0, values, true);
            }
            // Go through keys and add values if it needed.
            while (index < node.n && node.keys.get(index).compareTo(to) <= 0) {
                if (node.keys.get(index).compareTo(from) >= 0) {
                    values.add(node.values.get(index));
                }

                // Go to the right child if current node is not leaf.
                if (node.children.get(index + 1) != null) {
                    lookForRange(from, to, node.children.get(index + 1), 0, values, true);
                }
                index++;
            }
            // Go to the parent if we are not from parent.
            if (!fromParent && node.parent != null) {
                lookForRange(from, to, node.parent, node.index, values, false);
            }
        }

        /**
         * Update the presented key for new value.
         * @param key Key.
         * @param value Value.
         */
        public void update(K key, V value) {
            // Search for node with needed index of key.
            NodeWithKey data = search(root, key);
            if (data == null) {
                return;
            }
            // Update the value.
            data.node.values.set(data.index, value);
        }

        /**
         * Delete the key.
         * @param key Key.
         * @return Deleted value.
         */
        @Override
        public Object remove(K key) {
            return null;
        }
    }
}
