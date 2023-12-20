package networking.netty;

import client.MapleClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import networking.encryption.MapleAESOFB;
import networking.encryption.MapleCustomEncryption;

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
        length = -1;
        out.add(decryptedPacket);
    }
}
