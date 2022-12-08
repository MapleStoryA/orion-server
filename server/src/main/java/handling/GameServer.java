package handling;

import handling.MinaMapleServerHandler;
import handling.NettyMapleServerHandler;
import handling.PacketProcessor;
import handling.session.MinaMapleCodecFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
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

    interface SocketProvider {
        void initSocket(int channel, int port, PacketProcessor.Mode mode);

        void unbindAll();
    }

    class MinaProvider implements SocketProvider {

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
                inetSocketAddress = new InetSocketAddress(port);
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

    @Slf4j
    static class NettyProvider implements SocketProvider {


        private EventLoopGroup eventLoopGroupBoss, eventLoopGroupWorker;

        private ChannelFuture serverChannel;

        @Override
        public void initSocket(int channel, int port, PacketProcessor.Mode mode) {
            eventLoopGroupBoss = new NioEventLoopGroup();
            eventLoopGroupWorker = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();

            b.group(eventLoopGroupBoss, eventLoopGroupWorker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new NettyMapleServerHandler(channel,
                                    PacketProcessor.Mode.CASHSHOP.equals(mode),
                                    PacketProcessor.getProcessor(mode)));

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            try {
                serverChannel = b.bind(port).sync();
                log.info("Server bound to port " + port);

                serverChannel.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                eventLoopGroupWorker.shutdownGracefully();
                eventLoopGroupBoss.shutdownGracefully();
                log.info("Login server closed...");
            }
        }

        @Override
        public void unbindAll() {

        }
    }

    protected void unbindAll() {
        socketProvider.unbindAll();
    }

    public void onStart() {

    }

    public void shutdown() {
    }

}
