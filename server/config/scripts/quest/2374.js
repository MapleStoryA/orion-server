var status = -1;
function end(mode, type, selection) {
	
	if(qm.haveItem(4032619) && qm.getMap().getId() != 211000001){
		if(status == -1){
			qm.sendNext("I've been waiting for you. Do you have Arec's Letter? Please give me his letter.");
			status = 0;
			return;
		}
		if(status == 0){
			qm.sendNext("We have finally received Arec's official recognition. This is an important moment for us. It's also time to you experience a change.");
			status = 1;
			return;
		}
		if(status == 1){
			if(!qm.canHold(4032619, 1)){
				qm.sendNext("Please free at least one Equip slow before advancing to Blade Specialist.");
				qm.dispose();
				return;
			}else{
				status = 2;
				qm.sendOk("Now that we have Arec's recognition, you can make a job advancement by going see him when you reach Lvl 70. Finally, a new future has been opened for the Dual Blades.");
				qm.changeJob(432);
				qm.gainSp(1);
				qm.gainItem(1132021);
				qm.forceCompleteQuest();
			}
		}
		qm.dispose();
		return;
	}
	if (qm.getJob() == 431 && qm.getMap().getId() == 211000001) {
	    qm.forceStartQuest();
	    qm.dispose();
	    return;
	}
	qm.dispose();
}

function start(mode, type, selection) {
    qm.dispose();
}
