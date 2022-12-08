package handling;

import client.MapleClient;
import constants.ServerConstants;
import handling.cashshop.CashShopOperationHandlers;
import handling.cashshop.CashShopServer;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.PlayerHandler;
import handling.session.NettyMaplePacketDecoder;
import handling.session.NettyMaplePacketEncoder;
import handling.session.NettySession;
import handling.world.WorldServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import server.config.ServerEnvironment;
import tools.FileOutputUtil;
import tools.HexTool;
import tools.Randomizer;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@Slf4j
public class NettyMapleServerHandler extends ChannelInboundHandlerAdapter {
    public static final AttributeKey<Object> CLIENT_KEY = AttributeKey.valueOf(MapleClient.CLIENT_KEY);
    private final boolean isCashShop;
    private final PacketProcessor processor;
    private final int channel;

    public NettyMapleServerHandler(int channel, boolean isCashShop, PacketProcessor processor) {
        this.channel = channel;
        this.isCashShop = isCashShop;
        this.processor = processor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final String address = ctx.channel().remoteAddress().toString().split(":")[0];
        if (channel > -1) {
            if (WorldServer.getInstance().getChannel(channel).isShutdown()) {
                ctx.channel().close();
                return;
            }
        } else if (isCashShop) {
            if (CashShopServer.getInstance().isShutdown()) {
                ctx.channel().close();
                return;
            }
        }
        final byte[] ivSend = new byte[]{82, 48, 120, (byte) Randomizer.nextInt(255)};
        final byte[] ivRecv = new byte[]{70, 114, 122, (byte) Randomizer.nextInt(255)};
        final var client = new MapleClient(ivSend, ivRecv, new NettySession(ctx.channel()));
        client.setChannel(channel);
        NettyMaplePacketEncoder encoder = new NettyMaplePacketEncoder();
        ctx.pipeline().addFirst(new NettyMaplePacketDecoder(client), encoder);
        ctx.channel().writeAndFlush(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv));
        ctx.channel().attr(CLIENT_KEY).set(client);
        encoder.setClient(client);
        logServer(address);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        byte[] message = (byte[]) msg;

        try {
            var slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(message));
            if (slea.available() < 2) {
                return;
            }
            var header_num = slea.readShort();
            var client = (MapleClient) ctx.channel().attr(AttributeKey.valueOf(MapleClient.CLIENT_KEY)).get();
            var packetHandler = processor.getHandler(header_num);
            if (ServerEnvironment.isDebugEnabled()) {
                log.info("Received: " + header_num);
            }
            if (ServerEnvironment.isDebugEnabled() && packetHandler != null) {
                log.info("[" + packetHandler.getClass().getSimpleName() + "]");
            }
            if (packetHandler != null && packetHandler.validateState(client)) {
                packetHandler.handlePacket(slea, client);
                return;
            }
            for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                if (recv.getValue() == header_num) {

                    if (!client.isReceiving()) {
                        return;
                    }
                    if (recv.needsChecking()) {
                        if (!client.isLoggedIn()) {
                            return;
                        }
                    }
                    handlePacket(recv, slea, client);
                    return;
                }
            }
            log.info("Received data: " + HexTool.toString((byte[]) message));
            log.info("Data: " + new String((byte[]) message));

        } catch (Exception e) {
            FileOutputUtil.outputFileError(FileOutputUtil.PacketEx_Log, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        final MapleClient client = (MapleClient) ctx.channel().attr(CLIENT_KEY).get();
        if (client != null) {
            try {
                client.disconnect(true, isCashShop);
            } finally {
                ctx.channel().close();
                ctx.channel().attr(CLIENT_KEY).remove();
            }
        }
    }

    private void logServer(String address) {
        StringBuilder sb = new StringBuilder();
        if (channel > -1) {
            sb.append("[Channel Server] Channel ").append(channel).append(" : ");
        } else if (isCashShop) {
            sb.append("[Cash Server]");
        } else {
            sb.append("[Login Server]");
        }
        sb.append("IoSession opened ").append(address);
        log.info(sb.toString());
    }

    private void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea,
                              final MapleClient c) {
        switch (header) {
            case PLAYER_LOGGEDIN:
                final int playerId = slea.readInt();
                if (isCashShop) {
                    CashShopOperationHandlers.enterCashShop(playerId, c);
                } else {
                    InterServerHandler.loggedIn(playerId, c);
                }
                break;
            case CHANGE_MAP:
                if (isCashShop) {
                    CashShopOperationHandlers.leaveCashShop(slea, c, c.getPlayer());
                } else {
                    PlayerHandler.changeMap(slea, c, c.getPlayer());
                }
                break;
            default:
                if (slea.available() >= 0) {
                    FileOutputUtil.logPacket(String.valueOf(header), "[" + header + "] " + slea);
                }
                break;
        }
    }
}
