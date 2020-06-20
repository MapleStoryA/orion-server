package client;

import org.apache.mina.common.IoSession;

public class PingThread implements Runnable {

  private final MapleClient client;

  public PingThread(MapleClient client) {
    super();
    this.client = client;
  }

  @Override
  public void run() {
    final long then = System.currentTimeMillis();
    long difference = (then - client.getLastPong()) / 1000;
    if (difference > 60000) {
      IoSession session = client.getSession();
      if (session != null && session.isConnected()) {
        session.close();
        return;
      }
    }
    client.sendPing();
  }

}
