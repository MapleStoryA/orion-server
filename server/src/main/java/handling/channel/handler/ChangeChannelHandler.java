package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class ChangeChannelHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            log.info("Cannot change channel of null character");
            c.getSession().close();
        }
        if (!chr.isAlive()
                || chr.getEventInstance() != null
                || chr.getMap() == null
                || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.changeChannel(packet.readByte() + 1);
    }
}
