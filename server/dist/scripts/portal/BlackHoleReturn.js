function enter(pi) {
    if (!pi.haveItem(4031752, 20)) {
		pi.showInstruction("You need #e#b[20 Blinking Dingbats]#k to enter the black hole.", 350, 5);
	} else {
		pi.getPlayer().cancelMorphs(true);
		pi.updateCQInfo(100007, "done");
		pi.warp(502010030);
	}
}