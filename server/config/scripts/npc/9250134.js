
var status = 0;
var sel;

function start() {
	if (cm.getPlayer().getMapId() == 502010040 && cm.isCQActive(100004)) {
		status = -1;
	} else if (cm.getPlayer().getMapId() == 502010200) {
		status = 1;
	} else if (cm.isCQFinished(100005) && cm.getCustomQuestStatus(100006) == 0) { // Completed quest 100004
		status = 3;
	} else if (cm.isCQActive(100006)) { // started occupation quest, time to explain havent select
		status = 4;
	} else if (cm.isCQFinished(100006)) { // all done here
		status = 5;
	} else {
		status = 6;
	}
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
		} else {
            status--;
		}
        if (status == 0) {
			cm.sendNext("Dear Hero! The Aliens are approaching near and we need to prepare to fight back as soon as possible. Did you saw the #r#eTube Machine#k#n behind me? Yes, the machine will give you #d#eSpecial Power#k#n to fight againts the Aliens. But we are still lacking some material to make it function. Could you help us to find the Material at the Deep Sea?\r\nThe Material look like this : #i4032708#");
		} else if (status == 1) {
			if (cm.completeCQ(100004)) {
				cm.gainExp(100);
			}
			cm.startCQ(100005);
			cm.warp(502010200);
			cm.dispose();	
		} else if (status == 2) { // Second part
			if (!cm.haveItem(4032708, 10)) {
				cm.sendOk("I will need you to collect #r#e10x #i4032708##k#n for me.\r\nSince you has been protected by the Powerful Suit, hence you will not receive too much damage from the Alien.\r\n\r\nThe aliens here posses a #i4031757# which will react with the Broken Tube here.\r\n\r\n#d#ePlease drop the #t4031757# on the Broken Tube and collect the #t4032708# for me.");
				cm.dispose();
			} else {
				if (cm.completeCQ(100005)) {
					cm.gainExp(100); // fix
				}
				cm.sendNext("Thank you so much for this! Now let's go back to the Lab and get ready to cast the #r#eSpecial Power#k#n.");
			}
		} else if (status == 3) {
			cm.removeAll(4032708); //just incase
			cm.removeAll(4031757); //antellion relic
			cm.getPlayer().cancelMorphs(true);
			cm.warp(502010040);
			cm.dispose();
		} else if (status == 4) { // get ready to pick occupation
			cm.startCQ(100006);
			cm.sendNext("Let me explain briefly about the #r#eSpecial Power#k#n to you. The #r#eSpecial Power#k#n is known as #d#eOccupation#k#n in other word, it will grant you a specific #r#eSpecial Power#k#n.\r\nDifferent Occupation give different #r#eSpecial Power#k#n, for instance, Vortex will act like a black hole, Ninja will give you clone, and so on.\r\nOh well, let me fix my Power Tubes first. Talk to me again later.");		
			cm.dispose();
		} else if (status == 5) {// explain.
			cm.sendNext("My work here is done! The Power tube is now completely fixed. Thank for your effort on helping me.\r\nNow you may talk to my specific helper to pick your #d#eOccupation#k#e.");
			cm.dispose();
		} else if (status == 6) {// picked occ and talk back to me again
			cm.sendNext("Great! I see you has been granted a powerful skill!\r\nDr.Bing is looking for you in his room, you may visit him now.");
			cm.dispose();
		} else { // shouldn't come here
			cm.sendNext("What are you doing here? Get out from my Lab now!");
			cm.dispose();
		}
	}
}