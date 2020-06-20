var quest = 21753;


function enter(pi) {
	
	if(pi.getQuestStatus(quest) == 2 && pi.getPlayer().isAran()){~
		pi.forceCompleteQuest(21754);
		pi.warpAndSpawnMonster(910050000, 9001009, new java.awt.Point(-225, 181));
		return;
	}
	
	pi.playPortalSE();
	pi.warp(100000201, 1);
	return true;
}