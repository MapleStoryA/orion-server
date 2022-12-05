/*
    3th Job portal
 */
load("nashorn:mozilla_compat.js");
importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools);

function enter(pi) {
	var player = pi.getPlayer();
	var mapId;
	var monsterId;
	if (player.getJobValue().equals(MapleJob.FP_WIZARD)
			|| player.getJobValue().equals(MapleJob.IL_WIZARD)
			|| player.getJobValue().equals(MapleJob.CLERIC)) {
		mapId = 108010400;
		monsterId = 9001001;

	} else if (player.getJobValue().equals(MapleJob.FIGHTER)
			|| player.getJobValue().equals(MapleJob.PAGE)
			|| player.getJobValue().equals(MapleJob.SPEARMAN)) {
		mapId = 108010400;
		monsterId = 9001000;

	} else if (player.getJobValue().equals(MapleJob.ASSASSIN)
			|| player.getJobValue().equals(MapleJob.BANDIT) || player.getJobValue().equals(MapleJob.BLADE_SPECIALIST)) {
		mapId = 108010400;
		monsterId = 9001003;

	} else if (player.getJobValue().equals(MapleJob.HUNTER)
			|| player.getJobValue().equals(MapleJob.CROSSBOWMAN)) {
		mapId = 108010400;
		monsterId = 9001002;

	} else if (player.getJobValue().equals(MapleJob.BRAWLER)
			|| player.getJobValue().equals(MapleJob.GUNSLINGER)) {
		mapId = 108010400;
		monsterId = 9001008;
	}
	var fightMap = pi.getMap(mapId + 1);
	fightMap.killAllMonsters(false);
	
	var current = java.lang.System.currentTimeMillis();
	var startTime = pi.getPlayer().removeTemporaryData("3thJobTimer");
	var seconds = (current - startTime) / 1000;
	
	var mob = MapleLifeFactory.getMonster(monsterId);
	
	fightMap.spawnMonsterOnGroudBelow(mob, new java.awt.Point(200, 20));
	pi.warp(mapId + 1);
	pi.sendClock((20 * 60) - seconds);
	return true;
}