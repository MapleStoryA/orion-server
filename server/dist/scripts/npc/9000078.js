var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	var period = 1;
	if (mode != 1) {
		cm.dispose();
	} else {
		status++;
		if(status == 0){
			cm.sendSimple("Welcome to Golden Temple! I can issue you a Golden Ticket.\r\n\r\n#b#L0#Golden Ticket for 500,000 meso (one time use)#l\r\n#L1#Premium Golden Ticket for 999,999 meso#l");
		} else if (status == 1) {
			if (selection == 0) {
				if (cm.getMeso() < 500000) {
					cm.sendOk("You do not have enough meso.");
				} else if (!cm.canHold(4001431) || cm.haveItem(4001431)) {
					cm.sendOk("Either you have this already or can't hold it.");
				} else {
					cm.gainMeso(-500000);
					cm.gainItem(4001431, 1, period);
					cm.sendOk("Thank you.");
				}
			}
			else if(selection == 2){
				if (cm.getMeso() < 999999) {
					cm.sendOk("You do not have enough meso.");
				} else if (!cm.canHold(4001431)) {
					cm.sendOk("Either you have this already or can't hold it.");
				} else {
					cm.gainMeso(-999999);
					cm.createItem(4001433, 30);
					cm.sendOk("Thank you.");
				}
			}
			else {
				if (cm.getMeso() < 999999) {
					cm.sendOk("You do not have enough meso.");
				} else if (!cm.canHold(4001432) || cm.haveItem(4001432)) {
					cm.sendOk("Either you have this already or can't hold it.");
				} else {
					cm.gainMeso(-999999);
					cm.gainItem(4001432, 1, period);
					cm.sendOk("Thank you.");
				}
			}
			cm.dispose();
		}
	}
}