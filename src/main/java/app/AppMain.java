package app;

import java.io.*;
import java.util.*;

import soot.PackManager;
import soot.Transform;

/**
 * @author xp
 */
public class AppMain {

    private static String joinPath(String... pieces) {
        return String.join(File.separator, (String[]) pieces);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{
                    "code",
                    "test.FieldSensitivity"
            };
        }

        List<String> classpath = new ArrayList<>();
        classpath.add(args[0]);

//        String jreHome = System.getProperty("java.home");
//        classpath.add(joinPath(jreHome, "lib", "rt.jar"));
//        classpath.add(joinPath(jreHome, "lib", "jce.jar"));
        classpath.add(joinPath(args[0], "rt.jar"));
        classpath.add(joinPath(args[0], "jce.jar"));

//        soot.options.Options.v().set_whole_program(true);

        // whole-jimple transformation pack
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myapp", new MyTransformer()));
        soot.Main.main(new String[]{
                "-w",
                "-f", "J",
                "-p", "cg.spark", "enabled:true",
                "-p", "wjtp.myapp", "enabled:true",
                "-soot-class-path", String.join(File.pathSeparator, classpath),
                args[1]
        });
    }
}
