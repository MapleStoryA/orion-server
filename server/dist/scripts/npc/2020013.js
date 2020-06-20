/**
	Fedro: Pirate 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

var status = 0;
var job;


importPackage(Packages.client);

var status = 0;
var job;


function start() {
    if (!ThirdJobUtils.isPirateSecondJob(cm.getPlayer())) {
        cm.sendOk("May #rOdin#k be with you!");
        cm.dispose();
        return;
    }
    if (cm.isQuestComplete(100102)) {
        cm.sendNext("#rBy Odin's ring!#k Indeed, you have proven to be worthy of the strength I will now bestow upon you.");
    } else if (cm.isQuestActive(100102)) {
        cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
        cm.dispose();
    } else if (cm.isQuestComplete(100100)) {
        cm.sendNext("#rBy Odin's raven!#k I was right, your strength is truly excellent.");
    } else if (cm.isQuestActive(100102)) {
        cm.sendOk("Well, well. Now go and see #bthe Dark Lord#k. He will show you the way.");
        cm.dispose();
    }else if (ThirdJobUtils.isPirateSecondJob(cm.getPlayer()) &&
        cm.getLevel() >= 70 &&  cm.getPlayer().getRemainingSp() <= (cm.getLevel() - 70) * 3) {
        cm.sendNext("#rBy Odin's beard!#k You are a strong one.");
    }  else {
        cm.sendOk("Your time has yet to come...");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.sendOk("Make up your mind and visit me again.");
            cm.dispose();
            return;
        }
        status++;
        if (status == 1) {
            if (cm.isQuestComplete(100102)) {
                if (cm.getJobId()==510) {
                    cm.changeJobById(511);
                    cm.gainAp(5);
            		cm.gainSp(1);
                    cm.sendOk("You are now a #bMarauder#k. May #rOdin#k be with you!");
                    cm.dispose();
                } else if (cm.getJobId()==520) {
                    cm.changeJobById(521);
                    cm.gainAp(5);
            		cm.gainSp(1);
                    cm.sendOk("You are now a #bOutlaw#k. May #rOdin#k be with you!");
                    cm.dispose();
                } 
            } else if (cm.isQuestComplete(100100))
                cm.sendAcceptDecline("Is your mind ready to undertake the final test?");
            else
                cm.sendAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
        } else if (status == 2) {
            if (cm.isQuestComplete(100100)) {
                cm.startQuest(100102);
                cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
                cm.dispose();
            } else {
                cm.startQuest(100100);
                cm.sendOk("Well, well. Now go and see #bKyrin The captain#k. She will show you the way.");
                cm.dispose();
            }
        }
    }
}	
