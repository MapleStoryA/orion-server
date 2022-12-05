/**
 *9201026 - Nana(L)
 *@author Jvlaple
 */
 
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (cm.getPlayer().getMarriageQuestLevel() == 1 || cm.getPlayer().getMarriageQuestLevel() == 52) {
			if (!cm.haveItem(4000106, 30)) {
				if (status == 0) {
					cm.sendNext("Hey, you look like you need proofs of love? I can get them for you.");
				} else if (status == 1) {
					cm.sendNext("All you have to do is bring me 30 #bTeddy Cotton#k.");
					cm.dispose();
				}
			} else {
				if (status == 0) {
					cm.sendNext("Wow, you were quick! Heres the proof of love...");
					cm.gainItem(4000106, -30)
					cm.gainItem(4031370, 1);
					cm.dispose();
				}
			}
		} else {
			cm.sendOk("Hi, I'm Nana the love fairy... Hows it going?");
			cm.dispose();
		}
	}
}