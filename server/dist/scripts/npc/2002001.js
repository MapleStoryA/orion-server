
var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		cm.sendOk("Hmm ... finalmente e natal!");
		cm.dispose();
			
	}
}