package handling.session;

import java.net.SocketAddress;

public interface NetworkSession {

    void write(byte[] packet);

    void close();

    SocketAddress getRemoteAddress();

    boolean isConnected();
}
