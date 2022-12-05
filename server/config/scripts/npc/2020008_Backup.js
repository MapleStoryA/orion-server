/** 
	Tylus: Warrior 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

var status = 0;
var job;
var skills = Array(21001003, 21000000, 21100002, 21100004, 21100005, 21110002);
//polearm booster, combo ability, polearm mastery, final charge, combo smash, combo drain, full swing

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 1) {
	cm.sendOk("Make up your mind and visit me again.");
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;
    if (status == 0) {
	if (!(cm.getJob() == 110 || cm.getJob() == 120 || cm.getJob() == 130 || cm.getJob() == 2110)) {
	    if (cm.getQuestStatus(6192) == 1) {
		if (cm.getParty() != null) {
		    var ddz = cm.getEventManager("ProtectTylus");
		    if (ddz == null) {
			cm.sendOk("Unknown error occured");
		    } else {
			var prop = ddz.getProperty("state");
			if (prop == null || prop.equals("0")) {
			    ddz.startInstance(cm.getParty(), cm.getMap());
			} else {
			    cm.sendOk("Someone else is already trying to protect Tylus, please try again in a bit.");
			}
		    }
		} else {
		    cm.sendOk("Please form a party in order to protect Tylus!");
		}
	    } else if (cm.getQuestStatus(6192) == 2) {
		cm.sendOk("You have protected me. Thank you. I will teach you stance skill.");
		if (cm.getJob() == 112) {
			if (cm.getPlayer().getMasterLevel(1121002) <= 0) {
				cm.teachSkill(1121002, 0, 10);
			}
		} else if (cm.getJob() == 122) {
			if (cm.getPlayer().getMasterLevel(1221002) <= 0) {
				cm.teachSkill(1221002, 0, 10);
			}
		} else if (cm.getJob() == 132) {
			if (cm.getPlayer().getMasterLevel(1321002) <= 0) {
				cm.teachSkill(1321002, 0, 10);
			}
		}
	    } else {
		cm.sendOk("May #rOdin#k be with you!");
	    }
	    cm.dispose();
	    return;
	}
	if ((cm.getJob() == 110 || cm.getJob() == 120 || cm.getJob() == 130 || cm.getJob() == 2110 ) && cm.getPlayerStat("LVL") >= 70 && cm.getPlayerStat("RSP") <= (cm.getPlayerStat("LVL") - 70) * 3) {
	    cm.sendNext("You are indeed a strong one.");
	} else {
	    cm.sendOk("Please make sure that you have used all your 2nd job skill point before proceeding.");
	    cm.safeDispose();
	}
    } else if (status == 1) {
	    if (cm.getPlayerStat("LVL") >= 70 && cm.getPlayerStat("RSP") <= (cm.getPlayerStat("LVL") - 70) * 3) {
	    if (cm.getJob() == 110) { // FIGHTER
		cm.changeJob(111); // CRUSADER
		cm.gainAp(5);
		cm.gainSp(1);
		cm.sendOk("You are now a #bCrusader#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 120) { // PAGE
		cm.changeJob(121); // WHITEKNIHT
		cm.gainAp(5);
		cm.gainSp(1);
		cm.sendOk("You are now a #bWhite Knight#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 130) { // SPEARMAN
		cm.changeJob(131); // DRAGONKNIGHT
		cm.gainAp(5);
		cm.gainSp(1);
		cm.sendOk("You are now a #bDragon Knight#k. May #rOdin#k be with you!");
		cm.dispose();
	    } else if (cm.getJob() == 2110) { // ARAN
		cm.changeJob(2111); // ARAN
		cm.gainAp(5);
		cm.gainSp(1);
		for (var i = 0; i < skills.length; i++) {
			cm.teachSkill(skills[i], cm.getPlayer().getSkillLevel(skills[i]));
		}
		cm.sendOk("You are now a #bAran#k. May #rOdin#k be with you!");
		cm.dispose();
	    }
	    } else {
		cm.sendOk("Come back when you are level 70 and used SP.");
		cm.dispose();
	    }
    }
}