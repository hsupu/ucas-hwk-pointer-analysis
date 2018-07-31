# 程序分析 大作业 报告

## 题目要求

给出 `Benchmark.alloc(allocId)` 作为标记点，然后其后的第一处 new 得到的对象的变量需要被标记为（可能）来自该点。经过一系列可能的变量赋值，最终会有 `Benchmark.test(testId, var)` 来询问特定变量的值可能来自哪些标记点。

输出格式类似于 `<testId>: <allocId> <allocId>...`，每行一个。

## 样例输入

```java
public static void main(String[] args) {
    Benchmark.alloc(1);
    A a = new A();
    Benchmark.alloc(2);
    A b = new A();
    Benchmark.alloc(3);
    A c = new A();
    if (args.length>1) a=b;

    Benchmark.test(1, a);
    Benchmark.test(2, c);
}
```

上例应输出

```
1: 1 2
2: 3
```

## 设计思想

根据题意，需使用单独的程序对给定的 Java 代码做静态分析。我们组采用了 [SOOT 框架](https://github.com/Sable/soot)。

通过 `soot.Scene.v().getMainMethod()` 获取到程序入口。通过模拟变量值 `Val`、引用 `Var`、作用域 `Scope` 构建合适的分析环境。通过模拟程序执行（顺序执行、函数调用和分支处理），跟踪桩代码和变量引用的指向，将所需信息存入全局的 `Analyzer` 对象。最后根据查询要求，从 `Analyzer` 中获取相应的结果。

因为是静态分析，发现递归函数（在函数调用链上发现同一函数签名）和成环分支（在分支的前驱链上发现同一语句，一般是循环语句）时，会予以忽略。

## 题外话

因为是大作业，只为了完成作业要求做了一点工作，简单了解了 SOOT 是什么而已，尚有很多真正需要研究的地方没有学习。我们的研究方向也意不在此，粗浅涉猎一下，不多深究。

看到这个仓库的同学、同行：诸君，有缘，江湖再见。

