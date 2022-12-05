/*
	NPC Name: 		Kimu
	Description: 		Quest - Cygnus tutorial helper
*/

var status = -1;

function start(mode, type, selection) {
	qm.sendNext("You can press R to searh for parties!");
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
    qm.gainExp(100);
	qm.forceCompleteQuest();
	qm.gainExp(15);
	qm.dispose();
    
}