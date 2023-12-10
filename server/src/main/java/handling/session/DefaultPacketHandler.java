package handling.session;

import client.MapleClient;
import handling.PacketProcessor;
import handling.RecvPacketOpcode;
import handling.cashshop.CashShopOperationHandlers;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.PlayerHandler;
import lombok.extern.slf4j.Slf4j;
import server.config.ServerConfig;
import tools.HexTool;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.InPacket;

@Slf4j
public class DefaultPacketHandler {

    public static void handlePacket(MapleClient client, PacketProcessor processor, boolean isCashShop, byte[] message) {
        try {

            var slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(message));
            if (slea.available() < 2) {
                return;
            }
            var header_num = slea.readShort();
            var packetHandler = processor.getHandler(header_num);
            if (ServerConfig.isDebugEnabled()) {
                log.info("Received: " + header_num);
            }
            if (ServerConfig.isDebugEnabled() && packetHandler != null) {
                log.info("[" + packetHandler.getClass().getSimpleName() + "]");
            }
            if (packetHandler != null && packetHandler.validateState(client)) {
                // log.warn("Handling packet: {}", packetHandler.getClass().getSimpleName());
                packetHandler.handlePacket(slea, client);
                return;
            }
            for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                if (recv.getValue() == header_num) {

                    if (recv.needsChecking()) {
                        if (!client.isLoggedIn()) {
                            return;
                        }
                    }
                    DefaultPacketHandler.handlePacket(recv, slea, client, isCashShop);
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
            final RecvPacketOpcode header, final InPacket slea, final MapleClient c, boolean isCashShop) {
        switch (header) {
            case PLAYER_LOGGED_IN:
                final int playerId = slea.readInt();
                if (isCashShop) {
                    CashShopOperationHandlers.onEnterCashShop(playerId, c);
                } else {
                    InterServerHandler.onLoggedIn(playerId, c);
                }
                break;
            case CHANGE_MAP:
                if (isCashShop) {
                    CashShopOperationHandlers.onLeaveCashShop(slea, c, c.getPlayer());
                } else {
                    PlayerHandler.changeMap(slea, c, c.getPlayer());
                }
                break;
            default:
                if (slea.available() >= 0) {
                    log.info(String.valueOf(header), "[" + header + "] " + slea);
                }
                break;
        }
    }
}
