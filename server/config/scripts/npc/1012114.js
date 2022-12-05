var status = -1;

function action(mode, type, selection) {
	if (mode == 0 && status == 0) {
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendSimple("Hello, I'm Growlie and I want #bRice Cakes#k...#b\r\n#L0#I brought you Rice Cakes!#l\r\n#L1#What do I do here?#l#k");
	} else if (status == 1) {
		if (selection == 0) {
			if (!cm.isLeader()) {
				cm.sendNext("Only the leader may bring me Rice Cake.");
			} else {
				if (cm.haveItem(4001101,10)) {
					cm.gainItem(4001101, -10);
					cm.givePartyExp(1600);
					cm.givePartyNX(250);
					cm.endPartyQuest(1200);
					cm.warpParty(910010300);
				} else {
					cm.sendNext("You do not have 10 Rice Cakes.. ");
				}
			}
		} else if (selection == 1) {
			cm.sendNext("This is the Primrose Hill where the Moon Bunny will make #bRice Cakes#k when there is a full moon. To make a full moon, plant the seeds obtained from the primroses and when all 6 seeds are planted, them full moon will appear. The #rMoon Bunny will then be summoned, and you must protect him from the other monsters that try to attack him#k. In the event of #bMoon Bunny#k dying, you will fail the quest and I will be hungry and angry...");

		}
		cm.dispose();
	}
}