package client;

import lombok.extern.slf4j.Slf4j;
import networking.NetworkSession;

@Slf4j
public class PingThread implements Runnable {

    private final BaseMapleClient client;

    public PingThread(BaseMapleClient client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        final long then = System.currentTimeMillis();
        long difference = (then - client.getLastPong()) / 1000;
        if (difference > 60000) {
            NetworkSession session = client.getSession();
            if (session != null && session.isConnected()) {
                session.close();
                return;
            }
        }
        client.sendPing();
    }
}
