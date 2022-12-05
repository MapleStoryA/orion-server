var status = -1;

function start() {

    action(1,0,0);
}

function action(mode, type, selection) {
    if (mode != 1) {
	cm.dispose();
	return;
    }
status++;
    if (status == 0) {
	if (cm.isPlayerInstance()) {
		if (cm.getMapId() == 749040100) {
			cm.sendSimple("What would you like to do? \r\n #L0#Leave the Mini Dungeon#l");
		} else {
			cm.sendSimple("What would you like to do? \r\n #L0#Leave the Meso Map#l");
		}
	} else {
		cm.sendOk("Sorry, I am not coded yet.");
		cm.safeDispose();
		return;
	}
    }
    else if (status == 1) {
	cm.sendYesNo("Are you sure you want to do that? You won't be able to come back, and you won't be refunded either!");
    }
    else if (status == 2) {
	if (cm.isPlayerInstance()) { 
		cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
	}
	cm.warp(910000000, 0);
	cm.dispose();
    }
}