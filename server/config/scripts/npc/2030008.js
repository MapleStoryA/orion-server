/* Adobis
 * 
 * El Nath: The Door to Zakum (211042300)
 * 
 * Zakum Quest NPC 
 
 * Custom Quest 100200 = whether you can do Zakum
 * Custom Quest 100201 = Collecting Gold Teeth <- indicates it's been started
 * Custom Quest 100203 = Collecting Gold Teeth <- indicates it's finished
 * Quest 7000 - Indicates if you've cleared first stage / fail
 * 4031061 = Piece of Fire Ore - stage 1 reward
 * 4031062 = Breath of Fire    - stage 2 reward
 * 4001017 = Eye of Fire       - stage 3 reward
 * 4000082 = Zombie's Gold Tooth (stage 3 req)
*/

var status;
var mapId = 211042300;
var stage;
var teethmode;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
	cm.removeAll(4001015);
	cm.removeAll(4001016);
	cm.removeAll(4001018);
    if (status == 0) {
	if (cm.getPlayerStat("LVL") >= 50) {
	    if (cm.getQuestStatus(100200) != 2 && cm.getQuestStatus(100200) != 1) {
		cm.forceStartQuest(100200);
		cm.sendOk("You want to be permitted to do the Zakum Dungeon Quest?  Well, I, #bAdobis#k... judge you to be suitable.  You should be safe roaming around the dungeon.  Just be careful...");
		cm.dispose();
		return;
	    } else if (cm.getQuestStatus(100201) == 1) {
		// if they have gold teeth and the other items, they are good to go
		teethmode = 1;
		cm.sendNext("Have you got the items I asked for?  This ain't no charity.");
	    } else {
		if (cm.haveItem(4001109)) {
		    cm.sendSimple("Well... alright. You seem more than qualified for this.\n\rWhich one of these tasks do you want to tackle on? #b\r\n#L0#Explore the Dead Mine. (Level 1)#l\r\n#L1#Observe the Zakum Dungeon. (Level 2)#l\r\n#L2#Request for a refinery. (Level 3)#l\r\n#L3#Enter the center of Lava.#l\r\n#L4#Skip the Quest.#l");
		} else {
		    cm.sendSimple("Well... alright. You seem more than qualified for this.\n\rWhich one of these tasks do you want to tackle on? #b\r\n#L0#Explore the Dead Mine. (Level 1)#l\r\n#L1#Observe the Zakum Dungeon. (Level 2)#l\r\n#L2#Request for a refinery. (Level 3)#l\r\n#L4#Skip the Quest.#l");
		}
	    }
	    if (cm.getQuestStatus(100201) == 2) { // They're done the quests
		teethmode = 2;
	    }
	} else {
	    cm.sendOk("Please come back to me when you've become stronger.  I've seen a few adventurers in my day, and you're far too weak to complete my tasks.");
	    cm.dispose();
	}
    } else if (status == 1) {
	//quest is good to go.
	// if they're working on this quest, he checks for items.
	if (teethmode == 1) {
	    // check for items
	    if (cm.haveItem(4031061,1) && cm.haveItem(4031062,1) && cm.haveItem(4000082,30)) { // take away items, give eyes of fire, complete quest
		if (cm.canHold(4001017)) {
		    cm.gainItem(4031061, -1);
		    cm.gainItem(4031062, -1);
		    cm.gainItem(4000082, -30);
		    cm.gainItem(4001017, 5);
		    cm.sendNext("Here it is. You will now be able to enter the alter of the Zakum Dungeon when the door on the left is open.. You'll need #b#t4001017##k with you in order to go through the door and enter the stage. Now, let's see how many can enter this place ...?");
		    cm.completeQuest(100201);
		    cm.completeQuest(100200);
		} else {
		    cm.sendNext("Hmmm... are you sure you have all the items required to make #rEye of Fire#k with you? If so, then please check and see if your etc. inventory is full or not.");
		}
		cm.dispose();
	    } else { // go get more
		cm.sendNext("You shtill didn't get me my teef! Howsh a man shupposhed to conshentrate wifout teef?");
		cm.dispose();
	    }
	    return;
	}
	if (selection == 0) { //ZPQ
	    if (cm.getParty() == null) { //no party
		cm.sendNext("You are not currently in a party right now. You may only tackle this assignment as a party.");
		cm.safeDispose();
		return;
	    }
	    else if (!cm.isLeader()) { //not party leader
		cm.sendNext("Please have the leader of your party speak with me.");
		cm.safeDispose();
		return;
	    }
	    else {
		//check each party member, make sure they're above 50 and still in the door map
		//TODO: add zakum variable to characters, check that instead; less hassle
		var party = cm.getParty().getMembers();
		mapId = cm.getMapId();
		var next = true;

		for (var i = 0; i < party.size(); i++) {
		    if ((party.get(i).getLevel() < 50) || (party.get(i).getMapid() != mapId)) {
			next = false;
		    }
		}

		if (next) {
		    //all requirements met, make an instance and start it up
		    var em = cm.getEventManager("ZakumPQ");
		    if (em == null) {
			cm.sendOk("I can't let you in for unknown reasons. Please try again later.");
		    } else {
			var prop = em.getProperty("started");
			if (prop.equals("false") || prop == null) {
			    em.startInstance(cm.getParty(), cm.getMap());
			} else {
			    cm.sendOk("An another party has already started this quest. Please try again later.");
			}
		    }
		    cm.dispose();
		} else {
		    cm.sendNext("Please make sure all of your members are qualified to begin my trials...");
		    cm.dispose();
		}
	    }
	} else if (selection == 1) { //Zakum Jump Quest
	    stage = 1;
	    if (cm.haveItem(4031061) && !cm.haveItem(4031062)) {
		// good to go
		cm.sendYesNo("You have safely cleared the 1st stage. There's still a long way to go before meeting the boss of Zakum Dungeon, however. So, what do you think? Are you ready to move on to the next stage?");
	    } else {
		if (cm.haveItem(4031062))
		    cm.sendNext("You've already got the #bBreath of Lava#k, you don't need to do this stage.");
		else
		    cm.sendNext("It doesn't look like you have cleared the previous stage, yet. Please beat the previous stage before moving onto the next level.");
		cm.dispose();
	    }
	} else if (selection == 2) { //Golden Tooth Collection
	    stage = 2;
	    if (teethmode == 2 && cm.haveItem(4031061) && cm.haveItem(4031062)) {
		// Already done it once, they want more
		cm.sendYesNo("If you want more #bEyes of Fire#k, you need to bring me the same #b30 Zombie's Lost Gold Tooth#k.  Turns out gold dentures don't last long, and I need a new one.\r\nDo you have those teeth for me?");
	    } else if (cm.haveItem(4031061) && cm.haveItem(4031062)) {
		// check if quest is complete, if so reset it (NOT COMPLETE)
		cm.sendYesNo("Okay, you've completed the earlier trials.  Now, with a little hard work I can get you the #bseeds of Zakum#k necessary to enter combat.  But first, my teeths are not as good as they used to be.  You ever seen a dentist in Maple Story?  Well, I heard the Miner Zombies have gold teeth.  I'd like you to collect #b30 Zombie's Lost Gold Tooth#k so I can build myself some dentures.  Then I'll be able to get you the items you desire.\r\nRequired:\r\n#i4000082##b x 30");
				
	    } else {
		cm.sendNext("Please complete the earlier trials before attempting this one.");
		cm.dispose();
	    }
	} else if (selection == 3) { // Enter the center of Lava, quest
	    var dd = cm.getEventManager("FireDemon");
	    if (dd != null && cm.haveItem(4001109)) {
		dd.startInstance(cm.getPlayer());
	    } else {
		cm.sendOk("An unknown error occured.");
	    }
	    cm.dispose();
	} else if (selection == 4) {
	    if (cm.getQuestStatus(100200) == 2) {
		cm.sendOk("You're already done my quests.");
		cm.dispose();
	    } else {
	    	cm.sendYesNo("So, you wish to bribe me? Ha, I do not take them lightly. You will have to pay up #e50,000,000#n mesos for me to let you through!");
		status = 3;
	    }
	}
    } else if (status == 2) {
	if (stage == 1) {
	    cm.warp(280020000, 0); // Breath of Lava I
	    cm.dispose();
	}
	else if (stage == 2) {
	    if (teethmode == 2) {
		if (cm.haveItem(4031061,1) && cm.haveItem(4031062,1) && cm.haveItem(4000082,30)) { // take away items, give eyes of fire, complete quest
		    if (cm.canHold(4001017)) {
			cm.gainItem(4031061, -1);
			cm.gainItem(4031062, -1);
			cm.gainItem(4000082, -30);
			cm.gainItem(4001017, 5);
			cm.sendNext("Here it is. You will now be able to enter the alter of the Zakum Dungeon when the door on the left is open.. You'll need #b#t4001017##k with you in order to go through the door and enter the stage. Now, let's see how many can enter this place ...?");
			cm.completeQuest(100201);
			cm.completeQuest(100200);
		    } else {
			cm.sendNext("Hmmm... are you sure you have all the items required to make #rEye of Fire#k with you? If so, then please check and see if your etc. inventory is full or not.");
		    }
		    cm.dispose();
		} else {
		    cm.sendNext("I don't think you have #b30 Zombie's Lost Gold Teeth#k yet. Gather them all up and I may be able to refine them and make a special item for you ..." );
		    cm.dispose();
		}
	    } else {
		cm.forceStartQuest(100201);
		cm.dispose();
	    }
	}
    } else if (status == 4) { //bribe
	if (cm.getPlayer().getMeso() < 50000000) {
	    cm.sendNext("You do not have enough money.");
	} else if (!cm.canHold(4001017,5)) {
	    cm.sendNext("Please make room in ETC.");
	} else {
	    cm.gainItem(4001017,5);
	    cm.completeQuest(100201);
	    cm.completeQuest(100200);
	    cm.forceCompleteQuest(7000);
	    cm.completeQuest(100203);
	    cm.sendOk("Alright, go through.");
	    cm.gainMeso(-50000000);
	}
	cm.dispose();
    } else {
	cm.dispose();
    }
}