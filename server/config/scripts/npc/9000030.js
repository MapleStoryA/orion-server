var status = -1;

function start(){
	
	action(1, 0, 0);
}

function action(mode, type, selection) {

	if(status === -1){
		cm.sendNext("Hi #h0##n, did you vote today?");
		status = 1;
		return;
	}
	if(status === 1){
		cm.sendNext("Did you know you can vote and earn Maple Points?");
		status = 2;
		return;
	}
	if(status === 2){
		cm.sendYesNo("Would you like to go vote now?");
		status = 3;
		return;
	}
	if(status == 3 && mode == 1){
		cm.sendOk("Thank you for voting!");
		status = 4;
		return;
	}
	if(status == 4 && mode == 1){
		cm.openVoteWebpage();
	}
	
	
	
	cm.dispose();
}