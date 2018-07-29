package test;

import java.util.*;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

public class MyTest1 {

    private static final Random random = new Random();

    public MyTest1() {
    }

    private static void assign(A x, A y) {
        y.f = x.f;
    }

    public static void main(String[] args) {
        int r1 = random.nextInt();
        int r2 = random.nextInt();
        Benchmark.alloc(1);
        B b = new B();
        Benchmark.alloc(2);
        A a = new A(b);
        Benchmark.alloc(3);
        A c = new A();
        Benchmark.alloc(4);
        A d = new A();
        if (r1 < r2) {
            c.g = d.g;
        } else if (r1 > r2) {
            a.g = c.g;
        } else {
            d.g = a.g;
        }
        Benchmark.alloc(5);
        B e = new B();
        assign(a, c);
        c.g = e;
        c = a;

        Benchmark.test(1, a.g); // expected: 2 3	b1: 2		b2: 3		not 4
    }
}