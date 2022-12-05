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
			qm.sendNext("To enter the Grandmaster Hall of Crimsonwood Keep and find the new twisted Masters, Jack Barricade says I'll need to find some way to open the gate to the Keep&apos;s Inner Sanctum.  Perhaps someone who has firsthand knowledge of the Keep will know what I need... and I know just who to ask!");
		} else if (status == 1) {
			qm.sendNextPrev("I asked Lukan about the Inner Sanctum Gate, and as I surmised, he knew how to get in.  Being a well-respected Stormcaster Knight, he was given a #bCrimsonwood Keystone#k by the Keep's Grandmasters.  However, Lukan thinks that I'm mad to assault the castle and refuses to give me the key, calling it a suicide mission.  However, if I can whittle down the Keep's defenders, he'll relent and give me the Keystone.  I've already put the hurt on the Stormbreakers, but I'll need to defeat at least #b75 Windraiders, 75 Firebrands#k, and #b75 Nightshadows#k more to put a sizeable enough dent in this evil army!");
		} else if (status == 2) {
			qm.sendAcceptDecline("I defeated the twisted Crimsonwood warriors!  And true to his word, Lukan gave me his #bCrimsonwood Keystone#k.  He also said that although the Keystone will physically unlock the Inner Sanctum Gate, the red energy field that surrounds them is foreign to him, and likely a new addition by the Keep's new Masters.  I should take the key back to Jack Barricade and let him know what I've discovered.");
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
			qm.gainExp(85000);
			qm.forceCompleteQuest(8222);
			qm.dispose();
		}
	}
}