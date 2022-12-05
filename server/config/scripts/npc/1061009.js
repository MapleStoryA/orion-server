/* Door of Dimension
	Enter 3rd job event
*/
importPackage(Packages.client);



function start() {
	if (cm.getQuestStatus(100101) == 1 && !cm.haveItem(4031059)) {
		var varMapId = 108010400;
		var firstMap = cm.getMap(varMapId);
		var secondMap = cm.getMap(varMapId + 1) ;
		if(firstMap.characterSize() > 0 || secondMap.characterSize() > 0){
			cm.sendOk("Sorry, but someone is already inside.");
			cm.dispose();
			return;
		}
		cm.warpMap(varMapId, 0);
		var startTime = 20 * 60 - 1;
		cm.sendClock(startTime);
		cm.getPlayer().addTemporaryData("3thJobTimer", java.lang.System.currentTimeMillis());
	} else {
		cm.sendOk("...");
	}
	cm.dispose();
}

function action(mode, type, selection) {

}