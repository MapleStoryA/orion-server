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
			qm.sendNext("Hey, Man~ What's up? Haha! I am Roger who can teach you adorable new Maplers lots of information.");
		} else if (status == 1) {
			qm.sendNextPrev("You are asking who made me do this? Ahahahaha!\r\nMyself! I wanted to do this and just be kind to you new travellers.");
		} else if (status == 2) {
			qm.sendAcceptDecline("So..... Let me just do this for fun! Abaracadabra~!");
		} else if (status == 3) {
			if (qm.getPlayer().getHp() >= 50) {
				qm.getPlayer().setHp(25);
				qm.getPlayer().updateSingleStat(net.sf.odinms.client.MapleStat.HP, 25);
			}
			if (!qm.haveItem(2010007)) {
				qm.gainItem(2010007, 1);
			}
			qm.sendNext("Surprised? If HP becomes 0, then you are in trouble. Now, I will give you #rRoger's Apple#k. Please take it. You will feel stronger. Open the Item window and double click to consume. Hey, it's very simple to open the Item window. Just press #bI#k on your keyboard.");
		} else if (status == 4) {
			qm.sendPrev("Please take all Roger's Apples that I gave you. You will be able to see the HP bar increasing. Please talk to me again when you recover your HP 100%.");
		} else if (status == 5) {
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
			if (qm.getPlayer().getHp() <= 0) {
				qm.sendNext("TEST REPORT");
				qm.dispose();
			} else {
				qm.sendNext("Hmmm... wait, this is...!! Wow... interesting... it's definitely worth studying. Hey, you look like a traveler... have you ever been to Eos Tower? If so, then you may have met a monster that resembles an Octopus, #o3230302#. A monster called #b#o3230302##k, isn't it strange?");
			}
		} else if (status == 1) {
			qm.sendNextPrev("What do I mean by this? Well, the way #o3230302# is structured, it really looks familiar...and after some researching, I have come to conclusion that it may be close to that of the aliens. Maybe #o3230302# is made by the aliens. It's defintely worth more attention. Can you help me on this?");
		} else if (status == 2) {
			qm.sendNextPrev("But you know... there's gotta be a better method to research something like #t4000099#. Something like a #bManual#k... but it&apos;s really hard to get, so just disregard what I just said.");
		} else if (status == 3) {
			qm.gainExp(8000);
			qm.gainItem(2000011, 80);
			qm.gainItem(4000099, -1);
			//qm.gainItem(2010009, 3);
			qm.forceCompleteQuest(3452);
			qm.dispose();
		}
	}
}