var wui = 0;
	
	function start() {
	    cm.sendSimple ("#L0#Can I leave please?#l\r\n#L1#Sorry to have bothered you.#l");
	}
	
	function action(mode, type, selection) {
	    cm.dispose();
	    if (selection == 0) {
	            cm.warp(680000000);
	    }  else if (selection == 1) {
			cm.dispose();
			}
}