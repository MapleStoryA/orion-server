importPackage(java.lang);
importPackage(Packages.server)
importPackage(Packages.server.maps)

var a = -1;
var b = -1;
var meso = 10000;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 2 && mode == 0) {
			cm.sendOk("Alright, see you next time.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			a = Math.round(Math.random() * 100);
			b = Math.round(Math.random() * 100);
			cm.sendGetNumber("What is " + a + " + " + b + "?", 0, 0, 10000);
		} else if (status == 1) {
			if (selection == a + b) {
				cm.sendOk("Wow, you did it! I will send you back!");
			} else {
				cm.sendNext("You got it wrong!");
				meso -= 1000
				status = -1;
			}
		} else if (status == 2) {
			cm.gainMeso(meso);
			var map = cm.getChar().getSavedLocation(SavedLocationType.RANDOM_EVENT);
			if (map == cm.getChar().getMap().getId()) {
				meso += 100000;
				map = 100000000;
			}
			if (meso < 0) meso = 0;
			cm.getChar().clearSavedLocation(SavedLocationType.RANDOM_EVENT);
			cm.warp(map, 0);
		}
	}
}