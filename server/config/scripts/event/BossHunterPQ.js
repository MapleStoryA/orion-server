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
 * Boss Hunter Party Quest
 */
load("nashorn:mozilla_compat.js");
importPackage(java.lang);

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.server.life);

var exitMap;
var instanceId;
var minPlayers = 1;
var mobs = Array(2220000, 3220000, 3220001, 5220000, 9300003, 5220002, 9300012, 5220003, 9300119, 9300039, 6220000, 6130101, 6300005, 6220001, 7220000, 7220001, 7220002, 9300139, 9300140, 8220000, 8220001, 8220002, 8150000, 8150000, 8150000, 8180000, 8180001, 8220003, 8500001, 8500001, 8500001, 9400014, 9400014, 9400014, 9400014, 9400014, 9400014, 8800000, 8800000, 8800000, 8800000, 9400300, 9400300, 9400300, 9400300, 9400300);

function init() {
	
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup() {
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(221000000); //Teh exit map :) <---------t
	var instanceName = "BossHunterPQ" + instanceId;

	var eim = em.newInstance(instanceName);
	
	var mf = eim.getMapFactory();
	
	em.getChannelServer().addInstanceId();
	
	var map = mf.getMap(221000000, false, false);//wutt
	//map.toggleDrops();	
	var portals = map.getPortals();
	map.removePortals();
	map.spawnNpc(9201082, new java.awt.Point(2272, 158));
	//Fuck this timer
	em.schedule("timeOut", 60000 * 30);
	em.schedule("broadcastClock", 1500);
	em.schedule("invasion", 2400);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (30 * 60000));
	eim.setProperty("mobLvl", "0");
	
	
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(221000000);
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	player.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Omega Sector is under attack. Protect it!!"));
	player.getClient().getSession().write(MaplePacketCreator.musicChange("Bgm14/DragonNest"));
	//THE CLOCK IS SHIT
	//player.getClient().getSession().write(MaplePacketCreator.getClock(1800));
	//y=2000
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	if (eim.isLeader(player)) { //check for party leader
		//boot whole party and end
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim, party.get(i));
		}
		eim.dispose();
	}
	else { //boot dead player
		// If only 5 players are left, uncompletable:
		var party = eim.getPlayers();
		if (party.size() <= minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		}
		else
			playerExit(eim, player);
	}
}

function playerDisconnected(eim, player) {
	if (eim.isLeader(player)) { //check for party leader
		//PWN THE PARTY (KICK OUT)
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			}			
			else {
				playerExit(eim, party.get(i));
			}
		}
		eim.dispose();
	}
	else { //KICK THE D/CED CUNT
		// If only 5 players are left, uncompletable:
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		}
		else
			playerExit(eim, player);
	}
}

function leftParty(eim, player) {			
	// If only 5 players are left, uncompletable:
	var party = eim.getPlayers();
	if (party.size() <= minPlayers) {
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim,party.get(i));
		}
		eim.dispose();
	}
	else
		playerExit(eim, player);
}

function disbandParty(eim) {
	//boot whole party and end
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
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

function playerClocks(eim, player) {
  if (player.getMap().hasTimer() == false){
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	//player.getMap().setTimer(true);
	}
}

function playerTimer(eim, player) {
	if (player.getMap().hasTimer() == false) {
		player.getMap().setTimer(true);
	}
}

function broadcastClock(eim, player) {
	//var party = eim.getPlayers();
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerClocks(eim, pIter.next());
			}
		}
		//em.schedule("broadcastClock", 1600);
	}
	// for (var kkl = 0; kkl < party.size(); kkl++) {
		// party.get(kkl).getMap().setTimer(true);
	// }
	var iterr = em.getInstances().iterator();
	while (iterr.hasNext()) {
		var eim = iterr.next();
		if (eim.getPlayerCount() > 0) {
			var pIterr = eim.getPlayers().iterator();
			while (pIterr.hasNext()) {
				//playerClocks(eim, pIter.next());
				playerTimer(eim, pIterr.next());
			}
		}
		//em.schedule("broadcastClock", 1600);
	}
	em.schedule("broadcastClock", 1600);
}

function invasion(eim, player) {
	var iter = em.getInstances().iterator();
	var times = Math.round(Math.random() * 20);
	while (iter.hasNext()) {
		var eim = iter.next();
		var mf = eim.getMapFactory();
		var map = mf.getMap(221000000, false, false);
		var mobId = Integer.parseInt(eim.getProperty("mobLvl"));
		if (mobId > mobs.length) {
			mobId = mobs.length - 1;
		}
		var mobSpawn = mobs[mobId];
		if (mobSpawn < 2220000) {
			mobSpawn = 9400014;
		}
		if (eim.getPlayerCount() > 0) {
			if (map.countMonster(eim.getPlayers().get(0)) < 30) {
				for (var i = 0; i < times && map.countMonster(eim.getPlayers().get(0)) < 30; i++) {
						var mob = server.life.MapleLifeFactory.getMonster(mobSpawn);
						var overrideStats = new server.life.MapleMonsterStats();
						overrideStats.setHp(mob.getMaxHp() * 3);
						overrideStats.setExp(mob.getExp() / 2);
						overrideStats.setMp(mob.getMaxMp());
						overrideStats.setRevives(null);
						overrideStats.setFirstAttack(true);
						mob.setOverrideStats(overrideStats);
						mob.setHp(overrideStats.getHp());
						mob.setControllerHasAggro(true);
						mob.setControllerKnowsAboutAggro(true);
						//eim.registerMonster(mob);

						map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(randX(), 100));
				}
				if (Math.random() > 0.4 && mobId < mobs.length) {
					eim.setProperty("mobLvl", mobId + 1);
				}
			}
		}
	}
	em.schedule("invasion", 15000);
}

function randX() {
	var k = 1;
	if (Math.random() > 0.5) {
		k = -1;
	}
	var w = Math.round(Math.random() * 1000);
	return 2299 + k * w;
}