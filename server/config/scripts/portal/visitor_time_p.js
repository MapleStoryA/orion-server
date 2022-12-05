function enter(pi) {
	if (pi.getCQInfo(100008).contains("success")) {
		pi.updateCQInfo(100008, "timeTravelSus");
		pi.getPlayer().dropMessage(5, "Travelling back to the present....");
		pi.warp(502010030);
	} else {
		pi.getPlayer().dropMessage(-3, "I must complete my mission first!");
	}
}