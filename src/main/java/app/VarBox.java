package app;

import java.util.*;

import soot.Value;

/**
 * @author xp
 * The VarBox is a class to record variable(as Var) and its source.
 */
public class VarBox implements Cloneable{

    private String name;

    private Var var;

    private final Set<Integer> source = new HashSet<>();
    
    public VarBox(){
    	
    }
    
    public VarBox(Var var) {
        this.var = var;
    }

    public VarBox(VarBox varBox) {
        this.var = varBox.var;
        this.source.addAll(varBox.source);
    }
    
    public void addSource(Integer allocId) {
        if (allocId != null) {
            source.add(allocId);
        }
    }

    public void assign(VarBox varBox) {
        source.addAll(varBox.source);
        var = varBox.var;
    }

    public Var getVar() {
        return var;
    }

    public Set<Integer> getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "VarBox{"
                + name + " " 
                + var + " " 
                + source //+ " "+System.identityHashCode(source)
                + '}';
    }

    public static VarBox of(Value value) {
        Var var = new Var(value);
        return new VarBox(var);
    }
    
    public void reset(VarBox varBox) {
    	this.name = varBox.name;
        this.var = (Var)varBox.var;
        this.setSource(varBox.source);
    }
    
    public void setSource(Set<Integer> source){
    	this.source.clear();
    	for(Integer i : source){
    		this.source.add(i);
    	}
    }
   
    @Override
    protected Object clone() throws CloneNotSupportedException {
    	VarBox varBox = new VarBox((Var)this.var.clone());
    	varBox.setSource(this.source);
    	
		return varBox;
	}    
}
