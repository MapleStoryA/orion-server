package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.packet.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class AutoAggroHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        int monsteroid = packet.readInt();
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null || chr.isHidden()) { // no evidence :)
            return;
        }
        final MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);

        if (monster != null && chr.getPosition().distanceSq(monster.getPosition()) < 200000) {
            if (monster.getController() != null) {
                if (chr.getMap().getCharacterById(monster.getController().getId()) == null) {
                    monster.switchController(chr, true);
                } else {
                    monster.switchController(monster.getController(), true);
                }
            } else {
                monster.switchController(chr, true);
            }
        }
    }
}
