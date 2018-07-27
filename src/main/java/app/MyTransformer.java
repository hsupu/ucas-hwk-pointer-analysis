package app;

import java.nio.file.*;
import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.BriefUnitGraph;

/**
 * @author xp
 */
public class MyTransformer extends SceneTransformer {

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        try {
            GlobalScope globalScope = new GlobalScope();
            SootMethod mainMethod = Scene.v().getMainMethod();
            inMethod(mainMethod, new Scope(globalScope, null, Collections.emptyList()));
            MyPrinter.save(Paths.get("result.txt"), globalScope.analyze());
            MyPrinter.save(Paths.get("debug.txt"), MyPrinter.getLogs());
        } catch (Throwable e) {
            // 救我狗命
            e.printStackTrace();
        }
    }

    private void inMethod(SootMethod method, Scope scope) {
        Body body = method.getActiveBody();
        UnitGraph bg = new BriefUnitGraph(body);
        Iterator<Unit>	it = bg.iterator();
        Unit u = it.next();
        inBranch(u, bg, scope);
    }
    private Unit inBranch(Unit u, UnitGraph bg, Scope scope){
        List<Unit> succs = new ArrayList<>();
        try {
        	do{
            	MyPrinter.log("InBranch Scope:\n"+scope.toString());
            	oneUnit(u, scope);      	
            	succs = bg.getSuccsOf(u);
            	List<Unit> preds = bg.getPredsOf(u);
            	//MyPrinter.log("preds: "+preds.toString());
            	//MyPrinter.log(u.toString());
            	//MyPrinter.log("succs: "+succs.toString());
            	
    	        if(succs.size()==1){
    	        	u = succs.get(0);
    	        }else if (succs.size()==2){//if i0 <= i1 goto r10 = r9
    	        	Unit u1 = (Unit)succs.get(0);
    	        	Scope scope1 = scope.clone();
    	        	MyPrinter.log("InBranch scope1:\n"+scope1.toString());
    	        	u = inBranch(u1, bg, scope1);
    	        	MyPrinter.log("InBranch scope1:\n"+scope1.toString());

    	        	Unit u2 = (Unit)succs.get(1);
    	        	Scope scope2 = scope.clone();
    	        	MyPrinter.log("InBranch scope2:\n"+scope2.toString());
    	        	u = inBranch(u2, bg, scope2);
    	        	MyPrinter.log("InBranch scope2:\n"+scope2.toString());
    	        	
    	        	scope.reset(scope1.join(scope2));
    	        	MyPrinter.log("InBranch join scope:\n"+scope.toString());
    	        }
            	if(preds.size()>1){
            		return u;
            	}
        	}while(succs.size()>0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    	return u;
    }
    
    private void oneUnit(Unit u, Scope scope){
    	if (u instanceof IdentityStmt) {//xx:=xxx
            IdentityStmt is = (IdentityStmt) u;
            Value lop = is.getLeftOp();
            Value rop = is.getRightOp();
            if (rop instanceof ParameterRef) {//@parameterxx
                // @param
                ParameterRef pr = (ParameterRef) rop;
                scope.bind((Local) lop, pr.getIndex());
            } else if (rop instanceof ThisRef) {//@this
                // @this
                scope.bindThis((Local) lop);
            }
        } else if (u instanceof AssignStmt) {//xx=xxx
            AssignStmt as = (AssignStmt) u;
            Value lop = as.getLeftOp();
            Value rop = as.getRightOp();

            VarBox rbox = null;
            if (rop instanceof Constant || rop instanceof NewExpr) {//new benchmark.objects.B
                rbox = scope.createVarBox(rop);
            } else if (rop instanceof Local) {//r1 or $r1
                rbox = scope.getOrAdd((Local) rop);
            } else if (rop instanceof InstanceFieldRef) {//r2.<benchmark.objects.A: benchmark.objects.B g>
                InstanceFieldRef rref = (InstanceFieldRef) rop;
                Local rbase = (Local) rref.getBase(); //r2
                SootFieldRef rfield = rref.getFieldRef();//<benchmark.objects.A: benchmark.objects.B g>
                VarBox rbaseBox = scope.getOrAdd(rbase);
                rbox = rbaseBox.getVar().getField(rfield);
            } else {//$i0 virtualinvoke $r8.<java.util.Random: int nextInt()>()
            	return;
            }
            if (lop instanceof Local) {//r1 or $r1
                VarBox lbox = scope.getOrAdd((Local) lop);
                lbox.assign(rbox);
            } else if (lop instanceof InstanceFieldRef) {//r2.<benchmark.objects.A: benchmark.objects.B g>
                InstanceFieldRef lref = (InstanceFieldRef) lop;
                Local lbase = (Local) lref.getBase();
                SootFieldRef lfield = lref.getFieldRef();
                VarBox lbaseBbox = scope.getOrAdd(lbase);
                lbaseBbox.getVar().setField(lfield, rbox);
            }
        } else if (u instanceof InvokeStmt) {//xxinvoke xxx
            InvokeStmt is = (InvokeStmt) u;
            InvokeExpr ie = is.getInvokeExpr();
            if (ie != null) {
                GlobalScope globalScope = scope.getGlobalScope();
                SootMethod targetMethod = ie.getMethod();
                String targetSignature = targetMethod.getSignature();//<test.FieldSensitivity: void assign(benchmark.objects.A,benchmark.objects.A)>
                List<Value> invokeArgs = ie.getArgs();//[r2, r3]

                if (ie instanceof InstanceInvokeExpr) {//specialinvoke
                    InstanceInvokeExpr sie = (InstanceInvokeExpr) ie;
                    Local base = (Local) sie.getBase();
                    VarBox baseBox = scope.getOrAdd(base);
                    Scope targetScope = new Scope(globalScope, baseBox, invokeArgs);
                    inMethod(targetMethod, targetScope);//recursive
                } else {//staticinvoke
                    switch (targetSignature) {
                        case "<benchmark.internal.Benchmark: void alloc(int)>": {
                            int allocId = ((IntConstant) invokeArgs.get(0)).value;
                            globalScope.setAllocId(allocId);
                            break;
                        }
                        case "<benchmark.internal.Benchmark: void test(int,java.lang.Object)>": {
                            int id = ((IntConstant) invokeArgs.get(0)).value;
                            Local local = (Local) invokeArgs.get(1);
                            globalScope.addQuery(id, local);
                            break;
                        }
                    }
                }
            }
        }else{
        	//MyPrinter.log("other "+u.toString());
        }
    	return;
    }
}
