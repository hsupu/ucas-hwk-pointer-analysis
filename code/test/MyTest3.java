package test;

import java.util.*;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

public class MyTest3 {

    public static void main(String[] args) {
        Benchmark.alloc(1);
        B[] bs = new B[3];
        Benchmark.alloc(2);
        bs[0] = new B();
        bs[1] = bs[0];
        Benchmark.alloc(3);
        bs[2] = new B();

        Benchmark.test(1, bs);
        Benchmark.test(2, bs[0]);
        Benchmark.test(3, bs[1]);
        Benchmark.test(4, bs[2]);
    }
}