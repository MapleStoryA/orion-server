package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;
import server.MapleItemInformationProvider;

@lombok.extern.slf4j.Slf4j
public class CancelItemEffectHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int id = packet.readInt();
        if (id == 2211000 || id == 2212000) {
            chr.setMorphId((byte) 0);
        }
        chr.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-id), false, -1);
    }
}
