function start() {
	cm.showEffect(true, 1);
	cm.sendOk("#e#dIt seems that there are writing on this rock and they keep changing... What does this mean? \r\n\r\n#kWhich ranking would you like to check out?");
	cm.dispose();
}

function action(m,t,s) {
	if (m < 1) {
		cm.dispose();
	}
}