package client.commands;

import client.MapleClient;
import constants.ServerConstants;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandProcessor {

    private Map<String, Command> commands = new LinkedHashMap<>();

    private static CommandProcessor INSTANCE;

    private void register(Class<? extends Command> gameCommandClass) {
        try {
            var instance = (Command) gameCommandClass.getDeclaredConstructors()[0].newInstance();
            commands.put(instance.getTrigger(), instance);

        } catch (Exception e) {
            log.error("Cannot load command", e);
        }
    }

    public static CommandProcessor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandProcessor();
            try {
                Class[] classes = getClasses(INSTANCE.getClass().getPackageName());
                Arrays.stream(classes).forEach(c -> {
                    if (Command.class.isAssignableFrom(c) && !c.isInterface()) {
                        INSTANCE.register(c);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return INSTANCE;
    }

    public boolean processLine(MapleClient c, String line) {
        final String[] splited = line.split(" ");
        if (!c.getAccountData().isGameMaster()) {
            return false;
        }
        for (var entry : commands.entrySet()) {
            var command = entry.getKey();
            if (command.equals(splited[0].toLowerCase().replace("!", ""))) {
                String[] args = new String[splited.length - 1];
                System.arraycopy(splited, 1, args, 0, args.length);
                var impl = entry.getValue();
                impl.execute(c, args);
                return true;
            }
        }

        return false;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package
     * and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName
                        + '.'
                        + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static boolean processCommand(MapleClient c, String line, ServerConstants.CommandType type) {

        if (CommandProcessor.getInstance().processLine(c, line)) {
            return true;
        }
        return false;
    }
}
