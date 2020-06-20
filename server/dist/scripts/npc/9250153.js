status = 0;
var sel, ps, rs, str, inc;

function start() {
	cm.sendSimple("#eHello and welcome to the feedback npc. It is my job to tell the Admins what YOU think #h #! What would you like to do? \r\n\r\n#L0#Submit a comment#l\r\n\r\n " + (cm.getPlayer().isGM() ? "#r[GM Option]#k\r\n#L1# Read Comments#l" : ""));
}

function action (mode, type, selection) {
	status++;
	if (mode != 1) {
		cm.dispose();
		return;
	}
	if (status == 1) {
		sel = selection;
		if (selection == 0) {
			cm.sendGetText("#eWrite your feedback here.\r\nmax 95 words.\r\n");
		} else if (selection == 1) {
			cm.sendOk(cm.generateFeedbackList());
			cm.dispose();
		}
	} else if (status == 2) {
		if (sel == 0) {
			cm.sendFeedback(cm.getText());
			cm.sendOk("#eYour opinions on the server has been shared with our admins. Thanks for your time!");
			cm.dispose();
		}
	} else if (status == 3) {
	}
}