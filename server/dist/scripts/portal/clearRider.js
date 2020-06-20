function enter(pi) {
	if(pi.getPlayer().getTemporaryData("clearRider") == null || pi.getPlayer().getTemporaryData("clearRider").isEmpty()){
		pi.playerMessage(5, "The header's field has been cleared.");
		if(pi.isQuestActive(21610)){
			pi.forceCompleteQuest(21610);
		}
	}
	pi.getPlayer().addTemporaryData("clearRider", "true");
}