var status = -1;

function start(mode, type, selection) {
    	status++;
	if (status == 0) {
		qm.sendOk("Go to #bChief Tatamo#k in Leafre and bring back a Dragon Moss Extract.");
	} else if (status == 1) {
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
    	status++;
	if (status == 0) {
		if (qm.haveItem(4032531,1)) {
			qm.sendNext("Great! Please wait till I mix these ingredients together...");
		} else {
			qm.sendOk("Please go to #bChief Tatamo#k of Leafre and bring back a Dragon Moss Extract.");
			qm.dispose();
		}
	} else {
		if (qm.getJob() >= 3000) {
			qm.teachSkill(30001026, 1, 0); // Maker
		} else if (qm.getJob() == 2001 || qm.getJob() >= 2200) {
			qm.teachSkill(20011026, 1, 0); // Maker
	    } else if (qm.getJob() >= 2000) {
		qm.teachSkill(20001026, 1, 0); // Maker
	    } else if (qm.getJob() >= 1000) {
		qm.teachSkill(10001026, 1, 0); // Maker
	    } else {
		qm.teachSkill(1026, 1, 0); // Maker
	    }
		qm.gainExp(11000);
		qm.sendOk("There we go! You have learned the Soaring skill and will be able to fly, using great amounts of MP.");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}
	