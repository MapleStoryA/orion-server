/*
	Nella - Hidden Street : 1st Accompaniment
*/

var status;

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == 0 && status == 0) {
	cm.dispose();
	return;
    } else {
	if (mode == 1)
	    status++;
	else
	    status--;
	var mapId = cm.getMapId();
	if (mapId == 103000890) { 
	    cm.warp(103000000, "mid00");
	    cm.removeAll(4001007);
	    cm.removeAll(4001008);
	    cm.dispose();
	} else {
	    var outText;
	    if (mapId == 103000805) {
		outText = "Are you ready to leave this map?";
	    } else {
		outText = "Once you leave the map, you'll have to restart the whole quest if you want to try it again.  Do you still want to leave this map?";
	    }
	    if (status == 0) {
		cm.sendYesNo(outText);
	    } else if (mode == 1) {
		cm.warp(103000890, "st00"); // Warp player
		cm.dispose();
	    }
	}
    }
}