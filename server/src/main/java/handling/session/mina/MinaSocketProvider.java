package handling.session.mina;

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
public class MinaSocketProvider implements SocketProvider {

    private IoAcceptor acceptor;

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
            acceptor.bind(inetSocketAddress, new MinaMapleServerHandler(channel,
                    mode,
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
