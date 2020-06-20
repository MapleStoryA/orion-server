function enter(pi) {
	var field = pi.getPlayer().getFieldSet("Party1");
	var map = pi.getPlayer().getMapId();
	if(field.getVar("stage") == "3"){
		pi.warp(map + 1, "st00");
	}else{
		pi.playerMessage("The warp is currently unavailable.");
	}
}