var battlemap = 502030004;
var returnmap = 502029000; // Spaceship Crash site

function init() {}

function setup(charid) {
	var instanceName = "LumpEnergy" + charid;
	var eim = em.newInstance(instanceName);
	var map = eim.createInstanceMapS(battlemap);
	map.toggleDrops(); // blocks all drops
	map.spawnNpc(9250137, new java.awt.Point(405, 111));
	eim.setProperty("stage", 0);
	beginQuest(eim);
	return eim;
}

function beginQuest(eim) {
	if (eim != null) {
		eim.startEventTimer(7000);
	}
}

var monster1X = new Array(-310, 159, 41, -271, -400);
var monster1Y = new Array(-156, -156, 111, 111, 252);
var monster2X = new Array(-52, 123, -145, 372);
var monster2Y = new Array(-156, 111, 111, 250);

function monsterSpawn(eim) { // Custom function
	var map = eim.getMapInstance(0);
	var stage = parseInt(eim.getProperty("stage"));
	if (stage != 3) {
		eim.broadcastPacket(MaplePacketCreator.showEffect("evan/monster"));
		eim.broadcastPacket(MaplePacketCreator.playSound("Dojang/start"));
	}
	switch (stage) {
	case 0:
		for (var i = 0; i < monster1X.length; i++) {
			var mob = em.getMonster(4230116);
			eim.registerMonster(mob); // gray (zeta)
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(monster1X[i], monster1Y[i]));
		}
		for (var i = 0; i < monster2X.length; i++) {
			var mob = em.getMonster(4230116);
			eim.registerMonster(mob);
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(monster2X[i], monster2Y[i]));
		}
		break;
	case 1:
		eim.dropPlayerMsg(-3, "WHAT? Monsters again?..");
		for (var i = 0; i < monster1X.length; i++) {
			var mob = em.getMonster(4230120);
			eim.registerMonster(mob);
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(monster1X[i], monster1Y[i]));
		}
		for (var i = 0; i < monster2X.length; i++) {
			var mob = em.getMonster(4230118);
			eim.registerMonster(mob);
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(monster2X[i], monster2Y[i]));
		}
		break;
	case 2:
		eim.dropPlayerMsg(-3, "Woah! These monsters seemed to be strong. I must be careful...");
		for (var i = 0; i < monster1X.length; i++) {
			var mob = em.getMonster(9420128);
			eim.registerMonster(mob);
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(monster1X[i], monster1Y[i]));
		}
		for (var i = 0; i < monster2X.length; i++) {
			var mob = em.getMonster(9420129);
			eim.registerMonster(mob);
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(monster2X[i], monster2Y[i]));
		}
		break;
	case 3:
		eim.dropPlayerMsg(-3, "Ahhh! Finally I see the Cystanol Synthesis!");
		eim.dropPlayerMsg(5, "[Bing Force] I will be waiting for you at the entrance!");
		map.spawnNpc(9250139, new java.awt.Point(-23, 111)); // Crystanol Synthesis
		break;
	}
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(0);
	player.changeMap(map, map.getPortal(0));
}

function changedMap(eim, player, mapid) {
	if (mapid != battlemap) {
		eim.unregisterPlayer(player);
		player.cancelMorphs(true);
		eim.disposeIfPlayerBelow(0, 0);
	}
}

function scheduledTimeout(eim) {
	var num = parseInt(eim.getProperty("stage"));
	if (num < 4) { // 3 stage nia..0, 1, 2, 3
		monsterSpawn(eim);
		eim.setProperty("stage", num + 1);
	} else if (num == 5) {
		eim.broadcastPacket(MaplePacketCreator.showEffect("killing/fail"));
		eim.broadcastPacket(MaplePacketCreator.playSound("Party1/Failed"));
		for (var i = 0; i < bombx.length; i++) {
			eim.broadcastPacket(MaplePacketCreator.showMonsterBombEffect(bombx[i], bomby[i], 20));
		}
		eim.restartEventTimer(5000);
		eim.dropPlayerMsg(6, "You've failed the quest. You will be warped out in 5 seconds.");
		eim.setProperty("stage", num + 1);
	} else {
		var iter = eim.getPlayers().iterator();
		while (iter.hasNext()) {
			var player = iter.next();
			player.removeAll(4001459);
		}
		player.cancelMorphs(true);
		for (var i = 0; i < bombx.length; i++) {
			eim.broadcastPacket(MaplePacketCreator.showMonsterBombEffect(bombx[i], bomby[i], 20));
		}
		eim.disposeIfPlayerBelow(100, returnmap);
	}
}

var bombx = new Array(-371, 90, 249, -369, 211);
var bomby = new Array(-156, -156, 328, 252, 111);

function clearPQ(eim) {
	if (parseInt(eim.getProperty("stage")) == 4) {
		var num = parseInt(eim.getProperty("stage"));
		eim.setProperty("stage", num + 1);
		eim.restartEventTimer(30000);
		eim.dropPlayerMsg(5, "The mothership is going to explode in 30 seconds. Please hurry to the exit!");
		eim.dropPlayerMsg(-3, "Hmm..My sense tell me that the left portal is the exit!");
		for (var i = 0; i < bombx.length; i++) {
			eim.broadcastPacket(MaplePacketCreator.showMonsterBombEffect(bombx[i], bomby[i], 20));
		}
	}
}

function allMonstersDead(eim) {
	var mobnum = parseInt(eim.getProperty("stage"));
	if (mobnum < 4) {
		eim.restartEventTimer(3000);
		if (mobnum == 3) {
			eim.dropPlayerMsg(6, "[Alien] Nooooo...You can't stop me! I will come back again!");
		}
	}
}

function playerDead(eim, player) {
	var iter = eim.getPlayers().iterator();
	while (iter.hasNext()) {
		var player = iter.next();
		player.removeAll(4001459);
	}
	player.cancelMorphs(true);
	eim.disposeIfPlayerBelow(100, returnmap);
}

function playerRevive(eim, player) {
	return false;
}

function playerDisconnected(eim, player) {
	return 0;
}

function monsterValue(eim, mobid) {
	return 0;
}

function leftParty(eim, player) {}

function disbandParty(eim, player) {}

function removePlayer(eim, player) {
	eim.dispose();
}

function cancelSchedule() {}