package server.base.config;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Config {
    private boolean debug;
    private boolean debugPacket;
    private short version;
    private String patch;
    private Database database;
    private Socket socket;
    private Login login;
    private World world;
    private Channel channel;

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Database {
        private String url;
        private String user;
        private String password;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Socket {
        private String provider;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Login {
        private String key;
        private String host;
        private String serverName;
        private String eventMessage;
        private byte flag;
        private int userlimit;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class World {
        private int exp;
        private int meso;
        private int drop;
        private int cash;
        private boolean adminOnly;
        private int flags;
        private String serverMessage;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Channel {
        private String host;
        private int count;
        private List<String> events;
    }
}
