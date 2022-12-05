
var status = 99; // incase..

function start() {
	if (cm.getPlayer().getMapId() == 502010300) {
		if (cm.getMorphState() != 2210063) {
			cm.getPlayer().cancelMorphs(true);
			cm.warp(502010300, 0); // re-warp
			cm.dispose();
			return;
		}
		if (cm.getCQInfo(100001).contains("step=02")) {
			status = -1;
		} else if (cm.isCQActive(100002)) {
			status = 4;
		} else if (cm.isCQActive(100003)) {
			status = 5;
		} else {
			status = 6;
		}
	} else if (cm.getPlayer().getMapId() == 502010030) {
		if (cm.isCQActive(100009)) {
			status = 14;
		} else if (cm.isCQActive(100008)) {
			if (cm.getCQInfo(100008).contains("future")) { // clicked
				status = 12;
			} else {
				status = 13;
			}
		} else if (cm.isCQActive(100007)) {
			if (!cm.getCQInfo(100007).contains("timeMachine") && !cm.getCQInfo(100007).contains("done")) { // not yet talked
				status = 9; // next part
			} else if (cm.getCQInfo(100007).contains("done")) { // collected
				status = 10;
			} else {
				status = 11; // talked before
			}
		} else {
			status = 100;
		}
	} else {
		status = 99;
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
			cm.sendNextS("Ahh... you are finally here! I've been waiting for you all day long!", 1);
		} else if (status == 1) {
			cm.sendNextPrevS("Oh sorry! I'm a little late I guess? Anyway, what are you doing here?", 3);
		} else if (status == 2) {
			cm.sendNextPrevS("I was researching about the aliens' invasion in this map to see if there are any clues left by them. By the way, could you help me to do some stuffs so that I could finish my work and get back to the base earlier?", 1);
		} else if (status == 3) {
			cm.sendNextPrevS("Sure Doctor Bing! What do you need me to help with?", 3);
		} else if (status == 4) {
			cm.updateCQInfo(100001, "");
			if (cm.completeCQ(100001)) {
				cm.gainExp(34);
			}
			cm.startCQ(100002); // 15 snail shell + 30 snail
			cm.sendNext("I need you to eliminate 30 Snails and bring me back 15 Snail Shells after that. Good Luck!");
			cm.dispose();
		} else if (status == 5) {
			if (!cm.canHold(2000013, 20) || !cm.canHold(2000014, 20)) {
				cm.sendOk("Insufficient inventory slot");
				cm.dispose();
				return;
			}
			if (cm.completeCQ(100002)) {
				cm.sendOk("Thank you very much. You've eliminated 30 Snails for me and obtained 15 Snail Shells. Oh and I've forgotten about this! Saw the crystals there? I need you to hit those crystals and obtain #r5 Crystanol Fragments#k for me. Anyway, Here's some reward for you for helping me!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#i2000013# #t2000013# x 20\r\n#i2000014# #t2000014# x 20\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 40 exp\r\n");
				cm.gainItem(2000013, 20);
				cm.gainItem(2000014, 20);
				cm.gainExp(40);
				cm.startCQ(100003);
			} else {
				cm.sendOk("Hmmm... Defeat #r30 Snails#k and obtain #r15 Snail Shells, then come back to me again");
			}
			cm.dispose();
		} else if (status == 6) { // second quest
			if (cm.completeCQ(100003)) {
				cm.sendOk("You brought them all here! Thank you thank you! I shall start studying these samples carefully. I guess your work here is done. Could you please get back to the base and help my fellow friends in the laboratory? Oh, I almost forgot to reward you for your hard work.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#i1302000# #t1302000#\r\n#i1302000# #t1302000#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 100 exp\r\n");
				cm.gainExp(100);
				cm.removeAll(4001458);
			} else {
				cm.sendOk("I don't think you have collected all the items I requested. These are the ones you'll need: #b5 Crystanol Fragments#k.");
			}
			cm.dispose();	
		} else if (status == 7) {
			cm.sendOk("Please hurry, the scientists are waiting for you in the laboratory.");
			cm.dispose();
		} else if (status == 10){ // start from his lab
			cm.sendNext("The #r#eTime Machine#k#n is functioning now after I fill the tank with Crystanol Fragment.\r\n\r\nBut sadly, it will only allow you to go back the #b#ePast#k#n.\r\n\r\nTo save the MapleWorld, you will need to access to the #b#eFuture#k#n and steal the Main Source of Alien in their MotherShip.\r\n\r\nOur military will use the Source to defeat the Aliens. With the help of the Source, our military will able to create a great weapon to kick those Aliens's ass.\r\n\r\nPlease, #d#h ##k please, help us.\r\nI'm still lacking some material to make the Time Machine able to access the #r#eFuture#k#n. Please help me go back the #b#ePast#k#n and get the material for me.\r\nThe material look like this : #i4031752#.\r\n\r\nThe #r#eTime Machine#k#n is ready now! Please save the MapleWorld. Our destiny is in your hand!");
			cm.updateCQInfo(100007, "timeMachine");
			cm.dispose();
		} else if (status == 11) {// check for quest ite here
			if (cm.completeCQ(100007)) {
				cm.sendOk("Thanks for your help in collecting 20 #t4031752#.\r\n\r\nThe #b#eTime Machine#r#k should be able to access to the #e#rFuture#n#k now.\r\n\r\nHowever, here is #r#e5 x #i4031753##k#n. This is the currency that we using now. Do not lost it, you might find this useful in the #e#rFuture#n#k world.\r\n\r\nGood luck in your destination!");
				if (!cm.haveItem(4031753)) {
					cm.gainItem(4031753, 5);
				}
				cm.gainExp(40);
				cm.removeAll(4031752);
				cm.startCQ(100008);
				cm.updateCQInfo(100008, "future");
			} else {
				cm.sendOk("You don't have 20 Blinking Dingbats."); // shouldn't happen
			}
			cm.dispose();
		} else if (status == 12) { //takled before
			cm.sendOk("Hurry up! The #b#rTime Machine#n#k is ready now!\r\nBe note that if you die or disconnect during the Time Travel you will need to start over again!");
			cm.dispose();
		} else if (status == 13) { 
			cm.sendOk("Hurry up! The #b#eTime Machine#n#k is ready to access the #r#eFuture#k#n now!");
			cm.dispose();
		} else if (status == 14) {
			cm.sendOk("OMG! You are our savior! Thanks you for helping us! Thanks for saving MapleWorld!\r\n\r\nI will send the Lump of Energy to our military, they know what to do with this.\r\n\r\nAh and, our Bossy wanted to meet you, he is waiting you at the #r#eCentral Command Room#k#n, you may reach him at the right portal. Please take a visit.\r\n\r\nOnce again, thanks you!");
			cm.removeAll(4001459);
			cm.updateCQInfo(100008, "");
			cm.completeCQ(100008);
			cm.startCQ(100009);
			cm.updateCQInfo(100009, "bing");
			cm.dispose();
		} else if (status == 15) {
			cm.sendNext("Thanks you for saving the MapleWorld.\r\nOur OSSS Boss would like to meet you! Hurry up, he is waiting you at the Command Room.");
			cm.dispose();
		} else if (status == 100) { //	npc not coded
			cm.sendNext("i'm not coded..");
			cm.dispose();
		}
	}
}