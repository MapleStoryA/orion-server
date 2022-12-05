function enter(pi) {
	var map = pi.getPlayer().getMap();
	var reactor = map.getReactorByName("gate04");
	var state = reactor.getState();
	if (state >= 4) {
		pi.warp(670010600, 10);
		return true;
	} else {
		pi.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "O portal esta fechado."));
		return false;
	}
}