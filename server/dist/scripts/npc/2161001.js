var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.dispose();
	}
	status--;
    }
    if (status == 0) {
	cm.sendYesNo("Would you like to go to the Audience Room?");
    } else if (status == 1) {
	cm.warp(211070000, 1);
	cm.dispose();
    }
}