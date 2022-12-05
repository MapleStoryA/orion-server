package server;

import server.config.ServerEnvironment;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Emilyx3
 */
public class ServerProperties {


    public static String getProperty(String key) {
        return ServerEnvironment.getConfig().getProperty(key);
    }

    public static String getProperty(String s, String def) {
        return ServerEnvironment.getConfig().getProperty(s, def);
    }


}
