package server;

import server.config.ServerEnvironment;

@lombok.extern.slf4j.Slf4j
public class ServerProperties {


    public static String getProperty(String key) {
        return ServerEnvironment.getConfig().getProperty(key);
    }

    public static String getProperty(String s, String def) {
        return ServerEnvironment.getConfig().getProperty(s, def);
    }


}
