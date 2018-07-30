package test;

import java.util.*;

import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;

public class MyTest4 {

    public static void main(String[] args) {
        int j = 10;
        while (j > 0) {
            B b1 = new B();
            j--;
        }
        for (int i = 10; i > 0; i--) {
            B b2 = new B();
        }
    }
}