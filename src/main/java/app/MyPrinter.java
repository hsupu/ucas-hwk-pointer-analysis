package app;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author xp
 */
public class MyPrinter {

    private static List<String> logs = new LinkedList<>();

    public static void log(String s) {
        logs.add(s);
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
        try {
            Files.write(path, s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
