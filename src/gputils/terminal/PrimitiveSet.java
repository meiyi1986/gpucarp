package gputils.terminal;

import ec.gp.GPNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class for the primitive (terminal and/or function) set.
 * It includes a list of primitives to facilitate random sampling,
 * and a map of primitive for parsing from a string in a constant time.
 *
 * Created by Yi Mei on 30/08/17.
 */

public class PrimitiveSet {

    private List<GPNode> list;
    private Map<String, GPNode> map;

    public PrimitiveSet(List<GPNode> list, Map<String, GPNode> map) {
        this.list = list;
        this.map = map;
    }

    public PrimitiveSet(List<GPNode> list) {
        this.list = list;
        map = new HashMap<>();
        for (GPNode node : list) {
            map.put(node.toString(), node);
        }
    }

    /**
     * Default constructor constructs an empty primitive set.
     */
    public PrimitiveSet() {
        this(new LinkedList<>(), new HashMap<>());
    }

    public List<GPNode> getList() {
        return list;
    }

    public Map<String, GPNode> getMap() {
        return map;
    }

    /**
     * Get a primitive from an index in the list.
     * @param index the index of the primitive.
     * @return the desired primitive.
     */
    public GPNode get(int index) {
        return list.get(index);
    }

    /**
     * Get a primitive from a string key.
     * @param key the key of the primitive.
     * @return the desired primitive.
     */
    public GPNode get(String key) {
        return map.get(key);
    }

    /**
     * Get the size of the primitive set.
     * @return the size of the primitive set.
     */
    public int size() {
        return list.size();
    }

    /**
     * Add a primitive into the primitive set.
     * @param primitive the added primitive.
     */
    public void add(GPNode primitive) {
        list.add(primitive);
        map.put(primitive.toString(), primitive);
    }

    /**
     * Remove a primitive from the primitive set.
     * @param primitive the removed primitive.
     */
    public void remove(GPNode primitive) {
        list.remove(primitive);
        map.remove(primitive.toString());
    }
}
