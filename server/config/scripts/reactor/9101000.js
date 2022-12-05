/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @author BubblesDev
 * @purpose
 * [x]Full Moon,
 * [x]Summons Moon Bunny,
 * [x]shows animation,
 * [x]makes stirges and stuff appear
 */
importPackage(Packages.tools);
importPackage(Packages.server);
importPackage(Packages.server.life);
importPackage(Packages.server.maps);


function act() {
////  //  rm.getClient().getPlayer().getMap().killAllMonsters();
//    var eim = rm.getPlayer().getEventInstance();
//    var tehMap = eim.getMapInstance(910010000);
//    var bunny = MapleLifeFactory.getMonster(9300061);
////    rm.spawnMonster(9300061, 1, -187, -186);
//    tehMap.spawnMonsterOnGroundBelow(bunny, new java.awt.Point(-187, -186));
//    eim.registerMonster(bunny);
//    eim.setProperty("shouldDrop", "true");
//    rm.getPlayer().getMap().setMonsterRate(1);
    
    
//    rm.getPlayer().getMap().startMapEffect("Protect the Moon Bunny that's pounding the mill, and gather up 10 Moon Bunny's Rice Cakes!", 5120002, 7000);
//    rm.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "Protect the Moon Bunny!!!")); // not the real packet, but ok
//    //rm.getClient().getPlayer().getMap().broadcastMessage(MaplePacketCreator.showHPQMoon());
//    // GMS does this, IMO really stupid (actually it makes all monsters invisible
//    // so the monsters float and then they kill and spawn a new batch. GMS also
//    // spawns these in reverse order, but the time difference is negligible.
//    rm.getPlayer().getClient().getChannelServer().getMapFactory().disposeMap(910010000);
//    rm.getPlayer().getMap().setMonsterRate(1);
////    rm.spawnMonster(9300083, -901, -558);
////    rm.spawnMonster(9300082, -888, -655);
////    rm.spawnMonster(9300082, 609, -442);
////    rm.spawnMonster(9300081, -653, -836);
////    rm.spawnMonster(9300081, -958, -242);
////    rm.spawnMonster(9300063, 587, -263);
////    rm.spawnMonster(9300063, -947, -387);
////    rm.spawnMonster(9300062, 494, -755);
////    rm.spawnMonster(9300064, 177, -836);
////    rm.spawnMonster(9300064, 562, -597);
}