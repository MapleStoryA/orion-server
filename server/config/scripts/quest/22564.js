var status = -1;
//this quest is DRAGON KNOWLEDGE
function start(mode, type, selection) {
	qm.sendNext("Go talk to Chief Tatamo of Leafre.");
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	qm.getPlayer().gainSp(1);
	qm.forceCompleteQuest();
	qm.dispose();
}