var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode != 1) {
		cm.dispose();
	} else {
		status++;
		if(status == 0){
			cm.sendNext("Hi There~ I'm Armi. It's the #b Maple Leaf event right now!#k right now, do you wish to plant the Maple Tree with me? With the Sunlight from #bMaple Leaf's#k it can allow the tree to grow healthy! Please gather all the Maple Leaf's that you get from the monsters...");
		} else if (status == 1) {
			cm.sendSimple("Each time users collect the required Maple Leaf's, we can set the tree to grow to its maximum!\r\n#b#L0#Here, I brought the Maple Leaf's.#l#k\r\n#b#L1# Please show me the current status on collecting the Maple Leaf's.#l#k");
		} else if (status == 2) {
			if (selection == 0) {
				cm.sendGetNumber("Did you bring the Maple Leaf's with you? Then please give me the #bMaple Leaf's#k you have. I will make a nice firecracker. How many are you willing to give me?", cm.itemQuantity(4001126), 0, cm.itemQuantity(4001126));
			} else {
				cm.sendOk("Status of the tree's growth\r\n" + cm.getSunshines() + "\r\nIf we collect them all, the tree would grow to it's fullest.");
				cm.dispose();
			}
		} else if (status == 3) {
			if (selection < 0 || selection > cm.itemQuantity(4001126)) {
				selection = cm.itemQuantity(4001126);
			}
			if (selection == 0) {
				cm.sendOk("Please come back with some Maple Leaf's.");
			} else {
				cm.addSunshines(selection);
				cm.gainItem(4001126, -selection);
				cm.sendOk("Thank you for the Maple Leaf's.");
			}
			cm.dispose();
		}
	}
}