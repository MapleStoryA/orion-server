/**
	Konpei - Near the Hideout(801040000)
*/

function start() {
    action(1, 0, 0);
}

var status = -1;
function action(mode, type, selection) {
	
	if(cm.getPlayer().getMap().getAllMonster().size() > 0){
		cm.sendOk("Defeat him!");
		cm.dispose();
		return;
	}
	if(status === -1){
		cm.sendOk("The latter of Master of Disguise dropped...");
		status = 1;
		return;
	}
	if(status === 1){
		cm.forceStartQuest(21754);
		cm.warp(100000201, 1);
	}
	
	    
	cm.dispose();
}