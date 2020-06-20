function init() {
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}

function setup(eim, leaderid) {
    em.setProperty("leader", "true");
    var eim = em.newInstance("Arkarium");
    var map = eim.setInstanceMap(272020200);
    map.resetFully();

    var mob = em.getMonster(8860000);
    eim.registerMonster(mob);
    map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(0, -181));

    em.setProperty("state", "1");
  
    eim.startEventTimer(1800000); //30min
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapFactory().getMap(272020200);
    player.changeMap(map, map.getPortal(0));
}

function changedMap(eim, player, mapid) {
    if (mapid != 272020200) {
	eim.unregisterPlayer(player);
	if (eim.disposeIfPlayerBelow(0, 0)) {
	    em.setProperty("state", "0");
	    em.setProperty("leader", "true");
	}
    }
}

function playerDisconnected(eim, player) {
    return 0;
}

function scheduledTimeout(eim) {
    eim.disposeIfPlayerBelow(100, 272020110);
    em.setProperty("state", "0");
    em.setProperty("leader", "true");
}

function end(eim) {
    if (eim.disposeIfPlayerBelow(100, 272020110)) {
	em.setProperty("state", "0");
	em.setProperty("leader", "true");
    }
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);

    if (eim.disposeIfPlayerBelow(0, 0)) {
	em.setProperty("state", "0");
	em.setProperty("leader", "true");
    }
}

function end(eim) {
    if (eim.disposeIfPlayerBelow(100, 272020110)) {
	em.setProperty("state", "0");
	em.setProperty("leader", "true");
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function clearPQ(eim) {
    end(eim);
}

function playerRevive(eim, player) {
    return false;
}

function allMonstersDead(eim) {}
function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}