/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling.channel.handler;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class MobHandlerUtils {


  public static final void checkShammos(final MapleCharacter chr, final MapleMonster mobto, final MapleMap map) {
    if (!mobto.isAlive() && mobto.getId() == 9300275) { //shammos
      for (MapleCharacter chrz : map.getCharactersThreadsafe()) { //check for 2022698
        if (chrz.getParty() != null && chrz.getParty().getLeader().getId() == chrz.getId()) {
          //leader
          if (chrz.haveItem(2022698)) {
            MapleInventoryManipulator.removeById(chrz.getClient(), MapleInventoryType.USE, 2022698, 1, false, true);
            mobto.heal((int) mobto.getMobMaxHp(), mobto.getMobMaxMp(), true);
            return;
          }
          break;
        }
      }
      map.broadcastMessage(MaplePacketCreator.serverNotice(6, "Your party has failed to protect the monster."));
      final MapleMap mapp = chr.getClient().getChannelServer().getMapFactory().getMap(921120001);
      for (MapleCharacter chrz : map.getCharactersThreadsafe()) {
        chrz.changeMap(mapp, mapp.getPortal(0));
      }
    } else if (mobto.getId() == 9300275 && mobto.getEventInstance() != null) {
      mobto.getEventInstance().setProperty("HP", String.valueOf(mobto.getHp()));
    }
  }


}
