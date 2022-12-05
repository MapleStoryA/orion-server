var status = -1;

function action(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		cm.dispose();
		return;
	}
	if (status == 0) {
		cm.sendSimple("#e#rHello "+cm.getName()+" and welcome to the monster bang system. Players can donate zeta residues and after a certain amount of them is reached, monster will spawn everywhere in Henesys.\r\n \n\r #b#L0# Here, I brought the Zeta Residue.#l#k\r\n#b#L1# Please show me the current status on collecting the Zeta Residue.#l#k");
	} else if (status == 1) {
		if (selection == 1) {
			cm.sendNext("#e#r"+cm.getKegs()+"#k collected residue.");
			cm.safeDispose();
		} else if (selection == 0) {
			cm.sendGetNumber("Did you bring the Zeta Residue with you? Then, please give me the #bZeta Residue#k you have.  I will make a nice firecracker.  How many are you willing to give me? \n\r #b< Number of Zeta Residue in inventory : "+cm.getPlayer().getItemQuantity(4031753, false)+" >#k", 0, 0, 1000);
		}
	} else if (status == 2) {
		var num = selection;
		if (num == 0) {
			cm.sendOk("T.T I will need the Zeta Residue to start the monster bang....\r\n Please think again and talk to me.");
		} else if (cm.haveItem(4031753, num)) {
			cm.gainItem(4031753, -num);
			cm.giveKegs(num);
			cm.sendOk("Don't forget to give me the Zeta Residue when you want to participate in the event again.");
		}
		cm.safeDispose();
	}
}