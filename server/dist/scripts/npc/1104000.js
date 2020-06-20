var status = -1;

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if(!cm.getPlayer().isAran()){
		cm.sendNext("You are not suppost to be here.");
		cm.dispose();
		return;
	}
	var prop = cm.getPlayer().get("francisOnMap");
	if(prop === "true"){
		cm.sendNext("...");
		cm.dispose();
		return;
	}
	
	if(status == -1){
		cm.sendNext("You again! How in the world did you get in? I thought i warned you not to stand in my way!");
		status = 1;
		return;
	}
	if(status == 1){
		cm.PlayerToNpc("What exactly are you trying to do? Why are you controlling these monsters? Tell me what the black Wings are up to!");
		status = 2;
		return;
	}
	if(status == 2){
		cm.getPlayer().set("francisOnMap", "true");
		cm.sendNext("Hmm, I don't have to tell you anything! Now prepare to die!");
		status = 3;
		cm.spawnMonster(9300344, 1, new java.awt.Point(540, 245));
		cm.dispose();
		return;
	}
}