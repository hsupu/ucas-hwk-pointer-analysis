package app;

import java.util.*;

import soot.SootFieldRef;
import soot.Type;
import soot.Value;

/**
 * @author xp
 */
public class Var {

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
                + typeName
                + '}';
    }
}
