/* Author: Xterminator (Modified by RMZero213)
	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			qm.sendNext("The Versalian code that Jack uncovered and that Elpam translated points to some sort of imminent attack on New Leaf City by the Twisted Master's; army.  It's up to us to stop them!  I should talk to John...");
		} else if (status == 1) {
			qm.sendNextPrev("Jack and I have determined that we.  Unfortunately.  Someone who has firsthand knowledge of the Keep.  I think I have an idea who might be able to help.");
		} else if (status == 2) {
			qm.sendAcceptDecline("I brought Lukan's #bCrimsonwood Keystone#k to Jack.  I explained what Lukan told me: that this would unlock the gate mechanically, but the red energy field was a new development. Jack thinks he can figure out a solution for the magical barrier, but he'll need some time.  I should check in with him later and see if he's made any progress.");
		} else if (status == 3) {
			qm.forceStartQuest();
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
				qm.sendNext("I have found the keystone.");}
		
		} else if (status == 1) {
			qm.sendNextPrev("Here is your reward");
			} else if (status == 2) {
			qm.gainItem(3992041, -1);
			qm.gainExp(85000);
			qm.forceCompleteQuest(8230);
			qm.dispose();
		}
	}
}