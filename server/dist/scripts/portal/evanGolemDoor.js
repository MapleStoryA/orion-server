function enter(pi) {
   
	if(pi.getPlayer().isEvan()){
		if(pi.isQuestActive(22556)){
	    	pi.showQuestMsg("Now report back to chief when you reach level 35.");
	    	pi.forceCompleteQuest(22556);
	    }else if (pi.isQuestActive(22557)) {
			pi.registerEvent("Camila");
	    	return;
	    }
	}
	
    pi.warp(106010102, "out00");
    pi.playPortalSE();
}