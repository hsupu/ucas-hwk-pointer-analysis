package app;

import java.util.*;

import soot.Local;

/**
 * @author xp
 * @author wss
 * 记录了指针分析所需的值与变量关系，然后分析静态引用关系
 */
public class Analyzer {

    private int allocId = 0;

    private TreeMap<Integer, Local> queries = new TreeMap<>();

    private Map<Local, Set<Var>> sourceMap = new HashMap<>();

    public Analyzer() {
    }

    public void setAllocId(int allocId) {
        this.allocId = allocId;
    }

    public int getAllocId() {
        return allocId;
    }

    public void addQuery(int id, Local local) {
        queries.put(id, local);
    }

    public void register(Local local, Var var) {
        Set<Var> vars = sourceMap.computeIfAbsent(local, k -> new HashSet<>());
        vars.add(var);
    }

    public List<String> analyze() {
        List<String> results = new LinkedList<>();
        for (Map.Entry<Integer, Local> entry : queries.entrySet()) {
            Set<Var> vars = sourceMap.get(entry.getValue());
            if (vars != null) {
                Printer.log(0, "output " + entry.getKey() + " " + vars.toString());
                Set<Integer> sources = new HashSet<>();
                for (Var var : vars) {
                    sources.addAll(var.getSource());
                }
                if (!sources.isEmpty()) {
                    StringBuilder idsb = new StringBuilder();
                    for (int id : sources) {
                        if (id > 0) {
                            idsb.append(' ').append(id);
                        }
                    }
                    results.add(entry.getKey() + ":" + idsb.toString());
                }
            }
        }
        return results;
    }

    @Override
    public String toString() {
        return "Analyzer{"
                + "allocId:" + this.allocId + " "
                + "queries:" + this.queries + " "
                + "sourceMap:" + this.sourceMap + "\n"
                + "}";
    }
}
