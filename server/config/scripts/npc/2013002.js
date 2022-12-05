function action(mode, type, selection) {
	var em = cm.getEventManager("OrbisPQ");
	if (em == null) {
		cm.dispose();
		return;
	}
	for (var i = 4001044; i < 4001064; i++) {
		cm.removeAll(i); //holy
	}
	switch(cm.getMapId()) {
		case 920010100: //center stage, minerva warps to bonus
			//em.setProperty("done", "1");
			cm.givePartyExp(16099);
			cm.warpParty(920011100);
			break;
		default:
			if (!cm.canHold(4001158,1)) {
				cm.sendOk("Please make one ETC room.");
				cm.dispose();
				return;
			}
			cm.gainItem(4001158,1);
			cm.getPlayer().endPartyQuest(1203);//might be a bad implentation.. incase they dc or something
			cm.gainNX(3000);
			cm.warp(200080101);
			break;
	}
	cm.dispose();
}