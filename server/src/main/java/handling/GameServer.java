package handling;

import handling.session.netty.NettyProvider;
import handling.session.SocketProvider;

import java.net.InetSocketAddress;

@lombok.extern.slf4j.Slf4j
public class GameServer {

    protected InetSocketAddress inetSocketAddress;
    private final SocketProvider socketProvider;
    protected final int channel, port;


    public GameServer(int channel, int port, PacketProcessor.Mode mode) {
        this.channel = channel;
        this.port = port;
        this.socketProvider = new NettyProvider();
        socketProvider.initSocket(channel, port, mode);
    }

    protected void unbindAll() {
        socketProvider.unbindAll();
    }

    public void onStart() {

    }

    public void shutdown() {
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }
}
