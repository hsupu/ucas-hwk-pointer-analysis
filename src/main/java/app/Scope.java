package app;

import java.util.*;

import soot.Local;
import soot.Value;

/**
 * @author xp
 * @author wss
 * 模拟了本地变量作用域
 */
public class Scope {

    private final Analyzer analyzer;

    private final Scope invokerScope;

    private final Var thisVar;

    private final List<Value> args;

    private final int argCount;

    private final Scope parentScope;

    private Map<Local, Var> varMap = new HashMap<>();

    private Scope(Analyzer analyzer, Scope invokerScope, Var thisVar, List<Value> args, Scope parentScope) {
        this.analyzer = analyzer;
        this.invokerScope = invokerScope;
        this.thisVar = thisVar;
        this.args = args;
        this.argCount = args.size();
        this.parentScope = parentScope;
    }

    public Scope(Analyzer analyzer) {
        this(analyzer, null, null, Collections.emptyList(), null);
    }

    public Scope clone() {
        Scope scope = new Scope(analyzer, invokerScope, thisVar, args, parentScope);
        for (Map.Entry<Local, Var> entry : varMap.entrySet()) {
            scope.varMap.put(entry.getKey(), entry.getValue().clone());
        }
        return scope;
    }

    public Scope createSubScope() {
        return new Scope(this.analyzer, this.invokerScope, this.thisVar, this.args, this);
    }

    public Scope createInvokeScope(Var thisVar, List<Value> args) {
        return new Scope(this.analyzer, this, thisVar, args, null);
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    private void bindLocalAndVarBox(Local local, Var var) {
        varMap.put(local, var);
        analyzer.register(local, var);
    }

    public void bindArg(Local local, int paramIndex) {
        if (paramIndex >= 0 && paramIndex < argCount) {
            Value argValue = args.get(paramIndex);
            if (argValue instanceof Local) {
                Var argVar = invokerScope.get((Local) argValue);
                Var var;
                if (argVar != null) {
                    var = argVar.clone();
                } else {
                    var = Var.of(argValue);
                }
                bindLocalAndVarBox(local, var);
            }
        }
    }

    public void bindThis(Local local) {
        bindLocalAndVarBox(local, thisVar.clone());
    }

    public Var createVarBox(Value value) {
        Var var = Var.of(value);
        var.addSource(analyzer.getAllocId());
        return var;
    }

    private Var get(Local local) {
        Var var = null;
        if (parentScope != null) {
            var = parentScope.get(local);
            if (var != null) {
                varMap.put(local, var.clone());
            }
        }
        if (var == null) {
            var = varMap.get(local);
        }
        return var;
    }

    public Var getOrAdd(Local local) {
        Var var = get(local);
        if (var == null) {
            var = Var.of(local);
            bindLocalAndVarBox(local, var);
        }
        return var;
    }

    public void join(Scope another) {
        for (Map.Entry<Local, Var> entry : another.varMap.entrySet()) {
            Var existed = this.varMap.get(entry.getKey());
            if (existed != null) {
                existed.assign(entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        return "Scope{\n"
                + this.thisVar + "\n"
                + this.varMap + "\n"
                + "}";
    }

    public int depth() {
        int depth = 0;
        Scope scope;
        scope = invokerScope;
        while (scope != null) {
            depth++;
            scope = scope.invokerScope;
        }
        scope = parentScope;
        while (parentScope != null) {
            depth++;
            scope = scope.parentScope;
        }
        return depth;
    }
}
