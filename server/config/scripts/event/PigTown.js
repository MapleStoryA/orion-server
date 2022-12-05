/* 
 * This file is part of the OdinMS Maple Story Server
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
	
	THIS  FILE WAS MADE BY JVLAPLE. REMOVING THIS NOTICE MEANS YOU CAN'T USE THIS SCRIPT OR ANY OTHER SCRIPT PROVIDED BY JVLAPLE.
 */

/*
 * @Author Jvlaple
 * 
 * Pig town - part of HPQ
 */

load("nashorn:mozilla_compat.js");
importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);

var exitMap;
var instanceId;
var minPlayers = 1;

function init() {
    	exitMap = em.getChannelServer().getMapFactory().getMap(910010400); 
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(eim) {
        var eim = em.newInstance("PigTown");
	var mf = eim.getMapFactory();
        var eventTime = 3 * 60000;
	var map = mf.getMap(910010200);//wutt
        em.schedule("timeOut", eim, eventTime); 
        eim.startEventTimer(eventTime);
	
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(910010200);
	player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	playerExit(eim, player);
	if (eim.getPlayers().size() < 1)
		eim.dispose(); //Fixed PigTown
}

function playerDisconnected(eim, player) {
	playerExit(eim, player);
	if (eim.getPlayers().size() < 1)
		eim.dispose(); //Fixed PigTown
}

function leftParty(eim, player) {			
	playerExit(eim, player);
	if (eim.getPlayers().size() < 1)
		eim.dispose(); //Fixed PigTown
}

function disbandParty(eim) {
	playerExit(eim, player);
	if (eim.getPlayers().size() < 1)
		eim.dispose(); //Fixed PigTown
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
}

//Those offline cuntts
function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	//HTPQ does nothing special with winners
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function allMonstersDead(eim) {
        //Open Portal? o.O
}

function cancelSchedule() {
}

function timeOut() {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerExit(eim, pIter.next());
			}
		}
		eim.dispose();
	}
}
