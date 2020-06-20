// bing force
var status = 0;

function start() {
	if (cm.getMapId() == 502030004) {
		status = 99; // start from 100 (pq npc)
	} else {
		if (cm.getCQInfo(100008).contains("paid")) {
			status = -1;
		} else if (cm.getCQInfo(100008).contains("readyStart")) {
			status = 0;
		} else if (cm.getCQInfo(100008).contains("success")) {
			status = 2;
		} else {
			status = 3;
		}
	}
    action(1, 0, 0);	
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
		if (status == 1 && mode == 0) {
			cm.sendNext("Come back again later.");
			cm.dispose();
			return;
		}
        if (mode == 1) {
            status++;
		} else {
            status--;
		}
        if (status == 0) {
			//portal exit will update cq info.
			cm.sendNext("Hey #h #,\r\nI'm the guard of this crashed Mothership. You seem kinda strange for me, but whatever, do you want to enter the ship and kick those alien's ass?\r\nI will provide you some support through.");
			cm.updateCQInfo(100008, "readyStart");
			cm.dispose();
		} else if (status == 1) {
			cm.sendAcceptDecline("Are you ready to accept the quest?");
		} else if (status == 2) { // Second part
			var q = cm.getEventManager("LumpEnergy");
			if (q == null) {
			    cm.sendOk("Unknown error occured");
			} else {
				cm.removeAll(4001459);
			    q.startInstance_CharID(cm.getPlayer());
			}
			cm.dispose();
		} else if (status == 3) {
			cm.sendNext("Congrats! You has completed the quest, no need to thanks me, my job is here to help you.");
			cm.dispose();
		} else if (status == 4) {
			cm.sendNext("Hey kid! What are you doing here?");
			cm.dispose();		
		} else if (status == 100) {
			var eim = cm.getEventInstance();
		    if (eim != null && eim.getEventManager() != null) {
				var em = eim.getEventManager();
				var stage = parseInt(eim.getProperty("stage"));
				if (stage >= 4) {
					cm.sendNext("I will be waiting for you at the entrance!");
				} else { //add occupation check, give hint!"
					cm.sendNext(stage + "please eliminate the mobs..etc i will be here to help you...(give function to heal hp..)..if you wanna exit let me know..");
				} 
				cm.dispose();
			} else {
				cm.sendNext("what are you doing here?");
			}			
			cm.dispose();
		}		
	}
}