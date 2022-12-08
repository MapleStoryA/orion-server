package handling.session.netty;

import client.MapleClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import tools.MapleAESOFB;
import tools.MapleCustomEncryption;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class NettyMaplePacketDecoder extends ByteToMessageDecoder {

    private final MapleClient client;
    private int length = -1;

    public NettyMaplePacketDecoder(MapleClient mapleClient) {
        this.client = mapleClient;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (length <= 0) {
            if (buf.readableBytes() < 4) {
                return;
            }
            length = buf.readInt();
            length = MapleAESOFB.getPacketLength(length);
        }

        if (buf.readableBytes() < length) {
            return;
        }

        byte[] decryptedPacket = new byte[length];

        buf.readBytes(decryptedPacket);

        client.getReceiveCrypto().crypt(decryptedPacket);
        MapleCustomEncryption.decryptData(decryptedPacket);

        ByteBuffer packetBuffer = ByteBuffer.wrap(decryptedPacket).order(ByteOrder.LITTLE_ENDIAN);

        byte[] payload = new byte[length - 2];

        packetBuffer.get(payload);

        length = -1;

        out.add(payload);
    }
}
