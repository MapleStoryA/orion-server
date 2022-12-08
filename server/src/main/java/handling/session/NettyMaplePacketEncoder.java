package handling.session;

import client.MapleClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import tools.MapleAESOFB;
import tools.MapleCustomEncryption;

public class NettyMaplePacketEncoder extends MessageToByteEncoder<byte[]> {

    private MapleClient client;

    public void setClient(MapleClient client) {
        this.client = client;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] message, ByteBuf out) {
        if (client != null) {
            MapleAESOFB send_crypto = client.getSendCrypto();
            byte[] input = new byte[message.length];
            System.arraycopy(message, 0, input, 0, message.length);
            byte[] header = send_crypto.getPacketHeader(input.length);

            out.writeBytes(header);

            input = MapleCustomEncryption.encryptData(input);

            send_crypto.crypt(input);

            out.writeBytes(input);

        } else {
            out.writeBytes(message);
        }
    }
}
