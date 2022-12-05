
var status = 0;

function start() {
	if (!cm.isCQFinished(100000)) { // Impossible unless that player was warped here
		cm.sendNext("Hey! What are you doing here?");
		cm.dispose();
		return;
	}
	if (cm.getCustomQuestStatus(100001) == 0) {
		status = -1;
	} else if (cm.isCQFinished(100002) && cm.isCQFinished(100003) && !cm.isCQActive(100004) && !cm.isCQFinished(100005)&& !cm.isCQFinished(100006) && !cm.isCQFinished(100007) && !cm.isCQFinished(100008)&& !cm.isCQFinished(100009)) {
		status = 8;
	} else {
		status = 9;
	}
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
		} else {
            status--;
		}
        if (status == 0) {
			cm.sendNextS("ALERT! Who are you and what are you doing here? We do not allow outsiders to be here! Get away from here!", 1);
		} else if (status == 1) {
			cm.sendNextPrevS("Er..Doctor Bing...", 3);
		} else if (status == 2) {
			cm.sendNextPrevS("Doctor Bing what? Hurry and say! I do not have time to talk to you.", 1);
		} else if (status == 3) {
			cm.sendNextPrevS("Doctor Bing brought me here with his shuttle. He asked me to look for him here.", 3);
		} else if (status == 4) {
			cm.sendNextPrevS("Oh I see. Sorry for the misunderstanding just now. I'm really busy I've to do some research regarding the alien invasion that had happened. You know about it right?", 1);
		} else if (status == 5) {
			cm.sendNextPrevS("Yes, he've told me about it. By the way, may I know where is he now?", 3);
		} else if (status == 6) {
			cm.sendNextPrevS("Ahh, he has just gone to make some research around the fossil mineral mine where the aliens had invaded. He was curious why did the aliens came to invade our world and he said that there might be some clues left around there.", 1);
		} else if (status == 7) {
			cm.sendNextPrevS("Is there anyway I could get over to his place there?.", 3);
		} else if (status == 8) {
			cm.startCQ(100001);
			cm.updateCQInfo(100001, "step=01");
			cm.sendNext("Yes, you could ride OS4 Shuttle. It will bring you to where Doctor Bing is.");
			cm.dispose();
		} else if (status == 9) { // Second quest
			cm.sendNext("Welcome back! and thank you for helping Dr.Bing with his research. But there's also a bad news to hear from me. Someone is looking for you in the OSSS Base Laboratory. Please enter the first portal on your right.");
			cm.removeAll(4001458);
			cm.startCQ(100004);
			cm.updateCQInfo(100004, "step=01");
			cm.dispose();
		} else if (status == 10) {
			cm.sendOk("Could you please leave me alone? I'm really busy with my works..");
			cm.dispose();
		}
	}
	
	//	cm.forceCompleteQuest(23007);
		//	cm.warp(310000000, 9);
			
	//	public void sendNextPrevNPC(String text, byte type, int OtherNPC) {
	
			
			//cm.updateInfoQuest(21019, "helper=clear");
	    //cm.showWZEffect("Effect/Direction1.img/aranTutorial/face");
	  //  cm.showWZEffect("Effect/Direction1.img/aranTutorial/ClickLirin");
	   // cm.playerSummonHint(true);
	  //  cm.dispose();
	//	}
		//	cm.PlayerToNpc("I think I may be able to break the barrier using #t2430014#.");
		//	cm.dispose();
	//	}
	/*}if(status == 1){
		if(cm.isQuestActive(2314)){
			cm.ShowWZEffect("Effect/OnUserEff.img/normalEffect/mushroomcastle/chatBalloon1");
			cm.forceCompleteQuest(2314);
			cm.dispose();
		} else {
			cm.playerMessage("Please return to the Minister of Home Affairs and report results.");
		}
	}*/
}
			