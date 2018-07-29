package app;

import java.util.*;

import soot.SootFieldRef;
import soot.Type;
import soot.Value;

/**
 * @author xp
 * @author wss
 * 模拟了变量（值），支持子字段的模拟
 */
public class Val {

    private final Value value;

    private Type type;

    private String typeName;

    private Map<SootFieldRef, Var> fields = new HashMap<>();

    public Val(Value value) {
        this.value = value;
        this.type = value.getType();
        this.typeName = type.toString();
    }

    public Value getValue() {
        return value;
    }

    public Var getField(SootFieldRef fieldRef) {
        return fields.get(fieldRef);
    }

    public void setField(SootFieldRef fieldRef, Var box) {
        Var existed = fields.get(fieldRef);
        if (existed == null) {
            existed = box.clone();
            fields.put(fieldRef, existed);
        } else {
            existed.assign(box);
        }
    }

    @Override
    public String toString() {
        return "Val{"
                + typeName + " "
                + "fields:" + fields.toString()
                + '}';
    }
}
