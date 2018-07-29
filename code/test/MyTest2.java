package test;

import java.util.*;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

public class MyTest2 {

    private static final Random random = new Random();

    public MyTest2() {
    }

    private void assign(A x, A y) {
        y.f = x.f;
    }

    private void test() {
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
        
        Benchmark.test(1, a);   // expected: 2		b1: 2		b2: 2
        Benchmark.test(2, a.g); // expected: 2 3	b1: 2		b2: 3		not 4
        Benchmark.test(3, c.f); // expected: 1		b1: 1		b2: 1
        Benchmark.test(4, c);   // expected: 2

        // Benchmark.test(5, c.g);// expected : 5
    }

    /*
    result1:
    1: 2
    2: 2
    3: 1 2
    4: 2

    result2:
    1: 2
    2: 2 3
    3: 1 2
    4: 2
    */

    public static void main(String[] args) {
        MyTest2 fs2 = new MyTest2();
        fs2.test();
    }
}