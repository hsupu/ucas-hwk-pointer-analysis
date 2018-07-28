package app;

import java.util.*;

import soot.Local;

/**
 * @author xp
 * This class records global Benchmark.alloc(allocId), Benchmark.test(queries) 
 * and local-source(vbMap) info, then analyze vbMap to get result of point-to.
 */
public class GlobalScope implements Cloneable {

    private Integer allocId = null;

    private TreeMap<Integer, Local> queries = new TreeMap<>();

    private Map<Local, VarBox> vbMap = new HashMap<>();
    
    public GlobalScope() {
        
    }
    
    public GlobalScope(Integer allocId, TreeMap<Integer, Local> queries, Map<Local, VarBox> vbMap) {
        this.allocId = allocId;
        this.queries = queries;
        this.vbMap = vbMap;
    }
    
    public void setAllocId(Integer allocId) {
        this.allocId = allocId;
    }

    public Integer getAllocId() {
        return allocId;
    }
    
    public TreeMap<Integer, Local> getQueries() {
        return this.queries;
    }
    
    public Map<Local, VarBox> getVbMap() {
        return this.vbMap;
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
        //System.out.println(vbMap.toString());
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
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        GlobalScope globalScope = new GlobalScope();
        globalScope.allocId = this.allocId;
         for (Map.Entry<Integer, Local>entry : this.queries.entrySet()) {
            globalScope.queries.put(entry.getKey(), (Local)entry.getValue().clone());
        }
         for (Map.Entry<Local, VarBox>entry : this.vbMap.entrySet()) {
            globalScope.vbMap.put(entry.getKey(), (VarBox)entry.getValue().clone());
        }
        return globalScope;
    }

    @Override
    public String toString() {
        String string = new String();
        List<String> mapStr = new ArrayList<>();
        for (Map.Entry<Local, VarBox> entry : this.vbMap.entrySet()) {
            mapStr.add(entry.getKey().toString() + " = " + entry.getValue().toString() + "\n");
        }
        if (mapStr.size() > 0) {
            Collections.sort(mapStr.subList(1, mapStr.size()));
        }
        string = "GlobalScope{\n"
                + "allocId:"+ this.allocId + " "
                + "queries:"+ this.queries.toString() + " "
                + "vbMap:"+mapStr+"\n"
                + "}";
        return string;
    }
    
    public void reset(GlobalScope other) throws CloneNotSupportedException {
        this.allocId = other.allocId;
        for (Map.Entry<Integer, Local>entry : other.queries.entrySet()) {
            this.queries.put(entry.getKey(), (Local)entry.getValue().clone());
        }
        for (Map.Entry<Local, VarBox>entry : other.vbMap.entrySet()) {
            this.vbMap.put(entry.getKey(), (VarBox)entry.getValue().clone());
        }
        this.vbMap = other.vbMap;
    }
    
}
