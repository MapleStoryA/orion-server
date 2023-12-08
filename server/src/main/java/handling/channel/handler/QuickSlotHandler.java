package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class QuickSlotHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (packet.available() == 32 && chr != null) {

            final StringBuilder ret = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                ret.append(packet.readInt()).append(",");
            }
            ret.deleteCharAt(ret.length() - 1);
        }

    }

}
