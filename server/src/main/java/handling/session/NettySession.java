package handling.session;

import io.netty.channel.Channel;

import java.net.SocketAddress;

public class NettySession implements NetworkSession {
    private final Channel channel;

    public NettySession(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void write(byte[] packet) {
        channel.write(packet);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public boolean isConnected() {
        return channel.isActive();
    }
}
