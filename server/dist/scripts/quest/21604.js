var status = -1;

function start(mode, type, selection) {
	qm.forceStartQuest();
	qm.forceCompleteQuest();
	qm.teachSkill(20001004, 1);
	qm.teachSkill(20011004, 1);
	qm.debug("test");
	qm.dispose();
}

function end(mode, type, selection) {
	qm.forceStartQuest();
	qm.forceCompleteQuest();
	qm.teachSkill(20001004, 1);
	qm.teachSkill(20011004, 1);
	qm.dispose();
}