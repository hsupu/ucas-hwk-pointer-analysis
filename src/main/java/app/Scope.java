package app;

import java.util.*;

import soot.Local;
import soot.Value;

/**
 * @author xp
 * This class saves Local and its Varbox as a map.
 */
public class Scope implements Cloneable{

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
    
    public Scope(GlobalScope globalScope, VarBox thisVarBox, List<Value> args, Map<Local, VarBox> varMap) {
        this( globalScope, thisVarBox, args);
        this.varMap = varMap;
    }
    
    public GlobalScope getGlobalScope() {
        return this.globalScope;
    }

    private void bindLocalAndVarBox(Local local, VarBox varBox) {//real bind Local and its VarBox
        varMap.put(local, varBox);
        globalScope.add(local, varBox);
    }

    public void bind(Local local, int paramIndex) {//@parameter
        if (paramIndex >= 0 && paramIndex < argCount) {
            Value arg = args.get(paramIndex);
            if (arg instanceof Local) {
                VarBox argBox = globalScope.get((Local) arg);
                VarBox box;
                if (argBox != null) {
                    box = new VarBox(argBox);
                } else {
                    Var var = new Var(arg);
                    box = new VarBox(var);
                }
                bindLocalAndVarBox(local, box);
            }
        }
    }

    public void bindThis(Local local) {//@this
        bindLocalAndVarBox(local, thisVarBox);
    }

    public VarBox createVarBox(Value value) {//save Exprnew or Constant as varbox
        VarBox box = VarBox.of(value);
        box.addSource(globalScope.getAllocId());
        return box;
    }

    public VarBox getOrAdd(Local local) {//Local
        
        VarBox box = varMap.get(local);
        if (box == null) {
            box = VarBox.of(local);
            bindLocalAndVarBox(local, box);
        }
        return box;
    }
    
    // deep clone
    @Override
    protected Scope clone()throws CloneNotSupportedException {
        List<Value> cloneOfArgs = new ArrayList<Value>(); 
        Iterator<Value> iterator = this.args.iterator(); 
        Map<Local, VarBox> cloneOfVarMap = new HashMap<>();
        while(iterator.hasNext()) { 
            cloneOfArgs.add((Value)iterator.next().clone()); 
        }
        for (Map.Entry<Local, VarBox> entry : this.varMap.entrySet()) {
            cloneOfVarMap.put(entry.getKey(),(VarBox)entry.getValue().clone());
        }
        VarBox vb;
        if (this.thisVarBox!=null) {
            vb = (VarBox)this.thisVarBox.clone();
        }else {
            vb = this.thisVarBox;
        }
        Scope scope =  new Scope((GlobalScope) this.globalScope.clone(), 
                                vb,
                                cloneOfArgs, 
                                cloneOfVarMap);
        return scope;
    }
    
    @Override
    public String toString() {
        String string = new String();
        List<String> mapStr = new ArrayList<>();
        for (Map.Entry<Local, VarBox> entry : this.varMap.entrySet()) {
            mapStr.add(entry.getKey().toString() + " = " + entry.getValue().toString()+"\n");
        }
        if (mapStr.size()>0) {
            Collections.sort(mapStr.subList(1, mapStr.size()));
        }
        string ="scope{"
                +"globalScope:"+ this.globalScope.toString()+",\n"
                +"varMap:"+mapStr+"\n"
                +"}";
        return string;
    }

    public Scope join(Scope other) {
        GlobalScope globalScope = new GlobalScope(this.globalScope.getAllocId(), 
                                                  this.globalScope.getQueries(), 
                                                  joinMap(this.globalScope.getVbMap(), other.globalScope.getVbMap()));
        Scope scope =  new Scope(globalScope, 
                                 this.thisVarBox, 
                                 this.args, 
                                 joinMap(this.varMap, other.varMap));
        return scope;
    }

    private Map<Local, VarBox> joinMap(Map<Local, VarBox> vm1, Map<Local, VarBox> vm2) {
        Map<Local, VarBox> vm = new HashMap<>();
         for (Map.Entry<Local, VarBox> entry : vm1.entrySet()) {
            vm.put(entry.getKey(), entry.getValue());
        }
         for (Map.Entry<Local, VarBox> entry : vm2.entrySet()) {
            vm.put(entry.getKey(), entry.getValue());
        }
        return  vm;
    }
    
    public void reset(Scope scope) throws CloneNotSupportedException {
        this.globalScope.reset(scope.getGlobalScope());
        if (scope.thisVarBox!=null) {
            this.thisVarBox.reset((VarBox)scope.thisVarBox);
        }
        this.args.clear();
        for (Value value : scope.args) {
            this.args.add(value);
        }
        this.varMap.clear();
         for (Map.Entry<Local, VarBox> entry : scope.varMap.entrySet()) {
            this.varMap.put(entry.getKey(), (VarBox)entry.getValue().clone());
        }      
    }
}
