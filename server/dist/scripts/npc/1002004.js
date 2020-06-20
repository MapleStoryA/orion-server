/* Author: Xterminator
	NPC Name: 		VIP Cab
	Map(s): 		Victoria Road : Lith Harbor (104000000)
	Description: 		Takes you to Ant Tunnel Park
*/
var status = 0;
var cost;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 1 && mode == 0) {
	cm.sendNext("This town also has a lot to offer. Find us if and when you feel the need to go to the Ant Tunnel Park.");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	cm.sendNext("Hi there! This cab is for VIP customers only. Instead of just taking you to different towns like the regular cabs, we offer a much better service worthy of VIP class. It's a bit pricey, but... for only 10,000 mesos, we'll take you safely to the \r\n#bAnt Tunnel#k.");
    } else if (status == 1) {
	var job = cm.getJob();
	if (job == 0 || job == 2000 || job == 1000) {
	    cm.sendYesNo("We have a special 90% discount for beginners. The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island, where the 24 Hr Mobile Store is. Would you like to go there for #b1,000 mesos#k?");
	    cost = 1000;
	} else {
	    cm.sendYesNo("The regular fee applies for all non-beginners. The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island, where 24 Hr Mobile Store is. Would you like to go there for #b10,000 mesos#k?");
	    cost = 10000;
	}
    } else if (status == 2) {
	if (cm.getMeso() < cost) {
	    cm.sendNext("It looks like you don't have enough mesos. Sorry but you won't be able to use this without it.")
	} else {
	    cm.gainMeso(-cost);
	    cm.warp(105070001, 0);
	}
	cm.dispose();
    }
}