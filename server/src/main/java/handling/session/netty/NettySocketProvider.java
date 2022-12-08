package handling.session.netty;

import handling.PacketProcessor;
import handling.session.SocketProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettySocketProvider implements SocketProvider {


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
