importPackage(Packages.client);

function start() {
	cm.sendYesNo("You can use the Sparkling Crystal to go back to the real world. Are you sure you want to go back?");
}

function action(mode, type, selection) {
	if (mode == 1) {
		var map = cm.getMapId();
		var tomap;

		if(ThirdJobUtils.isBowmanSecondJob(cm.getPlayer())){
			tomap = 100000000;
		}
		else if(ThirdJobUtils.isSecondJobWarrior(cm.getPlayer())){
			tomap = 102000000;
		}
		else if(ThirdJobUtils.isClassicThiefSecondJob(cm.getPlayer()) || cm.getJobId() == 432){
			tomap = 103000000;
		}
		else if(ThirdJobUtils.isSecondJobMage(cm.getPlayer())){
			tomap = 101000000;
		}
		else if(ThirdJobUtils.isPirateSecondJob(cm.getPlayer())){
			tomap = 120000000;
		}
		
		if (cm.haveItem(4031059)) {
			cm.completeQuest(100101);
		}

		cm.warp(tomap);
	}
	cm.dispose();
}
