package networking.packet;

import client.MapleClient;
import handling.cashshop.CashShopOperationHandlers;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.PlayerHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.ByteArrayByteStream;
import networking.data.input.GenericSeekableLittleEndianAccessor;
import networking.data.input.InPacket;
import server.base.config.ServerConfig;
import tools.helper.HexTool;

@Slf4j
public class DefaultPacketHandler {

    public static void handlePacket(MapleClient client, PacketProcessor processor, boolean isCashShop, byte[] message) {
        try {

            var packet = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(message));
            if (packet.available() < 2) {
                return;
            }
            var header_num = packet.readShort();
            var packetHandler = processor.getHandler(header_num);
            if (ServerConfig.isDebugPacket()) {
                log.info("Received: " + header_num);
            }
            if (ServerConfig.isDebugPacket() && packetHandler != null) {
                log.info("[" + packetHandler.getClass().getSimpleName() + "]");
            }
            if (packetHandler != null && packetHandler.validateState(client)) {
                packetHandler.handlePacket(packet, client);
                return;
            }
            for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                if (recv.getValue() == header_num) {

                    if (recv.needsChecking()) {
                        if (!client.isLoggedIn()) {
                            return;
                        }
                    }
                    DefaultPacketHandler.handlePacket(recv, packet, client, isCashShop);
                    return;
                }
            }
            log.info("Received data: " + HexTool.toString(message));
            log.info("Data: " + new String(message));

        } catch (Exception e) {
            log.error("Log_Packet_Except.rtf", e);
        }
    }

    public static void handlePacket(
            final RecvPacketOpcode header, final InPacket packet, final MapleClient c, boolean isCashShop) {
        switch (header) {
            case PLAYER_LOGGED_IN:
                final int playerId = packet.readInt();
                if (isCashShop) {
                    CashShopOperationHandlers.onEnterCashShop(playerId, c);
                } else {
                    InterServerHandler.onLoggedIn(playerId, c);
                }
                break;
            case CHANGE_MAP:
                if (isCashShop) {
                    CashShopOperationHandlers.onLeaveCashShop(packet, c, c.getPlayer());
                } else {
                    PlayerHandler.changeMap(packet, c, c.getPlayer());
                }
                break;
            default:
                if (packet.available() >= 0) {
                    log.info(String.valueOf(header), "[" + header + "] " + packet);
                }
                break;
        }
    }
}
