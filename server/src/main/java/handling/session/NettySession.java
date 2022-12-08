package handling.session;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.concurrent.locks.ReentrantLock;

public class NettySession implements NetworkSession {
    private final Channel channel;

    private ReentrantLock connectionLock = new ReentrantLock(true);

    public NettySession(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void write(byte[] packet) {
        connectionLock.lock();
        try {
            channel.writeAndFlush(packet);
        } finally {
            connectionLock.unlock();
        }
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
