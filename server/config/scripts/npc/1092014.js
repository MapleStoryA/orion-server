/* Author: Xterminator
	NPC Name: 		Nautilus' Mid-Sized Taxi
	Map(s): 		Victoria Road : Nautilus Harbor (120000000)
	Description: 		Nautilus Harbor Taxi
*/

var status = -1;
var maps = Array(104000000, 102000000, 100000000, 103000000, 101000000);
var rCost = Array(1200, 1000, 1000, 1200, 1000);
var costBeginner = Array(120, 100, 100, 120, 100);
var cost = new Array("1,200", "1,000", "1,000", "1,200", "1,000");
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
	cm.sendNext("How's it going? I drive the Nautilus' Mid-Sized Taxi. If you want to go from town to town safely and fast, then ride our cab. We'll gladly take you to your destination with an affordable price.");
    } else if (status == 1) {
	if (cm.getJob() == 0) {
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
    } else if (status == 2) {
	if (cm.getJob() == 0) {
	    sCost = costBeginner[selection];
	    show = costBeginner[selection];
	} else {
	    sCost = rCost[selection];
	    show = cost[selection];
	}
	cm.sendYesNo("You don't have anything else to do here, huh? Do you really want to go to #b#m" + maps[selection] + "##k? It'll cost you #b" + show + " mesos#k.");
	selectedMap = selection;
    } else if (status == 3) {
	if (cm.getMeso() < sCost) {
	    cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
	    cm.safeDispose();
	} else {
	    cm.gainMeso(-sCost);
	    cm.warp(maps[selectedMap], 0);
	    cm.dispose();
	}
    }
}