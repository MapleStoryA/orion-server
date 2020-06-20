/*
	NPC Name: 		Han the Broker
	Map(s): 		Magatia
	Description: 	Quest - Test from the Head of Alcadno Society
*/

var status = -1;
var oreArray;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
    if (mode == -1) {
	qm.dispose();
    } else {
	oreArray = getOreArray();
	if (status == -1) {
	    if (oreArray.length > 0) {
		status++;
		qm.sendSimple("Oh, looks like someone's ready to make a deal. You want to join Alcadno so badly, huh? I really don't understand you, but that's just fine. What will you give me in return?\r\n" + getOreString(oreArray));
	    } else {
		qm.sendOk("What is this, you don't have the ores with you. No ore, no deal.");
		qm.dispose();
	    }
	} else if (status == 0) {
	    qm.gainItem(oreArray[selection], -2); // Take 2 ores
	    qm.sendNext("Then wait for awhile. I'll go and get the stuff to help you pass the test of Chief Alcadno.");
	    qm.forceCompleteQuest();
	    qm.dispose();
	} else {
	    qm.dispose();
	}
    }
}

function getOreArray() {
    var ores = new Array();
    for (var x = 4020000; x <= 4020008; x++) {
	if (qm.haveItem(x, 2)) {
	    ores.push(x);
	}
    }
    return ores;
}

function getOreString(ids) { // Parameter 'ids' is just the array of getOreArray()
    var thestring = "#b";
    var extra;
    for (var x = 0; x < ids.length; x++) {
	extra = "#L" + x + "##t" + ids[x] + "##l\r\n";
	thestring += extra;
    }
    thestring += "#k";
    return thestring;
}