/* 
 *  Dallier - King Medal
 *  Lith Habor = 104000000
 *  Sleepywood = 105040300
 */

var status = -1;

function start(mode, type, selection) {
    if (mode == 0) {
	if (status == 0) {
	    qm.sendNext("Come back when you feel like you are fully prepared for this.");
	    qm.dispose();
	    return;
	} else if (status == 2) {
	    status--;
	} else {
	    qm.dispose();
	    return;
	}
    } else {
	status++;
    }

    if (status == 0) {
	qm.askAcceptDecline("#v1142030# #e#b#t1142030##k\n\r\n\r - Time Limit: 1 hr\n\r - Donate the Most for this town....#nDo you want to test yourself and see if this Medal is for you?");
    } else if (status == 1) {
	qm.sendNext("Current Rank \n\r\n\r1. #bHotMeNow#k : ???,???,??? mesos");
    } else if (status == 2) {
	qm.sendNextPrev("Best of luck to you. There's no real set date for this, so if you feel like you qualify for this, then feel free to come see me so I can determine whether you qualify for it. And remember that you will not be able to challenge other Titles unless you either forfeit this challenge or complete it...");
	qm.dispose();
    }
}

function end(mode, type, selection) {
}

