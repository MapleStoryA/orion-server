package server;

import client.MapleClient;
import handling.session.NetworkSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@lombok.extern.slf4j.Slf4j
public class ClientStorage {

    private static final Map<Integer, MapleClient> clients = new ConcurrentHashMap<>();

    public static void addClient(MapleClient client) {
        clients.put(client.getAccountData().getId(), client);
    }

    public static void removeClient(MapleClient client) {
        clients.remove(client.getAccountData().getId());
    }

    public static boolean isConnected(MapleClient client) {
        boolean isConnected = clients.containsKey(client.getAccountData().getId());
        if (isConnected) {
            MapleClient connected = clients.get(client.getAccountData().getId());
            NetworkSession session = connected.getSession();
            if (session == null || !session.isConnected()) {
                removeClient(connected);
                return false;
            } else {
                return true;
            }
        }
        return isConnected;
    }
}
