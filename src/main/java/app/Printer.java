package app;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author xp
 */
public class Printer {

    private static List<String> logs = new LinkedList<>();

    public static void log(String s) {
        System.out.println(s);
        // logs.add(s);
    }

    public static void log(int depth, String s) {
        StringBuilder sb = new StringBuilder(depth + s.length());
        for (int i = depth; i > 0; i--) {
            sb.append('\t');
        }
        sb.append(s);
        log(sb.toString());
    }

    public static List<String> getLogs() {
        return logs;
    }

    public static void save(Path path, String s) {
        try {
            Files.write(path, s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(Path path, Collection<String> s) {
        if (!s.isEmpty()) {
            try {
                Files.write(path, s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
