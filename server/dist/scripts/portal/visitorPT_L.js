
load("nashorn:mozilla_compat.js");importPackage(Packages.tools);

function enter(pi) {
	if (pi.getCQInfo(100008).contains("readyStart")) {
		var eim = pi.getEventInstance();
		if (eim != null && eim.getEventManager() != null) {
			var em = eim.getEventManager();
			var stage = parseInt(eim.getProperty("stage"));
			if (stage == 5) {
				if (pi.haveItem(4001459)) {
					pi.updateCQInfo(100008, "success");
					pi.warpS(502029000, 0);
					pi.getPlayer().cancelMorphs(true);					
					pi.getPlayer().getClient().getSession().write(MaplePacketCreator.showEffect("killing/clear"));
					pi.getPlayer().getClient().getSession().write(MaplePacketCreator.playSound("Party1/Clear"));
				} else {
					pi.getPlayer().dropMessage(-3, "I can't get out yet till I obtain the Lump Energy..");
				}
			} else if (stage == 6) {
				pi.getPlayer().dropMessage(5, "The mothership's door already exploded. There's no way to run.");
			} else {
				pi.getPlayer().dropMessage(5, "Please eliminate all the monsters before you proceed to the next stage.");
			}
		}
	}
}