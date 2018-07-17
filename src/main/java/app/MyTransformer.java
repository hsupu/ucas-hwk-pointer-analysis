package app;

import java.nio.file.*;
import java.util.*;

import soot.*;
import soot.jimple.*;

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
            // MyPrinter.save(Paths.get("debug.txt"), MyPrinter.getLogs());
        } catch (Throwable e) {
            // 救我狗命
            e.printStackTrace();
        }
    }

    private void inMethod(SootMethod method, Scope scope) {
        PatchingChain<Unit> units = method.getActiveBody().getUnits();
        for (Unit u : units) {
            try {
                if (u instanceof IdentityStmt) {
                    IdentityStmt is = (IdentityStmt) u;
                    Value lop = is.getLeftOp();
                    Value rop = is.getRightOp();
                    if (rop instanceof ParameterRef) {
                        // @param
                        ParameterRef pr = (ParameterRef) rop;
                        scope.bind((Local) lop, pr.getIndex());
                    } else if (rop instanceof ThisRef) {
                        // @this
                        scope.bindThis((Local) lop);
                    }
                } else if (u instanceof AssignStmt) {
                    AssignStmt as = (AssignStmt) u;
                    Value lop = as.getLeftOp();
                    Value rop = as.getRightOp();

                    VarBox rbox = null;
                    if (rop instanceof Constant || rop instanceof NewExpr) {
                        rbox = scope.createVarBox(rop);
                    } else if (rop instanceof Local) {
                        rbox = scope.getOrAdd((Local) rop);
                    } else if (rop instanceof InstanceFieldRef) {
                        InstanceFieldRef rref = (InstanceFieldRef) rop;
                        Local rbase = (Local) rref.getBase();
                        SootFieldRef rfield = rref.getFieldRef();

                        VarBox rbaseBox = scope.getOrAdd(rbase);
                        rbox = rbaseBox.getVar().getField(rfield);
                    } else {
                        continue;
                    }
                    if (lop instanceof Local) {
                        VarBox lbox = scope.getOrAdd((Local) lop);
                        lbox.assign(rbox);
                    } else if (lop instanceof InstanceFieldRef) {
                        InstanceFieldRef lref = (InstanceFieldRef) lop;
                        Local lbase = (Local) lref.getBase();
                        SootFieldRef lfield = lref.getFieldRef();

                        VarBox lbaseBbox = scope.getOrAdd(lbase);
                        lbaseBbox.getVar().setField(lfield, rbox);
                    }
                } else if (u instanceof InvokeStmt) {
                    InvokeStmt is = (InvokeStmt) u;
                    InvokeExpr ie = is.getInvokeExpr();
                    if (ie != null) {
                        GlobalScope globalScope = scope.getGlobalScope();
                        SootMethod targetMethod = ie.getMethod();
                        String targetSignature = targetMethod.getSignature();
                        List<Value> invokeArgs = ie.getArgs();

                        if (ie instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr sie = (InstanceInvokeExpr) ie;
                            Local base = (Local) sie.getBase();
                            VarBox baseBox = scope.getOrAdd(base);
                            Scope targetScope = new Scope(globalScope, baseBox, invokeArgs);
                            inMethod(targetMethod, targetScope);
                        } else {
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
