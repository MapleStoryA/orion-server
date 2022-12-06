package handling.login;

import handling.MapleServerHandler;
import handling.PacketProcessor;
import handling.mina.MapleCodecFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GameServer {

    protected InetSocketAddress inetSocketAddress;
    protected IoAcceptor acceptor;

    protected int channel, port;

    public GameServer(int channel, int port, PacketProcessor.Mode mode) {
        this.channel = channel;
        this.port = port;
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        acceptor = new SocketAcceptor();
        final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getSessionConfig().setTcpNoDelay(true);
        cfg.setDisconnectOnUnbind(true);
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));

        try {
            inetSocketAddress = new InetSocketAddress(port);
            acceptor.bind(inetSocketAddress, new MapleServerHandler(channel, false, PacketProcessor.getProcessor(mode)), cfg);
            System.out.println("Listening on port " + port + ".");
        } catch (IOException e) {
            System.err.println("Binding to port " + port + " failed" + e);
        }
    }

    protected void unbindAcceptor() {
        acceptor.unbindAll();
        acceptor = null;
    }

    public void shutdown() {
    }

}
