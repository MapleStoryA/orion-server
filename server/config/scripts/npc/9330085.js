/* Yuan bao fo
*/
var fromYuanBaoMap;

function start() {
    if (cm.getMapId() == 749040000) {
	fromYuanBaoMap = true;
	cm.sendSimple("Is this place too spooky for you? \n\r #b#L0#Yes, bring me back to where I came from.#l");
    } else if (cm.getMapId() == 749040001) {
	cm.warp(cm.getSavedLocation("DONGDONGCHIANG"), 0);
	cm.dispose();
    } else {
	fromYuanBaoMap = false;
	cm.sendSimple("Happy New Yearrrrr! Hahahah! So, you need that awesome Stick Cracker & Cracker Shooter, right? With only 10,000 mesos I can give you not only Stick Cracker & Cracker Shooter buy also can send you to someplace very special. Guess what will be waiting for you there... A Dong Dong Chiang! How about that?\n\
\n\r #b#L0#Enter the map of Dong Dong Chiang#l \n\r #b#L1#Trade 88 Red Packet for 888,888 mesos.#l \n\r #b#L2#Trade 288 Red Packet for Silver Master key.#l \n\r #b#L3#Trade 488 Red Packet for Gold Master key.#l");
    }
}

function action(mode, type, selection) {
    if (!fromYuanBaoMap) {
	if (mode == 1) {
	    switch (selection) {
		case 0:
		    cm.saveLocation("DONGDONGCHIANG");
		    cm.warp(749040000, 0);

		    if (!cm.haveItem(1472081, 1, true, true)) {
			cm.gainItem(1472081, 1);
		    }
		    if (!cm.haveItem(2070020)) {
			cm.gainItem(2070020, 500);
		    }
		    break;
		case 1:
		    if (cm.haveItem(4000306, 88)) {
			cm.gainMeso(888888);
			cm.gainItem(4000306, -88);
		    }
		    break;
		case 2:
		    if (cm.haveItem(4000306, 288)) {
			cm.gainItem(5490001, 1);
			cm.gainItem(4000306, -288);
		    }
		    break;
		case 3:
		    if (cm.haveItem(4000306, 488)) {
			cm.gainItem(5490000, 1);
			cm.gainItem(4000306, -488);
		    }
		    break;
	    }

	} else {
	    cm.sendNext("Huh? Not interested in Dong Dong Chiang? Hmmm, well, hope it will be a good year for you anyway. Come back to me anytime when you change your mind. Okay?");
	}
    } else {
	if (mode == 1) {
	    cm.warp(cm.getSavedLocation("DONGDONGCHIANG"), 0);
	}
    }
    cm.dispose();
}