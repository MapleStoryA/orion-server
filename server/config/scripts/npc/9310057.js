var rewards = Array(2000005, 1012067, 1012066, 1012064, 1012065, 1012068, 1012069, 1140000, 1141000);
var expires = Array(-1, 30, 30, 30, 30, 30, 30, 60, 60);
var quantity = Array(3, 1, 1, 1, 1, 1, 1, 1, 1);
var needed = Array(10, 20, 20, 20, 20, 35, 35, 50, 50);
var gender = Array(2, 2, 2, 2, 2, 2, 2, 0, 1);
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	    cm.dispose();
	}
	status--;
    }
    if (status == 0) {
        for (var i = 3994059; i < 3994085; i++) {
	    cm.givePartyItems(i, 0, true);
	}
    }
    switch(cm.getPlayer().getMapId()) {
	case 520000000: //event map
    	    if (status == 0) {
	        cm.sendSimple("Hello~I am Dr.P of #bEnglish School!\r\n\r\n#L0#Go to English School#l\r\n#L1#I wish to exchange Obedient Child Medal#l\r\n#L2#What is English School?#l");
    	    } else if (status == 1) {
	        if (selection == 0) {
		    cm.warp(702090400,0); //exit map lobby
		    cm.dispose();
		} else if (selection == 1) {
		    var selStr = "Maybe you could trade me some...\r\n\r\n#b";
		    for (var i = 0; i < rewards.length; i++) {
			selStr += "#L" + i + "##v" + rewards[i] + "##t" + rewards[i] + "# x " + quantity[i] + " #r(" + needed[i] + " Medals)#b#l\r\n";
		    }
		    cm.sendSimple(selStr);
		} else if (selection == 2) {
		    cm.sendNext("English School is a Party Quest for 1-5 party members. There are 3 different quiz levels, Beginner, Intermediate and Advanced. Within the 10 minutes time limit, collect all the required alphabets to form the correct answers to the questions given! Give the letters to your party leader to pass to #r#eDavid#n.#k You can continue on with the quiz or redeem rewards within the time limit. The questions and rewards will be random. Each time one is answered correctly, you will receive a random reward and a Obedient Child Medal. Collect and exchange Obedient Child Medal!");
		    cm.dispose();
		}
	    } else if (status == 2) {
	        if (!cm.haveItem(4001137, needed[selection])) {
		    cm.sendNext("You don't have the needed medals.");
		} else if (!cm.canHold(rewards[selection], 1)) {
		    cm.sendNext("Please make the inventory space.");
		} else if (gender[selection] != 2 && gender[selection] != cm.getPlayer().getGender()) {
		    cm.sendNext("You are not the correct gender to receive this.");	
		} else {
		    cm.gainItem(4001137, -needed[selection]);
		    if (expires[selection] > 0) {
			cm.gainItemPeriod(rewards[selection], quantity[selection], expires[selection]);
		    } else {
			cm.gainItem(rewards[selection], quantity[selection]);
		    }
		}
		cm.dispose();
            }
	    break;
	case 702090400:
    	    if (status == 0) {
	        cm.sendSimple("Hello~I am Dr.P of #bEnglish School!\r\n\r\n#L0#Go to English School - Easy#l\r\n#L1#Go to English School - Medium#l\r\n#L2#Go to English School - Hard#l\r\n#L3#Return to Event Map#l");
    	    } else if (status == 1) {
	        if (selection == 0 || selection == 1 || selection == 2) {
   		    var em = cm.getEventManager("English");
    		    if (em == null) {
			cm.sendOk("Please try again later.");
			cm.dispose();
			return;
    		    }
		    if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
			cm.sendOk("The leader of the party must be here.");
		    } else {
			var party = cm.getPlayer().getParty().getMembers();
			var mapId = cm.getPlayer().getMapId();
			var next = true;
			var size = 0;
			var it = party.iterator();
			while (it.hasNext()) {
				var cPlayer = it.next();
				var ccPlayer = cm.getPlayer().getMap().getCharacterById(cPlayer.getId());
				if (ccPlayer == null) {
					next = false;
					break;
				}
				size++;
			}	
			if (next && size >= 1) {
		    		if (em.getInstance("English" + selection) == null) {
					em.startInstance_Party("" + selection, cm.getPlayer());
		    		} else {
					cm.sendOk("Another party quest has already entered this channel.");
		    		}
			} else {
				cm.sendOk("All members of your party must be here.");
			}
		    }
		} else if (selection == 3) {
		    cm.warp(520000000,0);
		}
	        cm.dispose();
            }
	    break;
    }
}