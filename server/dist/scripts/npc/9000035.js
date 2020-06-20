/**
 * Agent P
 * First Storyline NPC Quest
 */
var status = 0;

function start() {
    if (cm.isCQFinished(100000) || cm.getPlayerStat("LVL") > 2/* && cm.getPlayerStat("GM") == 0*/) { // impossible
        cm.sendNext("Hey! What are you doing here? Please don't disturb me! I'm really busy!");
        cm.dispose();
        return;
    }
    if (cm.getCQInfo(100000).contains("step=01")) {
        status = 11;
    } else if (cm.getCQInfo(100000).contains("step=02")) {
        status = 13;
    } else {
        status = -1;
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
            cm.sendNextS("You... are finally awake!", 1);
        } else if (status == 1) {
            cm.sendNextPrevS("...who are you?", 3);
        } else if (status == 2) {
            cm.sendNextPrevS("I've been waiting for you. Waiting for the hero that has potential to help us to save our world...!", 1);
        } else if (status == 3) {
            cm.sendNextPrevS("Wait, what are you saying? And who are you...?", 3);
        } else if (status == 4) {
            cm.sendNextPrevS("I'm Doctor Bing's assistant. He was in a rush when he called me few hours ago. I've no time to explain everything to you, let Dr.Bing talk to you. He will tell you everything about it. Hold on a minute.", 1);
        } else if (status == 5) {
            cm.sendNextNPC("#b(Agent P forward the call to you.)#k\r\nBeep...Beep...", 5, 9250120);
        } else if (status == 6) {
            cm.sendNextNPC("#b(The call was picked up.)#k\r\nFinally, you're here #h0#! I've been waiting for you since the past few hours.", 5, 9250143);
        } else if (status == 7) {
            cm.sendNextPrevS("Waiting for me? I don't understand. Why is it me?", 3);
        } else if (status == 8) {
            cm.sendNextPrevNPC("Since the past few hours, our world is being invaded by aliens, some of my friends got captured. I've done a research and finally found a brilliant idea to save this world. But...", 5, 9250143);
        } else if (status == 9) {
            cm.sendNextPrevS("But what?", 3);
        } else if (status == 10) {
            cm.sendNextPrevNPC("I couldn't do all these by myself. I need YOU to help me with it. This is what my Surveillance Camera caught.", 5, 9250143);
        } else if (status == 11) {
            cm.startCQ(100000);
            cm.updateCQInfo(100000, "step=01");
            cm.MovieClipIntroUI(true);
            cm.showWZEffect("Effect/DirectionVisitor.img/visitor/Basic");
            cm.dispose();
        } else if (status == 12) { // second part
            cm.sendNextNPC("Saw the scene which I've recorded uh? You must hurry now! I've already set up a #bshuttle#k for you to get to my base. I'll be waiting for you there. I will be at.....#bbeep!#k\r\n#b(The call was disengaged before he managed to finish his sentence.)#k", 5, 9250143);
        } else if (status == 13) {
            cm.updateCQInfo(100000, "step=02");
            cm.sendNext("Ahh! I guess the phone network has some interruption due to the alien invasion. Anyway, you must hurry now. Please proceed to the portal to board the shuttle to Dr.Bing's base.")
            cm.dispose();
        } else if (status == 14) { // third
            cm.sendOk("What are you waiting for? You must hurry! There is no time for this. The shuttle is ready now!");
            cm.dispose();
        }
    }
}