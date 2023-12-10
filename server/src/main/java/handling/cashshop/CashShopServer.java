package handling.cashshop;

import handling.GameServer;
import handling.PacketProcessor;
import handling.channel.PlayerStorage;
import lombok.extern.slf4j.Slf4j;
import server.config.ServerEnvironment;

@Slf4j
public class CashShopServer extends GameServer {

    private static final int PORT = 8799;
    private String ip;
    private PlayerStorage players;
    private boolean finishedShutdown = false;

    private static CashShopServer INSTANCE;

    public CashShopServer() {
        super(-1, 8799, PacketProcessor.Mode.CASHSHOP);
        players = new PlayerStorage(-10);
        ip = ServerEnvironment.serverConfig().getConfig().getChannel().getHost() + ":" + PORT;
    }

    public final String getPublicAddress() {
        return ip;
    }

    public final PlayerStorage getPlayerStorage() {
        return players;
    }

    public final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        log.info("Saving all connected clients (CS)...");
        players.disconnectAll();
        log.info("Shutting down CS...");
        super.shutdown();
        finishedShutdown = true;
    }

    public boolean isShutdown() {
        return finishedShutdown;
    }

    public static CashShopServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CashShopServer();
        }
        return INSTANCE;
    }
}
