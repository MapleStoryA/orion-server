/**
	El Nath Magic Spot - Orbis Tower <20th Floor>(200080200)
**/

var status = -1;

function action(mode, type, selection) {
    if (mode == 0) {
	cm.dispose();
	return;
    }
    status++;

    if (status == 0) {
	if (cm.haveItem(4001019)) {
	    cm.sendYesNo("You can use #b#t4001019#k to activate #b#p2012014##k. Will you teleport to where #b#p2012015##k is?");
	} else {
	    cm.sendOk("There's a #b#p2012015##k that'll enable you to teleport to where #b#p2012014##k is, but you can't activate it without the scroll.");
	    cm.safeDispose();
	}
    }
    if (status == 1) {
	cm.gainItem(4001019, -1);
	cm.warp(200080200,0);
	cm.dispose();
    }
}
