function enter(pi) {
	var temp = pi.getPlayer().getTemporaryData("talked");
	if(temp == null){
		pi.getPlayer().addTemporaryData("talked", "1");
		pi.forceStartQuest(22012);
		pi.openNpc(1013001);
	}
	return true;
}