function enter(pi) {
	if (pi.isCQFinished(100000) && pi.getPlayerStat("GM") == 0) {
		pi.dropMessage(5, "You are warped out due to an unknown force.");
		pi.warp(100000000);
	} else if (pi.getCQInfo(100000).contains("step=02")) {
		pi.openNpc(9250127);
	} else {
		pi.showInstruction("No #e#rtrespassers#k#n beyond this line!", 150, 10);
		pi.openNpc(9000035);
	}
}