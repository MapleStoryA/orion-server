/*
	NPC Name: 		Regular Cab at Lith Habour
	Map(s): 		Victoria Road : Lith Habour (104000000)
	Description: 		Lith Habour
*/
var status = 0;
var maps = Array(120000000, 102000000, 100000000, 103000000);
var rCost = Array(1200, 1000, 1000, 1200);
var costBeginner = Array(120, 100, 100, 120);
var cost = new Array("1,200", "1,000", "1,000", "1,200");
var show;
var sCost;
var selectedMap = -1;


function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status >= 2) {
	    cm.sendNext("There's a lot to see in this town, too. Come back and find us when you need to go to a different town.");
	    cm.safeDispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
	cm.sendNext("Hi! I drive the Lith Harbor Regular Cab. Would you like to travel to a different town? If so, try using my cab. I can take you to a different town for a cheap price.");
    } else if (status == 1) {
	if (!cm.haveItem(4032313)) {
	    var job = cm.getJob();
	    if (job == 0 || job == 1000 || job == 2000) {
		var selStr = "We have a special 90% discount for beginners. Choose your destination, for fees will change from place to place.#b";
		for (var i = 0; i < maps.length; i++) {
		    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " mesos)#l";
		}
	    } else {
		var selStr = "Choose your destination, for fees will change from place to place.#b";
		for (var i = 0; i < maps.length; i++) {
		    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " mesos)#l";
		}
	    }
	    cm.sendSimple(selStr);
	} else {
	    cm.sendNextPrev("Hey, since you have a Taxi Coupon, I can take you to the town indicated on the pass for free. It looks like your destination is #bHenesys#k!");
	}
    } else if (status == 2) {
	if (!cm.haveItem(4032313)) {
	    var job = cm.getJob();
	    if (job == 0 || job == 1000 || job == 2000) {
		sCost = costBeginner[selection];
		show = costBeginner[selection];
	    } else {
		sCost = rCost[selection];
		show = cost[selection];
	    }
	    cm.sendYesNo("You don't have anything else to do here, huh? Do you really want to go to #b#m" + maps[selection] + "##k? It'll cost you #b" + show + " mesos#k.");
	    selectedMap = selection;
	} else {
	    cm.gainItem(4032313, -1);
	    cm.warp(100000000, 6);
	    cm.dispose();
	}
    } else if (status == 3) {
	if (cm.getMeso() < sCost) {
	    cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
	    cm.safeDispose();
	} else {
	    cm.gainMeso(-sCost);
	    cm.warp(maps[selectedMap]);
	    cm.dispose();
	}
    }
}