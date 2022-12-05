/* Author: Xterminator (Modified by RMZero213)
	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
var status = -0;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 1) {
			qm.sendAcceptDecline("Found a strange statue made of ice. It's hard to tell what it looks like, because half of it has been broken into pieces. It might be the Spirit of the Snow Sculpture.");
		} else if (status == 2) {
			qm.forceStartQuest();
			qm.gainExp(14000);
			qm.dispose();
		}
	}
}

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (qm.getPlayer().getHp() <= 1) {
				qm.sendNext("Hey, your HP is not fully recovered yet. Did you take all the Roger's Apple that I gave you? Are you sure?");
				qm.dispose();
			} else {
				qm.sendNext("Are you ready to be sent to the secret group's area?");
			}
		
		} else if (status == 1) {
			qm.sendAcceptDecline("Found a strange statue made of ice. It's hard to tell what it looks like, because half of it has been broken into pieces. It might be the Spirit of the Snow Sculpture.");
		} else if (status == 2) {
			qm.gainExp(14000);
			//qm.gainMeso(90000);
			//qm.gainFame(3);
			//qm.gainItem(2010000, 3);
			//qm.gainItem(2010009, 3);
			//qm.warp(260000000);
			qm.forceCompleteQuest();
			qm.dispose();
		}
	}
}