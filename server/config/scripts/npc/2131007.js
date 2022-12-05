var status = -1;    

function start() {
    if (cm.getCQInfo(150002) == "01.1") {
        if (cm.completeCQ(150001)) {
            status = 11;
        } else {
            status = 9;
    }
    } else if (cm.getCQInfo(150002) == "02") {
        status = 14;
    } else if (cm.getCQInfo(150002) == "03") {
        status = 19;
    } else if (cm.getCQInfo(150002) == "04") {
        status = 24;
    }
    action(1,0,0);
}

function action(m,t,s) {
    if (m > 0 ) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.startCQ(150002);
        cm.updateCQInfo(150002, "01");
        cm.sendNext("#eHey there! You must be #r#h ##k!\r\n\r\nI'm tess, my dad told me you would pass by today. My dad is quite an amazing fighter and hes actually teaching me how to fight!");
    } else if (status == 1) {
        cm.sendNextS("#eWow you look very #rpowerful#k!", 3);
    } else if (status == 2) {
        cm.sendNextS("#eMaybe because i am! Anyway, lets move on to training now.", 1);
    } else if (status == 3) {
        cm.sendNextS("#eWe're gonna train? When are we starting?", 3);
    } else if (status == 4) {
        cm.sendOkS("#eRight now.\r\n\r\nAnihilate 20 Tiv over there. Show me what you can do!", 1);
    } else if (status == 5) {
        cm.startCQ(150001);
        cm.updateCQInfo(150002, "01.1");
        cm.getPlayer().dropMessage(-1 ,"custom quest #1 started!");
        cm.dispose();
    } else if (status == 10) {
        cm.sendOk("#eYou're not #bdone#k yet!");
        cm.dispose();
    } else if (status == 12) {
        cm.updateCQInfo(150002, "02");
        cm.warp(100030100);
        cm.sendOkS("#eNice job. You really do fight well too!", 1);
        cm.dispose();
    } else if (status == 15) {
        cm.sendOk("#eTalk with my brother!");
        cm.dispose();
    } else if (status == 20) {
        cm.sendSimple("#eWhich occupation would you like to select?\r\n#r\r\n\r\n#L0#Gamer\r\n#L1#NxWhore#l\r\n#L2#Huntsman#l\r\n#L3#Ninja#l\r\n#L4#Vortex#l\r\n\r\n#b#L5#What are the perk each occupations get?");
    } else if (status == 21) {
        sel = s;
        if (s == 5) {
            cm.sendOk("#eYou may talk to my brother to learn any information about the occupations!");
            cm.dispose();
        }else{
            cm.sendYesNo("#eAre you #r#eseriously sure#k you want to select your occupation as a #b#e" + (sel == 0 ? "Gamer" : (sel == 1 ? "NxWhore" : (sel == 2 ? "Huntsman" : (sel == 3 ? "Ninja" : "Vortex")))) + "#k#n?\r\n\r\n#eThis option will greatly dictate your future gameplay!#n");
        }
	} else if (status == 22) {
		if (sel == 0) {
			cm.getPlayer().changeOccupation(201); //Gamer
            cm.updateCQInfo(150002, "04");
			cm.sendOkS("#eOk, now talk with Utah.", 1);
			cm.dispose();
		} else if (sel == 1) {
			cm.getPlayer().changeOccupation(501); //NxWhore
            cm.updateCQInfo(150002, "04");
			cm.sendOkS("#eOk, now talk with Utah.", 1);
			cm.dispose();
		} else if (sel == 2) {
			cm.getPlayer().changeOccupation(301); //Huntsman
            cm.updateCQInfo(150002, "04");
			cm.sendOkS("#eOk, now talk with Utah.", 1);
			cm.dispose();
		} else if (sel == 3) {
			cm.getPlayer().changeOccupation(101); //Ninja
            cm.updateCQInfo(150002, "04");
			cm.sendOkS("#eOk, now talk with Utah.", 1);
			cm.dispose();
		} else if (sel == 4) {
			cm.getPlayer().changeOccupation(401); //Vortex
            cm.updateCQInfo(150002, "04");
			cm.sendOkS("#eOk, now talk with Utah.", 1);
		}
    } else if (status == 23) {
        cm.sendOkS("#eWe're gonna do a trial to test your occupation and if you complete this little trial/test, we will award you with a starter pack.", 1);
        cm.dispose();
    } else if (status == 25) {
        cm.sendOk("#eWhat are you waiting for?!?");
        cm.dispose();
    }
}