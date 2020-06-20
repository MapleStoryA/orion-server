var status = -1;
var gender;

function start() {
    if (cm.getCQInfo(150002) == "03") {
        status = 19;
        //cm.sendOk("#eGo ahead and talk to Tess!");
        //cm.dispose();
    } else if (cm.getCQInfo(150002) == "04") {
        status = 24;
    } else if (cm.getCQInfo(150002) == "05") {
        status = 29;
    } else if (cm.getCQInfo(150002) == "06") {
        if (cm.completeCQ(150003)) {
            status = 39;
        } else {
            status = 34;
        }
    } else if (cm.getCQInfo(150002) == "07") {
    	status = 49;
    }
    action(1,0,0);
}

function action(m,t,s) {
    gender = cm.getPlayer().getGender() == 0 ? "guy" : "girl";
    if (m > 0) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendNext("#eHey #h #! Our dad told us about you. Apparently you're new here.");
    } else if (status == 1) {
        cm.sendNextS("#eYeap. So, whats so special here on MapleStory? I heard it su...", 3);
    } else if (status == 2) {
        cm.sendNextNPC("#eDude...", 5, 2131007);
    } else if (status == 3) {
        cm.sendNextS("#eThis "+gender+" is clueless. We are FULL of costum feature here on MapleStory. Let me give you a small glimpse of what you can expect.", 1);
    } else if (status == 4) {
        cm.sendNextS("#e Entirely customized quest system that can hold custom data\r\n\r\n Exp card that give you exp when collected\r\n\r\n Stability Optimized to the max\r\n\r\n Professionally coded NPC's\r\n\r\n Fully working Evan and Dual Blade\r\n\r\n Fully working Family\r\n\r\n Packet Editing mostly fixed like getting any skill\r\n\r\n Maple Quiz fully working\r\n\r\n Awesome occupation\r\n\r\n Fully working Potion shop discount (not implemented though)\r\n\r\nreporting system for players to report people do @report for more info\r\n\r\n follow system work\r\n\r\n Triple Third Event (Extra exp for all third killed monster after being logged in for 1 hour)\r\n\r\n\r\n\r\nAND ALOT MORE.", 1);
    } else if (status == 5) {
        cm.sendNextS("#eThanks for clearing thing's up. This is truly one of the best server ever!! So what now?", 3);
    } else if (status == 6) {
        cm.updateCQInfo(150002, "03");
        cm.sendOk("#eYou need an occupation. Talk with #bTess#k.He'll let you choose!");
        cm.dispose();
    } else if (status == 20) {
        cm.sendSimple("#eWould you like to know about any occupation? \r\n\r\n#L0#The Gamer#l\r\n#L1#NxWhore#l\r\n#L2#Huntersman#l\r\n#L3#Ninja#l\r\n#L4#Vortex#l");
    } else if (status == 21) {
        if (s == 0) {
            cm.sendOk("#e#bOccupation:#n Gamer#k\r\n#eDescription:#n The ultimate madness player\r\n#eDefault Rate Changes:#n #r100% on all rates#k\r\n#eAdditional Passive:#n Boots up everyone's EXP Rate except itself in it's party.\r\n#eMethod of Gaining Occupation EXP:#n Find your Occupation NPC by using @occupation in the corresponding rebirth and do the quest to level up your Occupation.\r\n\r\n#e#bPerks:#n#k\r\n\r\nLevel  1 : +50x EXP rate\r\nLevel  2 : +50x EXP rate\r\nLevel  3 : +50x EXP rate\r\nLevel  4 : +50x EXP rate\r\nLevel  5 : +50x EXP rate\r\nLevel  6 : +50x EXP rate\r\nLevel  7 : +50x EXP rate\r\nLevel  8 : +50x EXP rate\r\nLevel  9 : +50x EXP rate\r\nLevel  10 : +50x EXP rate");
            cm.dispose();
        } else if (s == 1) {
            cm.sendOk("#e#bOccupation:#n NX Whore#k\r\n#eDescription:#n The ultimate lazy ass of Henesys\r\n#eDefault Rate Changes:#n #r80% on all rates#k\r\n#eAdditional Passive:#n Boots up everyone's Cash Rate except itself in it's party.\r\n#eMethod of Gaining Occupation EXP:#n Every 15 minute spent in Henesys Town, Henesys Game Park(Casino) will give you some EXP.\r\n\r\n#e#bPerks:#n#k\r\n\r\nLevel  1 : Gain 1,333,337 cash (cooldown 1 hours)\r\nLevel 2 : Gain @heal <name> commands, cooldown 15minutes\r\nLevel 3 : @heal <name> commands, cooldown reduce to 10 minutes\r\nLevel 4 : @heal <name> commands, cooldown reduce to 5minutes\r\nLevel 5 : @heal <name> commands, cooldown reduce to 1minutes\r\nLevel 6 :  Gain @bomb commands,  cooldown 20 minutes\r\nLevel 7 : @bomb commands, cooldown reduce to 10 minutes\r\nLevel 8 : @sex <name>commands, cooldown 180 minutes\r\nLevel 9 :  @sex <name> commands, cooldown reduce to 150 minutes\r\nLevel 10 : @kill <name>, cooldown 360minutes");
            cm.dispose();
        } else if (s == 2) {
            cm.sendOk("#e#bOccupation:#n Huntsman#k\r\n#eDescription:#n The ultimate madness hunter\r\n#eDefault Rate Changes:#n #r30% EXP Rate, 100% on others.#k\r\n#eAdditional Passive:#n Boots up everyone's Drop Rate except itself in it's party.\r\n#eMethod of Gaining Occupation EXP:#n Find your Occupation NPC by using @occupation in the corresponding rebirth and do the quest to level up your Occupation.\r\n\r\n#e#bPerks:#n#k\r\n\r\nLevel 1 : Mob drop extra 1 more etc = total 2 etc drop from mob. Success rate 30%\r\nLevel 2 : Mob drop extra 1 more etc = total 2 etc drop from mob. Success rate 100%\r\nLevel 3 : Mob drop extra 2 more etc = total 3 etc drop from mob. Success rate 30%\r\nLevel 4 : Mob drop extra 2 more etc = total 3 etc drop from mob. Success rate 100%\r\nLevel 5 : Mob drop extra 3 more etc = total 4 etc drop from mob. Success Rate 30%\r\nLevel 6 : Mob drop extra 3 more etc = total 4 etc drop from mob. Success Rate 100%\r\nLevel 7 : Mob drop extra 4 more etc = total 5 etc drop from mob. Success Rate 30%\r\nLevel 8 : Mob drop extra 4 more etc = total 5 etc drop from mob. Success Rate 100%\r\nLevel 9 : Mob drop extra 5 more etc = total 6 etc drop from mob. Success Rate 30%\r\nLevel 10 : Mob drop extra 5 more etc = total 6 etc drop from mob. Success Rate 100%,  ExpRate+ 100x");
            cm.dispose();
        } else if (s == 3) {
            cm.sendOk("#e#bOccupation:#n Ninja#k\r\n#eDescription:#n The ultimate assaulter\r\n#eDefault Rate Changes:#n #r100% on all rates#k\r\n#eMethod of Gaining Occupation EXP:#n Every monster killed gain 1exp in 40% chance.\r\n\r\n#e#bPerks:#n#k\r\n\r\nLevel 1 : @clone, 100% to summon 1st  clone, clone damage = 25%\r\nLevel 2 : @clone, 100% to summon 1st  clone, clone damage = 35%\r\nLevel 3 : @clone, 100% to summon 1st  clone, 70% to get 2nd clone, clone damage = 35%\r\nLevel 4 : @clone, 100% to summon 1st clone, 70% to get 2nd clone, clone damage = 40%\r\nLevel 5 : @clone, 100% to summon 1st clone, 80% to get 2nd clone, 70% to get 3rd clone, clone damage = 40%\r\nLevel 6 : @clone, 100% to summon 1st clone, 80% to get 2nd clone, 70% to get 3rd clone, clone damage = 50%\r\nLevel 7 : @clone, 100% to summon 1st clone, 90% to get 2nd clone, 80% to get 3rd clone, 60% to get 4th clone, clone damage = 50%\r\nLevel 8 : @clone, 100% to summon 1st clone, 90% to get 2nd clone, 80% to get 3rd clone, 60% to get 4th clone, clone damage = 55%\r\nLevel 9 : @clone, 100% to summon 1st clone, 100% to get 2nd clone, 90% to get 3rd clone, 70% to get 4th clone, 60% to get 5th clone, clone damage = 55%\r\nLevel 10 : @clone, 100% to summon 1st clone, 100% to get 2nd clone, 90% to get 3rd clone, 70% to get 4th clone, 60% to get 5th clone, clone damage = 55%\r\n@clone = 1hour cool down.");
            cm.dispose();
        } else if (s == 4) {
            cm.sendOk("#e#bOccupation:#n Vortex#k\r\n#eDescription:#n The ultimate vacuum\r\n#eDefault Rate Changes:#n #r60% EXP Rate, 100% on others.#k\r\n#eMethod of Gaining Occupation EXP:#n Loot items to increase occupation EXP.\r\n\r\n#e#bPerks:#n#k\r\n\r\nLevel 1 : 2% loot from whole screen.\r\nLevel 2 : 3% loot from whole screen.\r\nLevel 3 : 4% loot from whole screen.\r\nLevel 4 : 5% loot from whole screen.\r\nLevel 5 : 6% loot from whole screen.\r\nLevel 6 : 7% loot from whole screen.\r\nLevel 7 : 8% loot from whole screen.\r\nLevel 8 : 9% loot from whole screen.\r\nLevel 9 : 9.5% loot from whole screen.\r\nLevel 10 : 10% loot from whole screen.");
            cm.dispose();
        }
    } else if (status == 25) {
        cm.sendOk("#eAlright #h #... I'm sure Tess told you about it. Are you ready to take on this little test?!?");
    } else if (status == 26) {
        cm.updateCQInfo(150002, "05");
        cm.warp(100030300);
        cm.sendOkS("#eVery good. Please talk to me again once you are ready!\r\n\r\n#d#nNote: If you didn't appear at the entrance, all the way to the right, just walk until you reach it.", 1);
        cm.dispose();
    } else if (status == 30) {
        cm.sendNextS("#eAlright, as you may or may not have noticed, there are huge corrupted bird over there. They are no longer the cute Tiv they used to be.\r\n\r\n It seems like there a virus going on but we can't get in touch with it. Could you please kill a few of them ? Lets say 30. That'd be helpful.", 1);
    } else if (status == 31) {
        cm.sendOkS("#eCome back to me when you're done!", 1);
    } else if (status == 32) {
        cm.startCQ(150003);
        cm.updateCQInfo(150002, "06");
        cm.getPlayer().dropMessage(-1, "Custom quest #2 Started");
        cm.dispose();
    } else if (status == 35) {
        cm.sendOk("#eYou're not done yet!")
        cm.dispose();
    } else if (status == 40) {
        cm.updateCQInfo(150002, "07");
        cm.sendNextS("#eHaha thanks. That should definitely slow them down!", 1);
    } else if (status == 41) {
        cm.sendNextS("#eHey its nothing haha. They are still weak in my opinion!", 3)
    } else if (status == 42) {
        cm.sendNextS("#eAh. Anyway. As a reward for helping us, i'll give you a little pack with helpful items. Thanks for playing MapleStory. It is still in its very early ages! More feature will be developped soon.", 1);
    } else if (status == 43) {
        cm.getPlayer().dropMessage(1, "Please, if you like the server so far, go register on the forum right now and post an introduction. 'forum.MapleStory.info' or click the forum tab on the site.");
		cm.getPlayer().dropMessage(1, "Please, if you like the server so far, go register on the forum right now and post an introduction. 'forum.MapleStory.info' or click the forum tab on the site.");
		cm.warp(100000000, 0)
        cm.dispose();
    } else if (status == 50) {
    	cm.warp(100000000);
    	cm.sendOk("#efuck off. no occupation changing you bitch.")
    	cm.dispose();
	}
}