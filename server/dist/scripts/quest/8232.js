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
			qm.sendNext("With all the strange occurrences in Masteria and New Leaf City, Lita Lawless must be busy! I'll bet that she's willing to accept my help...maybe I can earn some mesos in the process! A quick jaunt to the Kerning City subway and I'll be on my way to New Leaf City!");
		} else if (status == 1) {
			qm.sendNextPrev("I spoke with Lita, and it looks like there a trickster roaming about the Phantom Forest. It caused her quite a bit of trouble-surprising for a warrior of her caliber. I've agreed to help rid the Forest of these nefarious creatures, and as proof, I have to bring her 30 of the strange silver clovers-she called them Lucky Charms. I'll be on guard-that forest is haunted...or so I've heard...");
		} else if (status == 2) {
			qm.sendAcceptDecline("So you'll help me?");
		} else if (status == 3) {
			
			qm.forceStartQuest();
			qm.dispose();
		}
	}
}
