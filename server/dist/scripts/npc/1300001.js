var status = -1;
var monsterid = 3300008;
var mapProperty = "3300008"
function start() {
	
   action(1, 1, 0);
}
     
function action(m, t, s) {
	var map = cm.getPlayer().getMap();
	var summoned = map.getProperty(mapProperty);
	if(cm.getLevel()> 40){
		cm.sendOk("You should not be here!");
		cm.dispose();
	}
	if(cm.haveMonster(monsterid)){
		cm.sendOk("Kill him!");
		cm.dispose();
	}
	if((cm.haveItem(4001318) || summoned != null) && status != 2){
		cm.sendNext("You saved the princess and there's peace in the kingdom! Thank you so much!");
		status = 2;
		return;
		
	}
	
	if(status == -1){
		 cm.sendNext("Are you ready to face the prime minister?");
		 status = 0;
		 return;
	}
	if(status == 0){
		cm.sendNext("As you wish...");
		cm.showInstruction("I will defeat it and steal her for me... ", 100, 50);
		status = 1;
		return;
	}
	if(status == 1){
		map.addProperty(mapProperty, "1");
		cm.spawnMonster(monsterid);
		cm.ShowWZEffect("Effect/Direction2.img/effect/open/violeta1");
		cm.dispose();
	}
	if(status == 2){
		map.removeProperty(mapProperty);
		cm.warp(106021400);
		
	}
	
	cm.dispose();
	
	
}
