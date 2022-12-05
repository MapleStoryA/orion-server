/**
 * OS4 Shuttle
 * Warps you around in the Base
 */
var status = 0;

function start() {
	if (cm.getPlayer().getMapId() == 970030020) { // first map
		status = -1;
	} else if (cm.getPlayer().getMapId() == 502010000) { // OSSS Base
		if (cm.getCustomQuestStatus(100001) == 1) { //Ready to go to miner's map
			status = 2;
		} else {
			status = 1;
		}
	} else if (cm.getPlayer().getMapId() == 502010300) {
		status = 4;
	} else if (cm.getPlayer().getMapId() == 502010600) {
		status = 6;
	} else {
		status = 8;
	}
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
		} else {
			if (status == 0 || status == 3 || status == 5) {
				cm.sendNext("If you're ready to go to the " + ((status == 0 || status == 5) ? "OSSS Secret Base" : "Fossil Mineral Mine") + ", please come to me again.");
				cm.dispose();
				return;
			}
            status--;
		}
		if (status == 0) { // 1st map
			if (cm.getCQInfo(100000).contains("step=02")) {
				cm.sendYesNo("Are you ready to go to the OSSS Secret Base?");
			} else {
				cm.sendOk("You are not in any mission yet.");
				cm.dispose();
			}
		} else if (status == 1) {
			cm.updateCQInfo(100000, "");
			if (cm.completeCQ(100000)) {
				cm.gainExp(15);
			}
			cm.MovieClipIntroUI(true);
			cm.showWZEffect("Effect/DirectionVisitor.img/visitor/Shuttle");
			cm.dispose();
		} else if (status == 2) { // base map default msg
			cm.sendOk("#b(You tried to start the shuttle but it doesn't seem to start.)#k\r\nBiii..Biii..");
			cm.dispose();
		} else if (status == 3) {
			cm.sendYesNo("Hi there! Are you ready to board the shuttle and go to the Fossil Mineral Mine where Doctor Bing is located at?");
		} else if (status == 4) {
			cm.updateCQInfo(100001, "step=02"); // will clear the quest when talk to doctor bing.
			cm.warp(502010300, 0); // will auto cast buff upon entering map. Can't exit till quest is done.
			//cm.dispose();
		} else if (status == 5) {
			if (cm.isCQFinished(100002) && cm.isCQFinished(100003)) {
				cm.sendYesNo("Are you ready to go back to the OSSS Secret Base?");
			} else {
				cm.sendOk("#b(You tried to start the shuttle but it doesn't seem to start.)#k\r\nBiii..Biii..");
				cm.dispose();
			}
		} else if (status == 6) {
			cm.removeAll(4001458);
			cm.warp(502010000, 0); // will auto cast buff upon entering map. Can't exit till quest is done.
			cm.getPlayer().cancelMorphs(true);	
			//cm.dispose();
		} else if (status == 7) {
			if (cm.getCQInfo(100009).contains("gave")) { // casted by osss boss
				cm.sendNext("Would you like to go back to the MapleWorld?"); //send accept decline here..
			} else { // if didn't follow storyline, will end up here..=.=
				cm.sendNext("ERROR");
				cm.dispose();
			}
		} else if (status == 8) {
			cm.warp(100000000);
			//cm.gainitemetcetc
			// broadcast msg, successfully finished storyline
			// give many things, etc etc etc
			// or maybe let player choose?
			//@cash..etc etc
			cm.dispose();
		} else if (status == 9) {
			cm.sendOk("NOT CODED...");
			cm.dispose();
		}
	}
}