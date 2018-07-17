package app;

import java.util.*;

import soot.Local;
import soot.Value;

/**
 * @author xp
 */
public class Scope {

    private final GlobalScope globalScope;

    private final VarBox thisVarBox;

    private final List<Value> args;

    private final int argCount;

    private Map<Local, VarBox> varMap = new HashMap<>();

    public Scope(GlobalScope globalScope, VarBox thisVarBox, List<Value> args) {
        this.globalScope = globalScope;
        this.thisVarBox = thisVarBox;
        this.args = args;

        this.argCount = args.size();
    }

    public GlobalScope getGlobalScope() {
        return globalScope;
    }

    private void bindLocalAndVarBox(Local local, VarBox varBox) {
        varMap.put(local, varBox);
        globalScope.add(local, varBox);
    }

    public void bind(Local local, int paramIndex) {
        if (paramIndex >= 0 && paramIndex < argCount) {
            Value arg = args.get(paramIndex);
            if (arg instanceof Local) {
                VarBox argBox = globalScope.get((Local) arg);
                VarBox box;
                if (argBox != null) {
                    box = new VarBox(argBox);
                } else {
                    box = VarBox.of(arg);
                }
                bindLocalAndVarBox(local, box);
            }
        }
    }

    public void bindThis(Local local) {
        bindLocalAndVarBox(local, thisVarBox);
    }

    public VarBox createVarBox(Value value) {
        VarBox box = VarBox.of(value);
        box.addSource(globalScope.getAllocId());
        return box;
    }

    public VarBox getOrAdd(Local local) {
        VarBox box = varMap.get(local);
        if (box == null) {
            box = VarBox.of(local);
            bindLocalAndVarBox(local, box);
        }
        return box;
    }
}
