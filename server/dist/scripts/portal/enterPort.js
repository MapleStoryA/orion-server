load("nashorn:mozilla_compat.js");
importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools);


function enter(pi) {
	if(pi.isQuestActive(21301)){
		pi.warp(108010700, 1);
		pi.sendClock(10 * 60);
		var mob = MapleLifeFactory.getMonster(9001013);
		pi.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(2358, 3));
		return;
	}
    pi.warp(140020300, 1);
}