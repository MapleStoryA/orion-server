function enter(pi) {
    if (pi.getQuestStatus(2369) == 1 && !pi.haveItem(4032617)) { //too lazy to do the map shit
    	//pi.gainItem(4032617,1);
    	pi.warp(910350100);
    	pi.sendClock(10 * 60);
    }
}