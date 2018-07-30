package app;

import java.util.*;

import soot.*;

/**
 * @author xp
 * @author wss
 * 模拟了变量（值），支持子字段的模拟
 */
public class Val {

    private final Value value;

    private Type type;

    private String typeName;

    private Map<SootFieldRef, Var> fieldMap;

    private List<Var> elements;

    public Val(Value value) {
        this.value = value;
        this.type = value.getType();
        this.typeName = type.toString();

        if (type instanceof RefLikeType) {
            fieldMap = new HashMap<>();
        }
        if (type instanceof ArrayType) {
            elements = Collections.emptyList();
        }
    }

    public Val duplicateDeeply() {
        Val val = new Val(value);
        for (Map.Entry<SootFieldRef, Var> entry : this.fieldMap.entrySet()) {
            val.fieldMap.put(entry.getKey(), entry.getValue().duplicate(true));
        }
        int arrSize = this.elements.size();
        if (arrSize > 0) {
            val.elements = new ArrayList<>(arrSize);
            for (Var var : this.elements) {
                val.elements.add(var.duplicate(true));
            }
        }
        return val;
    }

    public Value getValue() {
        return value;
    }

    public Var getField(SootFieldRef fieldRef) {
        return fieldMap.get(fieldRef);
    }

    public void setField(SootFieldRef fieldRef, Var var) {
        Var existed = fieldMap.get(fieldRef);
        if (existed == null) {
            fieldMap.put(fieldRef, var.duplicate(false));
        } else {
            existed.assign(var);
        }
    }

    public void asFixedArray(int size) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalStateException("not a array: " + toString());
        }
        elements = new ArrayList<>(size);
        for (int i = size; i > 0; i--) {
            elements.add(null);
        }
    }

    public Var getElement(int index) {
        return elements.get(index);
    }

    public void setElement(int index, Var var) {
        Var existed = elements.get(index);
        if (existed == null) {
            elements.set(index, var.duplicate(false));
        } else {
            existed.assign(var);
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
