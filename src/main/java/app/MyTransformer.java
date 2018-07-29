package app;

import java.nio.file.*;
import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * @author xp
 * @author wss
 */
public class MyTransformer extends SceneTransformer {

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        try {
            Analyzer analyzer = new Analyzer();
            SootMethod mainMethod = Scene.v().getMainMethod();
            inMethod(mainMethod, new Scope(analyzer));
            Printer.save(Paths.get("result.txt"), analyzer.analyze());
            Printer.save(Paths.get("debug.txt"), Printer.getLogs());
        } catch (Throwable e) {
            // 救我狗命
            e.printStackTrace();
        }
    }

    private void inMethod(SootMethod method, Scope scope) {
        Body body = method.getActiveBody();
        UnitGraph graph = new BriefUnitGraph(body);
        Unit head = graph.getHeads().iterator().next(); // method 只可能有一个 head
        inBlock(head, scope, graph);
    }

    private void inBlock(Unit u, Scope scope, UnitGraph graph) {
        Printer.log(scope.depth(), scope.toString());
        while (true) {
            Printer.log(scope.depth(), u.toString());
            oneUnit(u, scope);
            List<Unit> succs = graph.getSuccsOf(u);
            int succCount = succs.size();
            if (succCount == 1) {
                u = succs.iterator().next();
            } else if (succCount > 1) {
                for (Unit succ : succs) {
                    Printer.log(scope.depth(), "branch " + succ.toString());
                    inBlock(succ, scope.createSameScope(), graph);
                }
                return;
            } else {
                return;
            }
        }
    }

//    private Unit inBlock(Unit u, Scope scope, UnitGraph graph) {
//        while (true) {
//            oneUnit(u, scope);
//            List<Unit> succs = graph.getSuccsOf(u);
//            int succCount = succs.size();
//            if (succCount == 1) {
//                u = succs.iterator().next();
//                List<Unit> preds = graph.getPredsOf(u);
//                if (preds.size() > 1) {
//                    return u;
//                } else {
//                    continue;
//                }
//            } else if (succCount > 1) {
//                Map<Unit, Scope> unitScopeMap = new HashMap<>();
//                for (Unit succ : succs) {
//                    Scope subScope = new Scope(scope);
//                    succ = inBlock(succ, subScope, graph);
//                    if (succ != null) {
//                        Scope existed = unitScopeMap.get(succ);
//                        if (existed == null) {
//                            existed = scope.createSameScope();
//                            unitScopeMap.put(succ, existed);
//                        }
//                        existed.join(subScope);
//                    }
//                }
//                for (Map.Entry<Unit, Scope> entry : unitScopeMap.entrySet()) {
//                    inBlock(entry.getKey(), entry.getValue(), graph);
//                }
//            }
//            return null;
//        }
//    }

    private void oneUnit(Unit u, Scope scope) {
        if (u instanceof IdentityStmt) {
            IdentityStmt is = (IdentityStmt) u;
            Value lop = is.getLeftOp();
            Value rop = is.getRightOp();
            if (rop instanceof ParameterRef) {
                // @parameter
                ParameterRef pr = (ParameterRef) rop;
                scope.bindArg((Local) lop, pr.getIndex());
            } else if (rop instanceof ThisRef) {
                // @this
                scope.bindThis((Local) lop);
            }
        } else if (u instanceof AssignStmt) {
            AssignStmt as = (AssignStmt) u;
            Value lop = as.getLeftOp();
            Value rop = as.getRightOp();

            Var rvar = null;
            if (rop instanceof Constant || rop instanceof NewExpr) {
                rvar = scope.createVarBox(rop);
            } else if (rop instanceof Local) {
                rvar = scope.getOrAdd((Local) rop);
            } else if (rop instanceof InstanceFieldRef) {
                InstanceFieldRef rref = (InstanceFieldRef) rop;
                Local rbase = (Local) rref.getBase();
                SootFieldRef rfield = rref.getFieldRef();
                Var rbaseVar = scope.getOrAdd(rbase);
                rvar = rbaseVar.getVal().getField(rfield);
            } else {
                return;
            }
            if (rvar != null) {
                if (lop instanceof Local) {
                    Var lvar = scope.getOrAdd((Local) lop);
                    lvar.assign(rvar);
                } else if (lop instanceof InstanceFieldRef) {
                    InstanceFieldRef lref = (InstanceFieldRef) lop;
                    Local lbase = (Local) lref.getBase();
                    SootFieldRef lfield = lref.getFieldRef();
                    Var lbaseVar = scope.getOrAdd(lbase);
                    lbaseVar.getVal().setField(lfield, rvar);
                }
            }
        } else if (u instanceof InvokeStmt) {
            InvokeStmt is = (InvokeStmt) u;
            InvokeExpr ie = is.getInvokeExpr();
            if (ie != null) {
                SootMethod invokeMethod = ie.getMethod();
                String methodSignature = invokeMethod.getSignature();
                List<Value> invokeArgs = ie.getArgs();

                if (ie instanceof InstanceInvokeExpr) {
                    // specialinvoke
                    switch (methodSignature) {
                        case "<java.lang.Object: void <init>()>":
                            return;
                    }
                    InstanceInvokeExpr sie = (InstanceInvokeExpr) ie;
                    Local base = (Local) sie.getBase();
                    Var baseVar = scope.getOrAdd(base);
                    Scope invokeScope = scope.createInvokeScope(baseVar, invokeArgs);
                    Printer.log(scope.depth(), "invoke " + invokeMethod.toString());
                    inMethod(invokeMethod, invokeScope);
                } else {
                    // handle "staticinvoke" only
                    Analyzer analyzer = scope.getAnalyzer();
                    switch (methodSignature) {
                        case "<benchmark.internal.Benchmark: void alloc(int)>": {
                            int allocId = ((IntConstant) invokeArgs.get(0)).value;
                            analyzer.setAllocId(allocId);
                            break;
                        }
                        case "<benchmark.internal.Benchmark: void test(int,java.lang.Object)>": {
                            int id = ((IntConstant) invokeArgs.get(0)).value;
                            Local local = (Local) invokeArgs.get(1);
                            analyzer.addQuery(id, local);
                            break;
                        }
                    }
                }
            }
        } else if (u instanceof ReturnStmt || u instanceof ReturnVoidStmt) {
            Printer.log(scope.depth(), scope.toString());
        }
    }
}
