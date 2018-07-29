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

    private Map<SootFieldRef, Var> fieldMap = new HashMap<>();

    public Val(Value value) {
        this.value = value;
        this.type = value.getType();
        this.typeName = type.toString();
    }

    public Val duplicateDeeply() {
        Val val = new Val(value);
        for (Map.Entry<SootFieldRef, Var> entry : this.fieldMap.entrySet()) {
            val.fieldMap.put(entry.getKey(), entry.getValue().duplicate(true));
        }
        return val;
    }

    public Value getValue() {
        return value;
    }

    public Var getField(SootFieldRef fieldRef) {
        return fieldMap.get(fieldRef);
    }

    public void setField(SootFieldRef fieldRef, Var box) {
        Var existed = fieldMap.get(fieldRef);
        if (existed == null) {
            existed = box.duplicate(false);
            fieldMap.put(fieldRef, existed);
        } else {
            existed.assign(box);
        }
    }

    @Override
    public String toString() {
        return "Val{"
                + typeName + " "
                + "fieldMap:" + fieldMap.toString()
                + '}';
    }
}
