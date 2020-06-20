/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
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

/* Cloto
 * 
 * Hidden Street : 1st Accompaniment <1st Stage> (103000800)
 ~ 2nd, 3rd, 4th (801, 802, 803)
 * Hidden Street : 1st Accompaniment <Last Stage> (103000804)
 * 
 * Kerning City Party Quest NPC 
*/
importPackage(Packages.tools);
importPackage(Packages.server.life);
importPackage(java.awt);

var status;
			

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0){
	//var packetef = MaplePacketCreator.showEffect("quest/party/clear");
	//var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
	//var packetglow = MaplePacketCreator.environmentChange("gate",2);
	//var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(MaplePacketCreator.showEffect("quest/party/clear"));
	//map.broadcastMessage(packetsnd);
	cm.sendOk("Fuck u");
	cm.dispose();
}
}
}