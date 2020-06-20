/*
	Hak - Cabin <To Mu Lung>(200000141) / Mu Lung Temple(250000100) / Herb Town(251000000)
*/
var menu = new Array("Mu Lung","Orbis","Herb Town","Mu Lung");
var cost = new Array(6000,6000,1500,1500);
var hak;
var display = "";
var btwmsg;
var method;

function start() {
    status = -1;
    hak = cm.getEventManager("Hak");
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if(mode == 0 && status == 0) {
	cm.dispose();
	return;
    } else if(mode == 0) {
	cm.sendNext("OK. If you ever change your mind, please let me know.");
	cm.dispose();
	return;
    }
    status++;
    if (status == 0) {
	for(var i=0; i < menu.length; i++) {
	    if(cm.getMapId() == 200000141 && i < 1) {
		display += "\r\n#L"+i+"##b"+menu[i]+"("+cost[i]+" mesos)#k";
	    } else if(cm.getMapId() == 250000100 && i > 0 && i < 3) {
		display += "\r\n#L"+i+"##b"+menu[i]+"("+cost[i]+" mesos)#k";
	    }
	}
	if(cm.getMapId() == 200000141 || cm.getMapId() == 251000000) {
	    btwmsg = "#bOrbis#k to #bMu Lung#k";
	} else if(cm.getMapId() == 250000100) {
	    btwmsg = "#bMu Lung#k to #bOrbis#k";
	}
	if(cm.getMapId() == 200000141 && (hak == null || hak.getProperty("isRiding").equals("true"))) {
	    cm.sendNext("Someone else is on the way to Mu Lung right now. Talk to me a little bit more.");
	    cm.dispose();
	}
	if(cm.getMapId() == 251000000) {
	    cm.sendYesNo("Hello there? I'm the crane that flies from "+btwmsg+" and back. I fly around all the time, so I figured, why not make some money by taking travelers like you along for a small fee? It's good business for me. Anyway, what do you think? Do you want to fly to #b"+menu[3]+"#k right now? I only charge #b"+cost[3]+" mesos#k.");
	} else {
	    cm.sendSimple("Hello there? I'm the crane that flies from "+btwmsg+" and back. I fly around all the time, so I figured, why not make some money by taking travelers like you along for a small fee? It's good business for me. Anyway, what do you think?\r\n"+display);
	}
    } else if(status == 1) {
	if(selection == 2) {
	    cm.sendYesNo("Will you move to #b"+menu[2]+"#k now? If you have #b"+cost[2]+" mesos#k, I'll take you there right now.");
	} else {
	    if(cm.getMeso() < cost[selection]) {
		cm.sendNext("Are you sure you have enough mesos?");
		cm.dispose();
	    } else {
		if(cm.getMapId() == 251000000) {
		    cm.gainMeso(-cost[3]);
		    cm.warp(250000100);
		    cm.dispose();
		} else {
		    if(hak != null && hak.getProperty("isRiding").equals("false")) {
			cm.gainMeso(-cost[selection]);
			hak.newInstance("Hak");
			hak.setProperty("myRide",selection);
			hak.getInstance("Hak").registerPlayer(cm.getChar());
			cm.dispose();
		    } else {
			cm.sendNext("Someone else is on the way to Orbis right now. Talk to me a little bit more.");
			cm.dispose();
		    }
		}
	    }
	}
    } else if(status == 2) {
	if(cm.getMeso() < cost[2]) {
	    cm.sendNext("Are you sure you have enough mesos?");
	    cm.dispose();
	} else {
	    cm.gainMeso(-cost[2]);
	    cm.warp(251000000);
	    cm.dispose();
	}
    }
}