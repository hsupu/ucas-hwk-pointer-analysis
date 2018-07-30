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
            inMethod(mainMethod, Scope.of(analyzer));
            Printer.save(Paths.get("result.txt"), analyzer.analyze());
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
                    String branchSignature = succ.toString();
                    if (scope.isInBranchChain(branchSignature)) {
                        // 静态分析不处理循环
                        continue;
                    }
                    Printer.log(scope.depth(), "branch " + branchSignature);
                    inBlock(succ, scope.createBranchScope(branchSignature), graph);
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
//                            existed = scope.createBranchScope();
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
        try {
            Analyzer analyzer = scope.getAnalyzer();
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
                if (rop instanceof AnyNewExpr) {
                    rvar = scope.createVarBox(rop);
                    if (rop instanceof NewArrayExpr) {
                        NewArrayExpr rae = (NewArrayExpr) rop;
                        Value arrSizeValue = rae.getSize();
                        if (arrSizeValue instanceof IntConstant) {
                            int arrSize = ((IntConstant) arrSizeValue).value;
                            rvar.getVal().asFixedArray(arrSize);
                        }
                    }
                    // 根据测评要求，allocId 只对紧接着的一条 AssignStmt NewExpr 有效
                    rvar.addSource(analyzer.getAllocId());
                    analyzer.setAllocId(0);
                } else if (rop instanceof Constant) {
                    // 测评不需要处理非引用类型
                    // rvar = scope.createVarBox(rop);
                } else if (rop instanceof Local) {
                    rvar = scope.getOrAdd((Local) rop);
                } else if (rop instanceof FieldRef) {
                    FieldRef rfref = (FieldRef) rop;
                    SootFieldRef rfield = rfref.getFieldRef();
                    if (rop instanceof InstanceFieldRef) {
                        // 实例 字段
                        InstanceFieldRef rifref = (InstanceFieldRef) rop;
                        Local rbase = (Local) rifref.getBase();
                        Var rbaseVar = scope.getOrAdd(rbase);
                        rvar = rbaseVar.getVal().getField(rfield);
                    } else {
                        // 类 字段
                    }
                } else if (rop instanceof ArrayRef) {
                    // 数组元素
                    ArrayRef rref = (ArrayRef) rop;
                    Local rbase = (Local) rref.getBase();
                    Value rindexValue = rref.getIndex();
                    if (rindexValue instanceof IntConstant) {
                        Var rbaseVar = scope.getOrAdd(rbase);
                        int rindex = ((IntConstant) rindexValue).value;
                        rvar = rbaseVar.getVal().getElement(rindex);
                    }
                } else {
                    // 其他右值
                    return;
                }
                if (rvar != null) {
                    if (lop instanceof Local) {
                        // 本地变量
                        Var lvar = scope.getOrAdd((Local) lop);
                        lvar.assign(rvar);
                    } else if (lop instanceof FieldRef) {
                        FieldRef lfref = (FieldRef) lop;
                        SootFieldRef lfield = lfref.getFieldRef();
                        if (lop instanceof InstanceFieldRef) {
                            // 实例 字段
                            InstanceFieldRef lifref = (InstanceFieldRef) lop;
                            Local lbase = (Local) lifref.getBase();
                            Var lbaseVar = scope.getOrAdd(lbase);
                            lbaseVar.getVal().setField(lfield, rvar);
                        } else {
                            // 类 字段
                        }
                    } else if (lop instanceof ArrayRef) {
                        // 数组元素
                        ArrayRef lref = (ArrayRef) lop;
                        Local lbase = (Local) lref.getBase();
                        Value lindexValue = lref.getIndex();
                        if (lindexValue instanceof IntConstant) {
                            Var lbaseVar = scope.getOrAdd(lbase);
                            int lindex = ((IntConstant) lindexValue).value;
                            lbaseVar.getVal().setElement(lindex, rvar);
                        }
                    } else {
                        // 其他左值
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
                        // 跳过一些空函数，提高日志可读性
                        switch (methodSignature) {
                            case "<java.lang.Object: void <init>()>":
                                return;
                        }
                        if (scope.isInInvokeChain(methodSignature)) {
                            // 静态分析不处理递归函数
                            return;
                        }
                        InstanceInvokeExpr sie = (InstanceInvokeExpr) ie;
                        Local base = (Local) sie.getBase();
                        Var baseVar = scope.getOrAdd(base);
                        Scope invokeScope = scope.createInvokeScope(methodSignature, baseVar, invokeArgs);
                        Printer.log(scope.depth(), "invoke " + invokeMethod.toString());
                        inMethod(invokeMethod, invokeScope);
                    } else {
                        // 测评需要，只处理 staticinvoke
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
