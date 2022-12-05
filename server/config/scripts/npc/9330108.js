/* Kedrick
	Fishking King NPC
*/

var status = -1;
var sel;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
		if (status == 0) {
			cm.dispose();
			return;
		}
	status--;
    }

    if (status == 0) {
	cm.sendSimple("What do you want to do?\n\r #b#L0#Enter the Fishing Lagoon#l \n\r #L2#Return to old map#l");
    } else if (status == 1) {
	sel = selection;
	if (sel == 0) {
	    cm.sendSimple("Which one?\r\n#b#L0#Universe#l\r\n#L1#Fantasy#l\r\n#L2#Fairy#l#k");
	} else if (sel == 2) {
	    var returnMap = cm.getSavedLocation("FISHING");
	    if (returnMap < 0 || cm.getMap(returnMap) == null) {
		returnMap = 910000000; // to fix people who entered the fm trough an unconventional way
	    }
	    cm.clearSavedLocation("FISHING");
	    cm.warp(returnMap,0);
	    cm.dispose();
	}
    } else if (status == 2) {
	if (sel == 0 && selection <= 2 && selection >= 0) {
	    if (cm.getPlayer().getMapId() < 749050500 || cm.getPlayer().getMapId() > 749050502) {
	    	cm.saveLocation("FISHING");
	    }
	    cm.warp(749050500 + selection);
	    cm.dispose();
	} else {
	    cm.dispose();
	}
    }
}