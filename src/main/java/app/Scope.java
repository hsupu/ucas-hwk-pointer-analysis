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

    private final String methodSignature;

    private final Var thisVar;

    private final List<Value> args;

    private final int argCount;

    private final int depth;

    private Map<Local, Var> varMap = new HashMap<>();

    private Scope(Analyzer analyzer, Scope invokerScope, String methodSignature, Var thisVar, List<Value> args, int depth) {
        this.analyzer = analyzer;
        this.invokerScope = invokerScope;
        this.methodSignature = methodSignature;
        this.thisVar = thisVar;
        this.args = args;
        this.argCount = args.size();
        this.depth = depth;
    }

    public Scope(Analyzer analyzer) {
        this(analyzer, null, null, null, Collections.emptyList(), 0);
    }

    public Scope createSameScope() {
        Scope scope = new Scope(analyzer, invokerScope, methodSignature, thisVar, args, depth);
        for (Map.Entry<Local, Var> entry : this.varMap.entrySet()) {
            scope.varMap.put(entry.getKey(), entry.getValue().duplicate(true));
        }
        return scope;
    }

    public Scope createInvokeScope(String methodSignature, Var thisVar, List<Value> args) {
        return new Scope(this.analyzer, this, methodSignature, thisVar, args, depth + 1);
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
                    var = argVar.duplicate(false);
                } else {
                    var = Var.of(argValue);
                }
                bindLocalAndVarBox(local, var);
            }
        }
    }

    public void bindThis(Local local) {
        bindLocalAndVarBox(local, thisVar.duplicate(false));
    }

    public Var createVarBox(Value value) {
        Var var = Var.of(value);
        // 根据测评要求，allocId 只对紧接着的一条 AssignStmt NewExpr 有效
        // var.addSource(analyzer.getAllocId());
        return var;
    }

    private Var get(Local local) {
        return varMap.get(local);
    }

//    private Var get(Local local, boolean writeThrough) {
//        Var var = null;
//        if (parentScope != null) {
//            var = parentScope.get(local, false);
//            if (var != null) {
//                var = var.duplicate(false);
//                if (writeThrough) {
//                    varMap.put(local, var);
//                }
//            }
//        }
//        if (var == null) {
//            var = varMap.get(local);
//        }
//        return var;
//    }

    public Var getOrAdd(Local local) {
        Var var = get(local);
        if (var == null) {
            var = Var.of(local);
            bindLocalAndVarBox(local, var);
        }
        return var;
    }

//    public void join(Scope another) {
//        for (Map.Entry<Local, Var> entry : another.varMap.entrySet()) {
//            Var existed = this.varMap.get(entry.getKey());
//            if (existed != null) {
//                existed.assign(entry.getValue());
//            }
//        }
//    }

    public boolean isInInvokeChain(String methodSignature) {
        if (methodSignature.equals(this.methodSignature)) {
            return true;
        }
        if (invokerScope != null) {
            return invokerScope.isInInvokeChain(methodSignature);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Scope{"
                + this.thisVar + " "
                + this.varMap
                + "}";
    }

    public int depth() {
        return depth;
    }
}
