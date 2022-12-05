/*
 * @Author Jvlaple
 * @Re-Coder JavaScriptz
 * Ariant Coliseum (2)
 * Adicionado funcoes/Aperfeicoamento
 * 2014-21-09
 */

load("nashorn:mozilla_compat.js");
importPackage(java.lang);

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server);
importPackage(Packages.server.maps);
importPackage(Packages.tools);

var exitMap;
var instanceId;
var minPlayers = 2;

function init() {
	instanceId = 1;
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup() {
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(980010020);
	doneMap = em.getChannelServer().getMapFactory().getMap(980010010);
	var instanceName = "AriantPQ2" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	em.getChannelServer().addInstanceId();
	var map = mf.getMap(980010201);
        em.schedule("timeOut", 10 * 60000 + 10000); 
        em.schedule("scoreBoard", 10 * 60000); 
	em.schedule("broadcastClock", 1500);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (10 * 60000));
	var tehwat = Math.random() * 3;
	if (tehwat > 1) {
		eim.setProperty("theWay", "darkness");
	} else {
		eim.setProperty("theWay", "light");
	}
	
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(980010201);
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	if (eim.isSquadLeader(player, MapleSquadType.ARIANT2)) { 
		var squad = player.getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT2);
		player.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.ARIANT2);
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
	else { 
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

function playerDisconnected(eim, player) {
	if (eim.isSquadLeader(player, MapleSquadType.ARIANT2)) { 
		var squad = player.getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT2);
		player.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.ARIANT2);
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
	else { 
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
}

function disbandParty(eim) {
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerDone(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(doneMap, doneMap.getPortal(0));
	var squad = player.getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT2);
	if (eim.getProperty("disbanded") == null) {
	player.getChannelServer().removeMapleSquad(squad, MapleSquadType.ARIANT2);
	eim.setProperty("disbanded", "done");	
	}
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function allMonstersDead(eim) {
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
				playerDone(eim, pIter.next());
			}
		}
		eim.dispose();
	}
}

function scoreBoard(eim, player) {
	 var iter = em.getInstances().iterator();
	 var shouldScheduleThis = true;
	 while (iter.hasNext()) {
		 var eim = iter.next();
			 if (eim.getPlayerCount() > 0) {
				 var pIter = eim.getPlayers().iterator();
                                 var map = eim.getMapInstance(980010201);
	                         map.broadcastMessage(MaplePacketCreator.showAriantScoreBoard());
				 //tehMap.killAllMonsters(false);
				 shouldScheduleThis = false;
		 }
	 }
	 if (shouldScheduleThis)
	 em.schedule("scoreBoard", 100000);
 }

function playerClocks(eim, player) {
  if (player.getMap().hasTimer() == false){
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	}
}

function playerTimer(eim, player) {
	if (player.getMap().hasTimer() == false) {
		player.getMap().setTimer(true);
	}
}

function broadcastClock(eim, player) {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerClocks(eim, pIter.next());
			}
		}
	}
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
	}
	em.schedule("broadcastClock", 1600);
}
