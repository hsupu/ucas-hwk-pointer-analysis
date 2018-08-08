package app;

import java.util.*;

import soot.Local;
import soot.Value;

/**
 * @author xp
 * @author wss
 * 模拟了变量（引用），引用可以被重新赋值，使之指向不同的值
 * 记录了可能的引用标号
 */
public class Var {

    private String name;

    private Local origin;

    private Val val;

    private final Set<Integer> source = new HashSet<>();

    private Var(Value originValue, Val val) {
        this.name = originValue.toString();
        if (originValue instanceof Local) {
            this.origin = (Local) originValue;
        }
        this.val = val;
    }

    public Var duplicate(boolean deep) {
        Var var = new Var(origin, deep ? this.val.duplicateDeeply() : this.val);
        var.source.addAll(this.source);
        return var;
    }

    public static Var of(Value value) {
        Val val = new Val(value);
        return new Var(value, val);
    }

    public void addSource(Integer allocId) {
        if (allocId != null) {
            source.add(allocId);
        }
    }

    public void assign(Var var) {
        source.clear();
        source.addAll(var.source);
        val = var.val;
    }

    public Local getOrigin() {
        return origin;
    }

    public Val getVal() {
        return val;
    }

    public Set<Integer> getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Var{"
                + name + " "
                + System.identityHashCode(val) + " "
                + source
                + '}';
    }
}
