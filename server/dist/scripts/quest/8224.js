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
			qm.sendNext("I've heard people say that there's a band of bandits in the Phantom Forest.  Maybe they're responsible for the travelers that go missing in the woods.  Perhaps I should investigate and see if there is any truth to this story");
		} else if (status == 1) {
			qm.sendNextPrev("Turns out that these so-called bandits are not responsible for any disappearances, at least so they say.  However, I'm still not really sure why they're camped out here in the middle of these haunted woods.  Their leader, a mysterious tough named #bTaggrin#k, seems quite unfriendly to strangers.  Taggrin said that there was one thing I could do however: #bcut down the possessed Phantom Trees#b and bring him #b50#k of their #bseeds#k.  I'm not sure why he wants them, but I guess the fewer angry trees around the better for everyone. ");
			} else if (status == 2) {
			qm.sendAcceptDecline("I fulfilled Taggrin's request and brought him the fallen seeds from the Phantom Trees.  He seemed to warm up to me for doing this, and has opened the bandit's camp up to me.  Now I can buy and sell things from their provisioner, Mo.  Whew, this has definitely saved me the effort of having to trek all the way back to NLC!");
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
				qm.sendNext("Pia asked me to go to the thicket around the beach and take out Mano. I'm to bring back Rainbow-colored Snail Shell. So you get to stay here while I do all the work for you again? Don't you think this is a bit too much? I might use all of the Rainbow-colored Snail Shell for myself!");
			}
		
		} else if (status == 1) {
			qm.sendNextPrev("4Rainbow-colored Snail Shell wasn't an item that makes dreams come true. It turns people who use it into snails. Phew... learned a good lesson not to believe in rumors, otherwise I would be the one crawling about.");
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