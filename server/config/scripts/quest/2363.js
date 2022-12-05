var status = -1;

function end(mode, type, selection) {
	qm.debug("Mode: " + mode + " type: " + type + " selection: " + selection + " status: " + status);
	if(mode == 0 && selection == -1){
		status--;
	}else{
		status++;
	}
	
	if(qm.haveItem(4032616) && status == 0){
		qm.sendNext("This is great. The Mirror of Insight has choosen you. Are you ready to awaken as a Dual Blade?");
		return;
	}
	if(qm.haveItem(4032616) && status == 1){
		qm.forceCompleteQuest();
		qm.sendNext("From this moment, you are a #bBlade Recruit#k. Please have pride in all that you do.");
		if (qm.getJob() == 400) {
		    qm.changeJob(430);
		    qm.gainSp(1);
		    qm.resetStats(4, 25, 4, 4);
		    qm.expandInventory(1, 4);
		    qm.expandInventory(2, 4);
		    qm.expandInventory(3, 4);
		    qm.expandInventory(4, 4);
		    qm.gainItem(4032616, -1);
		    qm.gainItem(1342000, 1);
		    
		}
		qm.dispose();
	}
	
	
	
	if(status == 0){
		qm.sendNext("Good I like your confidence. In order to awaken as a Dual Blade, you must satisfy 2 conditions.\r\r\r\rThe first is that you must be Lv. 20 or above, and the second is that you must bring back the #bMirror of Insight#k, a legendary treasure that can only be obtained by one qualified to possess it.");
		return;
	}
	if(status == 1){
		qm.sendNextPrev("The Mirror of Insight is a treasure that  is said to give it's owner the gift of insight. They say that it will only appear to a master it finds suitable. Leave this room and follow the hallway to your left until you reach the Marble Room. Inside the room, you'll find marbles placed on a stand. When you hit these marbles, one will fall and turn into an opalescent color.");
		return;
	}
	if(status == 2){
		qm.sendOk("You can obtain the Mirror of Insight by breaking the opalescent marble. Of course, that is only if you are qualified to be the master of the Mirror of Insight...Good luck.");
		qm.forceStartQuest();
		qm.dispose();
		return;
	}
	
	/*qm.forceCompleteQuest();
	if (qm.getJob() == 400) {
	    qm.changeJob(430);
	    qm.gainSp(1);
	    qm.resetStats(4, 25, 4, 4);
	    qm.expandInventory(1, 4);
	    qm.expandInventory(2, 4);
	    qm.expandInventory(3, 4);
	    qm.expandInventory(4, 4);
	    qm.gainItem(1342000, 1);
	    qm.sendNext("You are now a Dual Blader.");
	}*/
	qm.dispose();
}

function start(mode, type, selection) {
	qm.debug("test");
	qm.dispose();
}
