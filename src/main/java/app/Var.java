package app;

import java.util.*;
import soot.SootFieldRef;
import soot.Type;
import soot.Value;

/**
 * @author xp
 * The Var class describes variable and its fields.
 * Field is saved in a map as a varbox(Var with its source) 
 * by its ref.
 */
public class Var implements Cloneable {

    private final Value value;

    private Type type;

    private String typeName;

    private Map<SootFieldRef, VarBox> fields = new HashMap<>();

    public Var(Value value) {
        this.value = value;
        this.type = value.getType();
        this.typeName = type.toString();
    }
    
    public VarBox getField(SootFieldRef fieldRef) {
        return fields.get(fieldRef);
    }

    public void setField(SootFieldRef fieldRef, VarBox box) {
        VarBox existed = fields.get(fieldRef);
        if (existed == null) {
            existed = new VarBox(box);
            fields.put(fieldRef, existed);
        }
        existed.assign(box);
    }

    @Override
    public String toString() {
        return "Var{"
                + typeName +" "
                + fields.toString()
                + '}';
    }

    public void setFields(Map <SootFieldRef, VarBox> fields) throws CloneNotSupportedException {
        for (Map.Entry<SootFieldRef, VarBox>entry : fields.entrySet()) {
            this.fields.put(entry.getKey(), (VarBox)entry.getValue().clone());
        }
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException { 
        Var var = new Var((Value)this.value);
        var.setFields(this.fields);
        return var;
    }
    
}
