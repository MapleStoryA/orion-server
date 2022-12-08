package handling.session.netty;

import client.MapleClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import tools.MapleAESOFB;
import tools.MapleCustomEncryption;

@Slf4j
public class NettyMaplePacketEncoder extends MessageToByteEncoder<byte[]> {

    private MapleClient client;

    public void setClient(MapleClient client) {
        this.client = client;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] message, ByteBuf out) {

        if (client != null) {
            final MapleAESOFB send_crypto = client.getSendCrypto();
            final byte[] inputInitialPacket = message;
            final byte[] unencrypted = new byte[inputInitialPacket.length];
            System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length);
            final byte[] ret = new byte[unencrypted.length + 4];
            final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
            MapleCustomEncryption.encryptData(unencrypted);
            send_crypto.crypt(unencrypted);
            System.arraycopy(header, 0, ret, 0, 4);
            System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
            out.writeBytes(ret);
        } else {
            // no client object created yet, send unencrypted (hello)
            out.writeBytes(message);
        }

    }
}
