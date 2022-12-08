package handling.session.mina;

import handling.GameServer;
import handling.MinaMapleServerHandler;
import handling.PacketProcessor;
import handling.session.SocketProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
@SuppressWarnings("unused")
public class MinaProvider implements SocketProvider {

    private final GameServer gameServer;
    private IoAcceptor acceptor;

    public MinaProvider(GameServer gameServer) {
        this.gameServer = gameServer;
    }


    public void initSocket(int channel, int port, PacketProcessor.Mode mode) {
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        acceptor = new SocketAcceptor();
        final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getSessionConfig().setTcpNoDelay(true);
        cfg.setDisconnectOnUnbind(true);
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaMapleCodecFactory()));

        try {
            var inetSocketAddress = new InetSocketAddress(port);
            gameServer.setInetSocketAddress(inetSocketAddress);
            acceptor.bind(inetSocketAddress, new MinaMapleServerHandler(channel,
                    PacketProcessor.Mode.CASHSHOP.equals(mode),
                    PacketProcessor.getProcessor(mode)), cfg);
            log.info("Listening on port " + port + ".");
        } catch (IOException e) {
            System.err.println("Binding to port " + port + " failed" + e);
        }
    }

    public void unbindAll() {
        acceptor.unbindAll();
        acceptor = null;
    }
}
