var status = -1;
function end(mode, type, selection) {
	
	if(qm.haveItem(4032617) && qm.getPlayer().getLevel() >= 30){
		qm.sendNext("Finally... I have my father's Diary. Thank you. I'm starting to trust you even more.. Your current position doesn't seem to suit your great abilities. I think you have the qualifications to advance to a #bBlade Acolyte#k. I will advance you to a Blade Acolyte now.");
		qm.gainSp(1);
		qm.gainItem(4032617, -1);
		qm.gainItem(1052244, 1);
		qm.changeJob(431);
		qm.forceCompleteQuest();
		qm.dispose();
		return;
	}
	if (qm.getJob() == 430) {
	  qm.sendOk("Inside the Jazz Bar, where the Dark Lord resides, there is a secret room. Within that room lies the last remaining article left by my father. It's his #bDiary#k. Please go there and return with the Diary before the entire place is engulfed in war.\r\r\rTo enter the secret room, you'll have to enter throught a small door towards the back of the #bJazz Bar#k counter. Be careful and make sure the Dark Lord doesn't see you.");
	  qm.forceStartQuest();
	  qm.dispose();
	}
	qm.dispose();
}

function start(mode, type, selection) {
    qm.dispose();
}
