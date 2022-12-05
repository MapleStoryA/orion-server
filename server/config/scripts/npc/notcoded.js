function action(mode, type, selection) {
	if (cm.getNpc() >= 9901000) {
		cm.sendNext("Hello #h0#, I am in the Hall of Fame for reaching LEVEL 200.");
	} else {
		cm.sendOk("Hello, #h0##n.");
	}
	cm.safeDispose();
}