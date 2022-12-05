function levelMenu(){
	if(target.nLevel() > 120){
		self.say("Hey.. I think you are already reached the max level I can help you with.");
		return;
	}
	let ret = self.askYesNo("Are you ready to gain some exp and help us testing the server?");
	if(ret == 0){
		self.say("Ok, you can try it later...");
		return;
	}
	target.incEXP(99999999, true);
	self.say("Report any bugs on our discord channel!");
}

let menu = self.askMenu("What you want to do?\r\n#b#L0# Get a free level #l#k\r\n#b#L1# Get a free gachapon ticket#l#k\r\n#b#L2# Gain mesos");
switch(menu){
	case 0:
		levelMenu();
		break;
	case 1:
		let val = target.get("GACHA_TEST_");
		inventory.exchange(0, 5220000, 1);
		target.set("GACHA_TEST_", "1");
		self.say("Enjoy your ticket!");
		break;
	case 2:
		let val2 = target.get("MESOS_TEST");
		target.incMoney(1000000, true);
		target.set("MESOS_TEST", "1");
	break;	
}


 
