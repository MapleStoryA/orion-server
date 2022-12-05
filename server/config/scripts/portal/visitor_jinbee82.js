function enter(pi) {
	if (pi.getCQInfo(100008).contains("paid")) {
		pi.warpS(502022010, 1);
	} else if (pi.getCQInfo(100008).contains("success")) {
		pi.getPlayer().dropMessage(-3, "I think I should go back to the present and give this Lump Energy to Dr.Bing");
	} else {
		pi.getPlayer().dropMessage(-3, "I'm confuse on this map, should I ask someone else first?");
	}
}