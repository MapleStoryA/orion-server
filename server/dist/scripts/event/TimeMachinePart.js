function init() {
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup(charid) {
	var eim = em.newInstance("TimeMachinePart" + charid);

    var map = eim.setInstanceMap(502040100);
    map.resetFully();
    map.respawn(true);
    eim.startEventTimer(300000);

    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapFactory().getMap(502040100);
    player.changeMap(map, map.getPortal(0));
	player.dropMessage(6, "You must collect 20 Blinking Dingbat in 5 minutes!");
}

function changedMap(eim, player, mapid) {
	if (mapid != 502040100) {
		eim.unregisterPlayer(player);
		eim.disposeIfPlayerBelow(0, 0);
	}
}

function playerRevive(eim, player) {
	return false;
}

function playerDisconnected(eim, player) {
    return 0;
}

function scheduledTimeout(eim) {
	eim.disposeIfPlayerBelow(100, 502010030);
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
	eim.disposeIfPlayerBelow(100, 502010030);
}

function playerDead(eim, player) {
}

function leftParty(eim, player) {			
}

function disbandParty(eim) {
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}