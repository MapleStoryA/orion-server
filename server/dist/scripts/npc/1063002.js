/* NPC : A pile of white flower
 * Location : Sleepywood, forest of patient
 */

var itemSet = new Array(4020007, 4020008, 4010006);
var rand = Math.floor(Math.random() * itemSet.length);

function action(mode, type, selection) {
    if (mode == 1) {
	cm.warp(105040300);
            
	if (cm.getMapId() == 105040300) {
	    if (cm.getQuestStatus(2052) == 1 && !cm.haveItem(4031025)) {
		cm.gainItem(4031025, 10);
	    }
	} else {
	    cm.gainItem(itemSet[rand], 2);
	}
    }
    cm.dispose();
}