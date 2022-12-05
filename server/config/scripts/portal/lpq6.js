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
load("nashorn:mozilla_compat.js");
importPackage(Packages.server.maps);
importPackage(Packages.net.channel);
importPackage(Packages.tools);

/*
LudiPQ - 1 - 2 Portal
@author Jvlaple
*/

function enter(pi) {
	var nextMap = 922010600;
	var eim = pi.getPlayer().getEventInstance()
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("st00");
	// only let people through if the eim is ready
	var avail = eim.getProperty("5stageclear");
	if (avail == null) {
		// can't go thru eh?
		pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Alguns selos e bloquear esta porta."));
		return false;	}
	else {
		pi.getPlayer().changeMap(target, targetPortal);
		return true;
	}
}