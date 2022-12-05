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
			qm.sendNext("I've heard tell that many travelers get lost in the spooky Phantom Forest.  If I come across any of them, perhaps one of them will have an interesting tale to tell about the area.  I should search them out, but I better make sure I don't become one of these lost travelers myself... yikes!");
		} else if (status == 1) {
			qm.sendNextPrev("I met a knight lost in the Phantom Forest named #bLukan de Vrisien#k.  His armor is very ancient-looking, and he seems to be from another era.  Lukan asked if I knew anything about the unfriendly #bStormbreaker#k warriors wandering the forest.  He seems to hate them, though strangely, his armor looks almost exactly like theirs... lightning bolts and all!  Regarding this, Lukan remains tight-lipped... at least until I prove I'm not in league with them.  To gain his trust, and prove my fighting prowess, I need to #bdefeat these Stormbreakers#k and #bcollect 10 of their badges#k to bring to him.");
		} else if (status == 2) {
			qm.sendAcceptDecline("After defeating the Stormbreakers as he requested, Lukan explained that he is a #bStormcaster Knight#k from Crimsonwood Keep.  When I told him that Crimsonwood Keep was ancient history, and that Masteria had been lost for a millenium, he seemed at a loss for words.  Somehow, he has been transported to this time period, his future.  Strange things are brewing indeed.");
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
				qm.sendNext("I defeated the Stormbreakers.");}
		
		} else if (status == 1) {
			qm.sendNextPrev("Here is your reward");
			} else if (status == 2) {
			qm.gainExp(85000);
			qm.forceCompleteQuest(8222);
			qm.dispose();
		}
	}
}