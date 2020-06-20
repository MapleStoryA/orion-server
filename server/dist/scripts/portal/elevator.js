var em;

function enter(pi) {
    em = pi.getEventManager("elevator");

    if (em != null) {
	if (pi.getMapId() == 222020100) {
	    if (em.getProperty("isDown").equals("true")) {
		pi.playPortalSE();
		pi.warp(222020110, "sp");
	    } else {
		pi.playerMessage("The elevator is not available for the riding at this time. Please try again later.");
	    }
	} else { // 222020200
	    if (em.getProperty("isUp").equals("true")) {
		pi.playPortalSE();
		pi.warp(222020210, "sp");
	    } else {
		pi.playerMessage("The elevator is not available for the riding at this time. Please try again later.");
	    }
	}
    }
}
