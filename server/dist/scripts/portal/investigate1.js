function enter(pi) {
	if(pi.isQuestFinished(2314) && pi.isQuestFinished(2319)){
		return true;
	}
	if(pi.isQuestActive(2314) || pi.isQuestFinished(2319)){
		pi.openNpc(1300014);
		return true;
	}
	return false;
}