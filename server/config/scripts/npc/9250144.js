// Time machine
var status = 0;

function start() {
	if (cm.getPlayer().getMapId() == 502010030) {
		if (cm.getCQInfo(100007).contains("timeMachine")) {
			status = -1; 
		} else if (cm.getCQInfo(100008).contains("future")) {
			status = 5;
		} else {
			status = 7;
		}
	} else {
		status = 99;
	}
    action(1, 0, 0);
	
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
		} else {
            status--;
		}
        if (status == 0) {
			if (cm.haveItem(4031752, 20)) {
				cm.sendNext("(I have already collected 20 Blinking Dingbats)");
				cm.dispose();
			} else {
				cm.sendNext("Bii.. Bii..\r\n(Seem like this machine will travel me to the #r#ePast#n#k. Should I go now?)");
			}
		} else if (status == 1) {
			var q = cm.getEventManager("TimeMachinePart");
			if (q == null) {
			    cm.sendOk("Unknown error occured");
			} else {
				cm.removeAll(4031752);
			    q.startInstance_CharID(cm.getPlayer());
			}
			cm.dispose();
		} else if (status == 6) {
			cm.sendNext("Bii.. Bii..\r\n(Seem like this machine will travel me to the #r#eFuture#n#k. Should I go now?)");
		} else if (status == 7) {
			cm.warp(502021010);
			cm.dispose();
		} else if (status == 8) {			
			cm.sendNext("The #b#eTime Machine#k#n seem buggy.");
			cm.dispose();
		} else if (status == 100) { //	npc not coded
			cm.sendNext("i'm not c11oded..");
			cm.dispose();
		}
	}
}