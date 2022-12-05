var status = -1;
//this quest is SECRET ORGANIZATION 1
function start(mode, type, selection) {
	qm.sendNext("Make a Growth Accelerant.");
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	if (qm.isQuestFinished(22568) || qm.haveItem(4032468,10)) {
		qm.getPlayer().gainSp(2);
		qm.forceCompleteQuest();
		qm.gainItem(4032468, -10);	
	} else {
		qm.sendNext("Make a growth accelerant.");
		qm.forceStartQuest();
	}
	qm.dispose();
}