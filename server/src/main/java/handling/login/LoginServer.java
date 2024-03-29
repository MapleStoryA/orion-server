package handling.login;

import client.MapleClient;
import database.LoginState;
import handling.world.WorldServer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import networking.GameServer;
import networking.packet.PacketProcessor;
import server.base.config.Config;
import server.base.config.ServerConfig;
import server.base.timer.Timer;
import tools.MaplePacketCreator;
import tools.packet.LoginPacket;

@Slf4j
public class LoginServer extends GameServer {

    public static final int PORT = 8484;
    private static LoginServer INSTANCE;
    private final String serverName;
    private final String eventMessage;
    private final int userLimit;
    private final boolean adminOnly;
    private Map<Integer, Integer> load = new HashMap<>();
    private byte flag;
    private int usersOn = 0;
    private boolean finishedShutdown = true;
    private long lastUpdate = 0;

    public LoginServer(ServerConfig config) {
        super(-1, PORT, PacketProcessor.Mode.LOGINSERVER);
        Config.Login login = config.getConfig().getLogin();
        userLimit = login.getUserlimit();
        serverName = login.getServerName();
        eventMessage = login.getEventMessage();
        flag = login.getFlag();
        adminOnly = config.getConfig().getWorld().isAdminOnly();
    }

    public static synchronized LoginServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LoginServer(ServerConfig.serverConfig());
        }
        return INSTANCE;
    }

    public void addChannel(final int channel) {
        load.put(channel, 0);
    }

    public void removeChannel(final int channel) {
        load.remove(channel);
    }

    public void shutdown() {
        if (finishedShutdown) {
            return;
        }
        log.info("Shutting down login...");
        super.shutdown();
        finishedShutdown = true;
    }

    public final String getServerName() {
        return serverName;
    }

    public final String getServerEventMessage() {
        return eventMessage;
    }

    public final byte getServerFlag() {
        return flag;
    }

    public final Map<Integer, Integer> getServerLoad() {
        return load;
    }

    public void setLoad(final Map<Integer, Integer> load, final int usersOn) {
        this.load = load;
        this.usersOn = usersOn;
    }

    public final void setFlag(final byte flag) {
        this.flag = flag;
    }

    public final int getUserLimit() {
        return userLimit;
    }

    public final int getUsersOn() {
        return usersOn;
    }

    public final boolean isAdminOnly() {
        return adminOnly;
    }

    public void registerClient(final MapleClient c) {
        if (LoginServer.getInstance().isAdminOnly()) {
            if (!c.getAccountData().isGameMaster()) {
                c.getSession()
                        .write(MaplePacketCreator.serverNotice(
                                1,
                                "The server is currently set to Admin login only.\r\n"
                                        + "We are currently testing some issues.\r\n"
                                        + "Please try again later."));
                c.getSession().write(LoginPacket.getLoginFailed(7));
                return;
            }
        }

        if (System.currentTimeMillis() - lastUpdate > 600000) {
            lastUpdate = System.currentTimeMillis();
            final Map<Integer, Integer> load = WorldServer.getInstance().getChannelLoad();
            int usersOn = 0;
            final double loadFactor = 1200 / ((double) LoginServer.getInstance().getUserLimit() / load.size());
            for (Map.Entry<Integer, Integer> entry : load.entrySet()) {
                usersOn += entry.getValue();
                load.put(entry.getKey(), Math.min(1200, (int) (entry.getValue() * loadFactor)));
            }
            LoginServer.getInstance().setLoad(load, usersOn);
            lastUpdate = System.currentTimeMillis();
        }
        c.updateLoginState(LoginState.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
        c.setIdleTask(
                Timer.PingTimer.getInstance().schedule(() -> c.getSession().close(), 10 * 60 * 10000));
    }
}
