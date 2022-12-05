
var status = 0;
var sel;

function start() {
	if (cm.getCQInfo(100009).contains("bing")) {
		status = -1;
	} else {
		status = 11;
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
			cm.sendNextS("Hellow, Mr.OSSS Boss.", 3);
		} else if (status == 1) {
			cm.sendNextS("#h #,\r\n\r\nYou are our savior, thanks you so much on helping to save the MapleWorld!", 1);
		} else if (status == 2) {
			cm.sendNextS("No problem, I just do what I can do.", 3);
		} else if (status == 3) {
			cm.sendNextS("Hahahaha! You are a cute guys! However, although the alien has been elimated, but the MapleWorld still need a hero to maintain it's balance and keep it safe, will you be the one?", 1);
		} else if (status == 4) {
			cm.sendNextS("Oh sure! I always wanted to be a Hero. Challenge accepted.", 3);
		} else if (status == 5) {
			cm.sendNextS("Alright, I will send you to the Secret Base now, there are a shuttle in there which will send you back to Henesys. Are you ready now?", 1);
		} else if (status == 6) {
			cm.sendNextS("Yes Sir! I'm ready now!", 3);
		} else if (status == 7) {
			cm.gainItem(4031753, 2);
			cm.updateCQInfo(100009, "gave");
			cm.sendNextS("I will send you right now. By the way, here are 2 Zeta Residue as a reward for you to save the MapleWorld.", 1);
		} else if (status == 8) {
			cm.warp(502010600);
			cm.dispose();
		} else if (status == 11) {
			cm.warp(502010600);
			cm.dispose();
		} else if (status == 12) { // shouldn't come here
			cm.warp(502010600);
			cm.dispose();
		}
	}
}