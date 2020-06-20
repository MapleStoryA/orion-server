function enter(pi) {
    var em = pi.getEventManager("HorntailBattle");

    if (em != null) {
	var map = pi.getMapId();
    
	if (map == 240060000) {
	    if (em.getProperty("state").equals("2")) {
		em.warpAllPlayer(240060000, 240060100);
	    } else {
		pi.playerMessage("The portal is blocked.");
	    }
	} else if (map == 240060100) {
	    if (em.getProperty("state").equals("3")) {
		em.warpAllPlayer(240060100, 240060200);
	    } else {
		pi.playerMessage("The portal is blocked.");
	    }
	}
    }
}