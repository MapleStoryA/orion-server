package handling.session.netty;

import client.MapleClient;
import constants.ServerConstants;
import handling.cashshop.CashShopServer;
import handling.packet.PacketProcessor;
import handling.session.DefaultPacketHandler;
import handling.world.WorldServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import tools.Randomizer;
import tools.packet.LoginPacket;

@Slf4j
public class NettyMapleServerHandler extends ChannelInboundHandlerAdapter {
    public static final AttributeKey<Object> CLIENT_KEY = AttributeKey.valueOf(MapleClient.CLIENT_KEY);
    private final PacketProcessor processor;
    private final PacketProcessor.Mode mode;
    private final int channel;

    public NettyMapleServerHandler(int channel, PacketProcessor.Mode mode) {
        this.channel = channel;
        this.mode = mode;
        this.processor = PacketProcessor.getProcessor(mode);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (channel > -1) {
            if (WorldServer.getInstance().getChannel(channel).isShutdown()) {
                ctx.channel().close();
                return;
            }
        } else if (PacketProcessor.Mode.CASHSHOP.equals(mode)) {
            if (CashShopServer.getInstance().isShutdown()) {
                ctx.channel().close();
                return;
            }
        }
        final byte[] ivSend = new byte[] {82, 48, 120, (byte) Randomizer.nextInt(255)};
        final byte[] ivRecv = new byte[] {70, 114, 122, (byte) Randomizer.nextInt(255)};
        final var client = new MapleClient(ivSend, ivRecv, new NettyNetworkSession(ctx.channel()));
        client.setChannel(channel);
        NettyMaplePacketEncoder encoder = new NettyMaplePacketEncoder();
        ctx.pipeline().addFirst(new NettyMaplePacketDecoder(client), encoder, new SendPingOnIdle(5, 1, 5, client));
        ctx.channel().writeAndFlush(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv));
        ctx.channel().attr(CLIENT_KEY).set(client);
        encoder.setClient(client);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] message = (byte[]) msg;
        var client = (MapleClient)
                ctx.channel().attr(AttributeKey.valueOf(MapleClient.CLIENT_KEY)).get();
        DefaultPacketHandler.handlePacket(client, processor, PacketProcessor.Mode.CASHSHOP.equals(mode), message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception with client", cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final MapleClient client = (MapleClient) ctx.channel().attr(CLIENT_KEY).get();
        if (client != null) {
            try {
                client.disconnect(true, PacketProcessor.Mode.CASHSHOP.equals(mode));
            } finally {
                ctx.channel().close();
                ctx.channel().attr(CLIENT_KEY).remove();
            }
        }
    }

    static class SendPingOnIdle extends IdleStateHandler {

        private final MapleClient client;

        public SendPingOnIdle(
                int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, MapleClient client) {
            super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
            this.client = client;
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {

            client.sendPing();

            super.channelIdle(ctx, evt);
        }
    }
}
