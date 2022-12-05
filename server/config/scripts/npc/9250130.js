
var status = 0;
var sel;

function start() {
	if (cm.getCQInfo(100008).contains("future")) {
		status = -1;
	} else if (cm.getCQInfo(100008).contains("success")) {
		status = 10;
	} else {
		status = 11;
	}
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
		} else {
            status--;
		}
        if (status == 0) {
			cm.sendNextS("Er.. Excuse me Sir..?", 3);
		} else if (status == 1) {
			cm.sendNextS("Sup man, I am not hiding.. Mehhh!", 1);
		} else if (status == 2) {
			cm.sendNextS("*Sigh* Yup so obvious, you are not hiding. Anyway, may I know how can I go to the Mothership?", 3);
		} else if (status == 3) {
			cm.sendNextS("What, what Mother shit?", 1);
		} else if (status == 4) {
			cm.sendNextS("I mean, the Alien's Mothership.", 3);
		} else if (status == 5) {
			cm.sendNextS("Oh, you mean the square ugly ship. You want to know how to go there huh? Better pay me some moneh!", 1);
		} else if (status == 6) {
			if (cm.haveItem(4031753, 3)) {
				cm.gainItem(4031753, -3);
				cm.updateCQInfo(100008, "paid");
			}
			cm.sendNextS("Here you go, 3 Zeta Residue.", 3);
		} else if (status == 7) {
			cm.sendNextS("Wow I love Zeta Residue! Alright, the ship is guarded by Government now. You can only access to there by getting permit from Lily.", 1);
		} else if (status == 8) {
			cm.sendNextS("Where is Lily then?", 3);
		} else if (status == 9) {
			cm.sendNextS("Lily is located around the Energy Laboratory. You can reach her by entering the portal in right hand side.", 1);
		} else if (status == 10) {
			cm.sendNextS("Alright! Thanks you a lots! You have a nice hair though!", 2);
			cm.dispose();
		} else if (status == 11) {
			cm.sendNext("Congrats! You has completed the given mission. You may go back to your world now. By the way, I don't have hair duhh..");
			cm.dispose();
		} else if (status == 12) { // shouldn't come here
			cm.sendNext("Hurry to the right portal!");
			cm.dispose();
		}
	}
}