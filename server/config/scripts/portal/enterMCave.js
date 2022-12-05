function enter(pi) {
	if(pi.isQuestActive(21302)){ //Aran 3th job
		pi.forceStartQuest(21303);
		pi.warp(108010701, 0);
		pi.playPortalSE();
		pi.sendClock(20 * 60);
		
		return;
	}
    if (pi.isQuestActive(21201)) { //aran first job
		pi.forceCompleteQuest(21201);
		pi.forceStartQuest(21202);
		
    }
    
    pi.playerMessage(5, "You recovered your memories!");
    pi.warp(108000700,0);
 //what does this even do
}