var status = 0;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	//cm.debug("Mode: " + mode + " Type: " + type + " Selection: " + selection);
	var mobCount = cm.getMap().getAllMonstersThreadsafe().size();
	var property = cm.getPlayer().getTemporaryData("KILL_DARK_LORD");
	if(mobCount > 0){
		cm.sendOk("... Protect the diary.");
		cm.dispose();
	}
	else if(mobCount == 0 && property != null && property.equals("1")){
		cm.sendNext("You've obtained the Former Dark Lord's Diary. You better leave before someone comes in.");
		cm.getPlayer().removeTemporaryData("KILL_DARK_LORD");
		cm.gainItem(4032617, 1);
		cm.dispose();
		return;
	}
	if(mode == 0 && selection == -1){
		cm.dispose();
		return;
	}
	if(mode == 1){
		status++;
	}else{
		status--;
	}
	if(status == 0){
		cm.sendYesNo("You see a dust-covered diary. Do you wish to take the diary?");
		return;
	}else{
		var x = -36, y = 149;
		var mob = 9001019;
		
		if(mobCount <= 0){
			cm.spawnMobWithCustomHP(mob, 10, new java.awt.Point(x, y), 1000, 1000);
			cm.getPlayer().addTemporaryData("KILL_DARK_LORD", "1");
		}
		
	}
	cm.dispose();
}	


