package app;

import java.util.*;

import soot.Local;

/**
 * @author xp
 */
public class GlobalScope {

    private Integer allocId = null;

    private TreeMap<Integer, Local> queries = new TreeMap<>();

    private Map<Local, VarBox> vbMap = new HashMap<>();

    public void setAllocId(Integer allocId) {
        this.allocId = allocId;
    }

    public Integer getAllocId() {
        return allocId;
    }

    public void addQuery(int id, Local local) {
        queries.put(id, local);
    }

    public void add(Local local, VarBox vb) {
        vbMap.put(local, vb);
    }

    public VarBox get(Local local) {
        return vbMap.get(local);
    }

    public List<String> analyze() {
        List<String> results = new LinkedList<>();
        for (Map.Entry<Integer, Local> entry : queries.entrySet()) {
            StringBuilder idsb = new StringBuilder();
            VarBox vb = vbMap.get(entry.getValue());
            if (vb != null) {
                for (Integer id : vb.getSource()) {
                    idsb.append(' ').append(id);
                }
                results.add(entry.getKey() + ":" + idsb.toString());
            }
        }
        return results;
    }
}
