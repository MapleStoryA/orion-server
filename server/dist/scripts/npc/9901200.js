var status = 0;
var sel;

function start() {
	if (cm.getPlayerStat("OCC") == 0 && cm.isCQActive(100006)) { // started occupation quest, time to explain havent select
		status = -1;
	} else if (cm.isCQFinished(100006)) { // all done here
		status = 98;
	} else { // not yet done any quest..
		status = 99;
	}
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 0 && status == 0) {
		cm.dispose();
		return;
	}
	if (mode == 1) {
		status++;
	} else {
		status--;
	}
	if (status == 0) { // send simple must have selections
		cm.sendSimple("Hellow #e#h ##n,\r\n#b#eOccupation have a range of benefits#k#n! #r#eClick on one to learnt about#k#n..\r\n\r\n#L0#NX Whore#l\r\n#L1#Gamer#l\r\n#L2#Huntsman#l\r\n#L3#Ninja#l\r\n#L4#Vortex#l");
	} else if (status == 1) {
		sel = selection;
		if (selection == 0) { //NXWhore
			cm.sendAcceptDecline("#eClick #bAccept#k if you wish to become this occupation...#n\r\n\r\n#e#bOccupation:#n NX Whore#k\r\n#eDescription:#n The ultimate lazy ass of Henesys\r\n#eDefault Rate Changes:#n #r80% on all rates#k\r\n#eAdditional Passive:#n Boots up everyone's Cash Rate except itself in it's party.\r\n#eMethod of Gaining Occupation EXP:#n Every 15 minute spent in Henesys Town, Henesys Game Park(Casino) will give you some EXP.\r\n\r\n#e#bSkills:#n#k\r\n\r\nLevel  1 : Gain 1,333,337 cash (cooldown 1 hours)\r\nLevel 2 : Gain @heal <name> commands, cooldown 15minutes\r\nLevel 3 : @heal <name> commands, cooldown reduce to 10 minutes\r\nLevel 4 : @heal <name> commands, cooldown reduce to 5minutes\r\nLevel 5 : @heal <name> commands, cooldown reduce to 1minutes\r\nLevel 6 :  Gain @bomb commands,  cooldown 20 minutes\r\nLevel 7 : @bomb commands, cooldown reduce to 10 minutes\r\nLevel 8 : @sex <name>commands, cooldown 180 minutes\r\nLevel 9 :  @sex <name> commands, cooldown reduce to 150 minutes\r\nLevel 10 : @kill <name>, cooldown 360minutes");
		} else if (selection == 1) { //Gamer
			cm.sendAcceptDecline("#eClick #bAccept#k if you wish to become this occupation...#n\r\n\r\n#e#bOccupation:#n Gamer#k\r\n#eDescription:#n The ultimate madness player\r\n#eDefault Rate Changes:#n #r100% on all rates#k\r\n#eAdditional Passive:#n Boots up everyone's EXP Rate except itself in it's party.\r\n#eMethod of Gaining Occupation EXP:#n Find your Occupation NPC by using @occupation in the corresponding rebirth and do the quest to level up your Occupation.\r\n\r\n#e#bSkills:#n#k\r\n\r\nLevel  1 : +50x EXP rate\r\nLevel  2 : +50x EXP rate\r\nLevel  3 : +50x EXP rate\r\nLevel  4 : +50x EXP rate\r\nLevel  5 : +50x EXP rate\r\nLevel  6 : +50x EXP rate\r\nLevel  7 : +50x EXP rate\r\nLevel  8 : +50x EXP rate\r\nLevel  9 : +50x EXP rate\r\nLevel  10 : +50x EXP rate");
		} else if (selection == 2) { //Huntsman
			cm.sendAcceptDecline("#eClick #bAccept#k if you wish to become this occupation...#n\r\n\r\n#e#bOccupation:#n Huntsman#k\r\n#eDescription:#n The ultimate madness hunter\r\n#eDefault Rate Changes:#n #r30% EXP Rate, 100% on others.#k\r\n#eAdditional Passive:#n Boots up everyone's Drop Rate except itself in it's party.\r\n#eMethod of Gaining Occupation EXP:#n Find your Occupation NPC by using @occupation in the corresponding rebirth and do the quest to level up your Occupation.\r\n\r\n#e#bSkills:#n#k\r\n\r\nLevel 1 : Mob drop extra 1 more etc = total 2 etc drop from mob. Success rate 30%\r\nLevel 2 : Mob drop extra 1 more etc = total 2 etc drop from mob. Success rate 100%\r\nLevel 3 : Mob drop extra 2 more etc = total 3 etc drop from mob. Success rate 30%\r\nLevel 4 : Mob drop extra 2 more etc = total 3 etc drop from mob. Success rate 100%\r\nLevel 5 : Mob drop extra 3 more etc = total 4 etc drop from mob. Success Rate 30%\r\nLevel 6 : Mob drop extra 3 more etc = total 4 etc drop from mob. Success Rate 100%\r\nLevel 7 : Mob drop extra 4 more etc = total 5 etc drop from mob. Success Rate 30%\r\nLevel 8 : Mob drop extra 4 more etc = total 5 etc drop from mob. Success Rate 100%\r\nLevel 9 : Mob drop extra 5 more etc = total 6 etc drop from mob. Success Rate 30%\r\nLevel 10 : Mob drop extra 5 more etc = total 6 etc drop from mob. Success Rate 100%,  ExpRate+ 100x");
		} else if (selection == 3) { //Ninja
			cm.sendAcceptDecline("#eClick #bAccept#k if you wish to become this occupation...#n\r\n\r\n#e#bOccupation:#n Ninja#k\r\n#eDescription:#n The ultimate assaulter\r\n#eDefault Rate Changes:#n #r100% on all rates#k\r\n#eMethod of Gaining Occupation EXP:#n Every monster killed gain 1exp in 40% chance.\r\n\r\n#e#bSkills:#n#k\r\n\r\nLevel 1 : @clone, 100% to summon 1st  clone, clone damage = 25%\r\nLevel 2 : @clone, 100% to summon 1st  clone, clone damage = 35%\r\nLevel 3 : @clone, 100% to summon 1st  clone, 70% to get 2nd clone, clone damage = 35%\r\nLevel 4 : @clone, 100% to summon 1st clone, 70% to get 2nd clone, clone damage = 40%\r\nLevel 5 : @clone, 100% to summon 1st clone, 80% to get 2nd clone, 70% to get 3rd clone, clone damage = 40%\r\nLevel 6 : @clone, 100% to summon 1st clone, 80% to get 2nd clone, 70% to get 3rd clone, clone damage = 50%\r\nLevel 7 : @clone, 100% to summon 1st clone, 90% to get 2nd clone, 80% to get 3rd clone, 60% to get 4th clone, clone damage = 50%\r\nLevel 8 : @clone, 100% to summon 1st clone, 90% to get 2nd clone, 80% to get 3rd clone, 60% to get 4th clone, clone damage = 55%\r\nLevel 9 : @clone, 100% to summon 1st clone, 100% to get 2nd clone, 90% to get 3rd clone, 70% to get 4th clone, 60% to get 5th clone, clone damage = 55%\r\nLevel 10 : @clone, 100% to summon 1st clone, 100% to get 2nd clone, 90% to get 3rd clone, 70% to get 4th clone, 60% to get 5th clone, clone damage = 55%\r\n@clone = 1hour cool down.");
		} else if (selection == 4) { //Vortex
			cm.sendAcceptDecline("#eClick #bAccept#k if you wish to become this occupation...#n\r\n\r\n#e#bOccupation:#n Vortex#k\r\n#eDescription:#n The ultimate vacuum\r\n#eDefault Rate Changes:#n #r60% EXP Rate, 100% on others.#k\r\n#eMethod of Gaining Occupation EXP:#n Loot items to increase occupation EXP.\r\n\r\n#e#bSkills:#n#k\r\n\r\nLevel 1 : 2% loot from whole screen.\r\nLevel 2 : 3% loot from whole screen.\r\nLevel 3 : 4% loot from whole screen.\r\nLevel 4 : 5% loot from whole screen.\r\nLevel 5 : 6% loot from whole screen.\r\nLevel 6 : 7% loot from whole screen.\r\nLevel 7 : 8% loot from whole screen.\r\nLevel 8 : 9% loot from whole screen.\r\nLevel 9 : 9.5% loot from whole screen.\r\nLevel 10 : 10% loot from whole screen.");
		}
		} else if (status == 2) {
			cm.sendYesNo("Are you #r#eseriously sure#k#n you want to select your occupation as a #b#e" + (sel == 0 ? "NxWhore" : (sel == 1 ? "Gamer" : (sel == 2 ? "Huntsman" : (sel == 3 ? "Ninja" : "Vortex")))) + "#k#n?\r\n\r\n#eThis option will greatly dictate your future gameplay!#n");
	} else if (status == 3) {
		if (sel == 0) {
			cm.getPlayer().changeOccupation(501); //NxWhore
			cm.sendOk("#eCongratulations! Thanks for all your effort in helping to save the MapleWorld!#n\r\n\r\nHave fun and enjoy with my occupation!");
			cm.updateCQInfo(100006, "");
			cm.completeCQ(100006);
			cm.dispose();
		} else if (sel == 1) {
			cm.getPlayer().changeOccupation(201); //Gamer
			cm.sendOk("#eCongratulations! Thanks for all your effort in helping to save the MapleWorld!#n\r\n\r\nHave fun and enjoy with my occupation!");
			cm.updateCQInfo(100006, "");
			cm.completeCQ(100006);
			cm.dispose();
		} else if (sel == 2) {
			cm.getPlayer().changeOccupation(301); //Huntsman
			cm.sendOk("#eCongratulations! Thanks for all your effort in helping to save the MapleWorld!#n\r\n\r\nHave fun and enjoy with my occupation!");
			cm.updateCQInfo(100006, "");
			cm.completeCQ(100006);
			cm.dispose();
		} else if (sel == 3) {
			cm.getPlayer().changeOccupation(101); //Ninja
			cm.sendOk("#eCongratulations! Thanks for all your effort in helping to save the MapleWorld!#n\r\n\r\nHave fun and enjoy with my occupation!");
			cm.updateCQInfo(100006, "");
			cm.completeCQ(100006);
			cm.dispose();
		} else if (sel == 4) {
			cm.getPlayer().changeOccupation(401); //Vortex
			cm.sendOk("#eCongratulations! Thanks for all your effort in helping to save the MapleWorld!#n\r\n\r\nHave fun and enjoy with my occupation!");
			cm.updateCQInfo(100006, "");
			cm.completeCQ(100006);
			cm.dispose();
		}
	} else if (status == 98) { // Already selected occ
		cm.sendOk("You cannot change your occupation! The tube only work once.");
		cm.dispose();
	} else { // Not yet done any quest
		cm.sendOk("The laboratory tube is not yet been repaired. Please come back later."); //not yet finish the asssstant quets
		cm.dispose();
	}
}
