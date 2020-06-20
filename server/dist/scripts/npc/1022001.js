/* Author: Xterminator
	NPC Name: 		Regular Cab
	Map(s): 		Victoria Road : Perion (102000000)
	Description: 		Perion Cab
*/

var status = 0;
var maps = Array(104000000, 100000000, 101000000, 103000000, 120000000);
var rCost = Array(1200, 1000, 1000, 800, 1000);
var costBeginner = Array(120, 100, 100, 80, 100);
var cost = new Array("1,200", "1,000", "1,000", "800", "1,000");
var show;
var sCost;
var selectedMap = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 1 && mode == 0) {
	cm.dispose();
	return;
    } else if (status >= 2 && mode == 0) {
	cm.sendNext("There's a lot to see in this town, too. Come back and find us when you need to go to a different town.");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	cm.sendNext("Hello, I drive the Regular Cab. If you want to go from town to town safely and fast, then ride our cab. We'll gladly take you to your destination with an affordable price.");
    } else if (status == 1) {
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
    } else if (status == 2) {
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
    } else if (status == 3) {
	if (cm.getMeso() < sCost) {
	    cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
	} else {
	    cm.gainMeso(-sCost);
	    cm.warp(maps[selectedMap]);
	}
	cm.dispose();
    }
}