function start() {
    status = -1;
    cm.sendNext("So I can see that you saved Ludibrium.");
	action(1, 0, 0);
}

function action(mode, type, selection) {
	status++;
	
	if(status === 1){
		if(!cm.isQuestActive(7104)){
			cm.startQuest(7104);
		}else{
			cm.sendNext("You'll have to collect 3 Pieces of Cracked dimension parts If you want a complete one. You should delivery it to Flo, he know what to do.. ");
		}
		cm.dispose();
		return;
	}
	
	if(status == 2){
		cm.dispose();
	}
}