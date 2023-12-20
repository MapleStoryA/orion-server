package handling.channel.handler;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class MobHandlerUtils {

    public static final void checkShammos(final MapleCharacter chr, final MapleMonster mobto, final MapleMap map) {
        if (!mobto.isAlive() && mobto.getId() == 9300275) { // shammos
            for (MapleCharacter chrz : map.getCharactersThreadsafe()) { // check for 2022698
                if (chrz.getParty() != null && chrz.getParty().getLeader().getId() == chrz.getId()) {
                    // leader
                    if (chrz.haveItem(2022698)) {
                        MapleInventoryManipulator.removeById(
                                chrz.getClient(), MapleInventoryType.USE, 2022698, 1, false, true);
                        mobto.heal((int) mobto.getMobMaxHp(), mobto.getMobMaxMp(), true);
                        return;
                    }
                    break;
                }
            }
            map.broadcastMessage(MaplePacketCreator.serverNotice(6, "Your party has failed to protect the monster."));
            final MapleMap mapp =
                    chr.getClient().getChannelServer().getMapFactory().getMap(921120001);
            for (MapleCharacter chrz : map.getCharactersThreadsafe()) {
                chrz.changeMap(mapp, mapp.getPortal(0));
            }
        } else if (mobto.getId() == 9300275 && mobto.getEventInstance() != null) {
            mobto.getEventInstance().setProperty("HP", String.valueOf(mobto.getHp()));
        }
    }
}
