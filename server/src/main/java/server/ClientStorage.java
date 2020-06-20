package server;

import client.MapleClient;
import org.apache.mina.common.IoSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStorage {

  private static final Map<Integer, MapleClient> clients = new ConcurrentHashMap<>();

  public static void addClient(MapleClient client) {
    clients.put(client.getAccID(), client);
  }

  public static void removeClient(MapleClient client) {
    clients.remove(client.getAccID());
  }

  public static boolean isConnected(MapleClient client) {
    boolean isConnected = clients.containsKey(client.getAccID());
    if (isConnected) {
      MapleClient connected = clients.get(client.getAccID());
      IoSession session = connected.getSession();
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
