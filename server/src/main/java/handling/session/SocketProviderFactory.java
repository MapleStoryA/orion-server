package handling.session;

import lombok.extern.slf4j.Slf4j;
import server.config.ServerEnvironment;

@Slf4j
public class SocketProviderFactory {

    public static SocketProvider getSocketProvider() {
        try {
            var config = ServerEnvironment.serverConfig();
            var clazz = Class.forName(config.getConfig().getSocket().getProvider());
            return (SocketProvider) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Could not instantiate the socket provider", e);
            throw new RuntimeException(e);
        }
    }
}
