package app;

import java.util.*;

import soot.Value;

/**
 * @author xp
 */
public class VarBox {

    private String name;

    private Var var;

    private final Set<Integer> source = new HashSet<>();

    public VarBox(Var var) {
        this.var = var;
    }

    public VarBox(VarBox varBox) {
        this.var = varBox.var;
        this.source.addAll(varBox.source);
    }

    public void addSource(Integer allocId) {
        if (allocId != null) {
            source.add(allocId);
        }
    }

    public void assign(VarBox varBox) {
        source.addAll(varBox.source);
        var = varBox.var;
    }

    public Var getVar() {
        return var;
    }

    public Set<Integer> getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "VarBox{"
                + name + " "
                + var
                + " " + source
                + '}';
    }

    public static VarBox of(Value value) {
        Var var = new Var(value);
        return new VarBox(var);
    }
}
