/*
	NPC Name: 		Kimu
	Description: 		Quest - Cygnus tutorial helper
*/

var status = -1;

function start(mode, type, selection) {
	qm.forceStartQuest();
	qm.sendNext("...");
	qm.dispose();
	qm.warp(910400000);
}

function end(mode, type, selection) {
    qm.gainExp(100);
	qm.forceCompleteQuest();
	qm.gainExp(15);
	qm.dispose();
    
}