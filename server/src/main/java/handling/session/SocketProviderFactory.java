package handling.session;

import handling.session.mina.MinaSocketProvider;
import handling.session.netty.NettySocketProvider;

public class SocketProviderFactory {

    private static boolean isNetty = true;

    public static SocketProvider getSocketProvider() {
        if (!isNetty) {
            return new MinaSocketProvider();
        } else {
            return new NettySocketProvider();
        }
    }
}
