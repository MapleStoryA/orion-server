
var status = 0;
var sel;

function start() {
	if (cm.getCQInfo(100008).contains("paid")) {
		if (cm.getPlayer().getMapId() == 502029000) {
			status = 1;
		} else { // 502022010 energy laboratory long map
			status = -1;
		}
	} else if (cm.getCQInfo(100008).contains("success")) {
		status = 2;
	} else {
		status = 4;
	}
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
		if (status == 0 && mode == 0) {
			cm.sendNext("Come back again later.");
			cm.dispose();
			return;
		}
        if (mode == 1) {
            status++;
		} else {
            status--;
		}
        if (status == 0) {
			cm.sendYesNo("I will teleport you to the Crashed Site for 2 Zeta Residue."); //conversation here
		} else if (status == 1) {
			if (cm.haveItem(4031753, 2)) {
				cm.gainItem(4031753, -2);
				cm.warp(502029000, 0);
			} else {
				cm.sendOk("Error. Report this to a GM.");
				// shouldn't happen
			}
			cm.dispose();
		} else if (status == 2) { 
			cm.sendNext("Bing Force is waiting you at the entrance, don't let him wait.");
			cm.dispose();
		} else if (status == 3) {
			cm.sendNext("Congrats! You has completed your mission! I will send you back now."); // no esc
		} else if (status == 4) {
			cm.warp(502021010); // map with rozz
			cm.dispose();
		} else if (status == 5) {
			cm.sendNext("ERROR1");
			cm.dispose();
		}
	}
}