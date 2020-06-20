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
			qm.sendNext("I heard that John Barricade has received news from his brother Jack! I should go find John in Bigger Ben and see what he's learned.");
		} else if (status == 1) {
			qm.sendNextPrev("Apparently, Jack Barricade is in Masteria and has discovered Crimsonwood Keep deep in the Phantom Forest. However, the last letter John received from his brother was dated over a month ago. John wants me to head to Crimsonwood after him and see why Jack's communiques have stopped. Looks like I need to make my way through the Phantom Forest... I'd better stock up on potions!");
		} else if (status == 2) {
			qm.sendAcceptDecline("So you'll help me?");
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
				qm.sendNext("I found Jack Barricade resting in a cave on Crimsonwood Mountain. It turns out that after exploring the Masterian wilderness for weeks, Jack finally came across the ancient stronghold - only it wasn't deserted.");
				qm.dispose();
			} else {
				qm.sendNext("Jack was captured and imprisoned in the Keep by the evil soldiers I've been seeing around, but luckily managed to escape. John will be relieved that Jack is okay.");
			}
		
		} else if (status == 1) {
			qm.sendNextPrev("I found Jack Barricade resting in a cave on Crimsonwood Mountain. It turns out that after exploring the Masterian wilderness for weeks, Jack finally came across the ancient stronghold - only it wasn't deserted. Jack was captured and imprisoned in the Keep by the evil soldiers I've been seeing around, but luckily managed to escape. John will be relieved that Jack is okay.");
		} else if (status == 2) {
			//qm.gainExp(22500);
			//qm.gainMeso(90000);
			//qm.gainFame(3);
			//qm.gainItem(2010000, 3);
			//qm.gainItem(2010009, 3);
			qm.forceCompleteQuest();
			qm.dispose();
		}
	}
}