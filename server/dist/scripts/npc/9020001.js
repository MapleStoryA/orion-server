/**
	Cloto - Hidden Street : 1st Accompaniment
**/
load("nashorn:mozilla_compat.js");
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
var questions = Array("Here's the question. Collect the same number of coupons as the minimum level required to make the first job advancement as warrior.",
    "Here's the question. Collect the same number of coupons as the minimum amount of STR needed to make the first job advancement as a warrior.",
    "Here's the question. Collect the same number of coupons as the minimum amount of INT needed to make the first job advancement as a magician.",
    "Here's the question. Collect the same number of coupons as the minimum amount of DEX needed to make the first job advancement as a bowman.",
    "Here's the question. Collect the same number of coupons as the minimum amount of DEX needed to make the first job advancement as a thief.",
    "Here's the question. Collect the same number of coupons as the minimum level required to advance to 2nd job.");
var qanswers = Array(10, 35, 20, 25, 25, 30);
var party;
var preamble;
var stage2rects = Array(new Rectangle(-770,-132,28,178),new Rectangle(-733,-337,26,105),new Rectangle(-601,-328,29,105),new Rectangle(-495,-125,24,165));
var stage2combos = Array(Array(0,1,1,1),Array(1,0,1,1),Array(1,1,0,1),Array(1,1,1,0));
var stage3rects = Array(new Rectangle(608,-180,140,50),new Rectangle(791,-117,140,45),new Rectangle(958,-180,140,50),new Rectangle(876,-238,140,45),new Rectangle(702,-238,140,45));
var stage3combos = Array(Array(0,0,1,1,1),Array(0,1,0,1,1),Array(0,1,1,0,1),Array(0,1,1,1,0),Array(1,0,0,1,1),Array(1,0,1,0,1),Array(1,0,1,1,0),Array(1,1,0,0,1),Array(1,1,0,1,0),Array(1,1,1,0,0));
var stage4rects = Array(new Rectangle(910,-236,35,5),new Rectangle(877,-184,35,5),new Rectangle(946,-184,35,5),new Rectangle(845,-132,35,5),new Rectangle(910,-132,35,5),new Rectangle(981,-132,35,5));
var stage4combos = Array(Array(0,0,0,1,1,1),Array(0,0,1,0,1,1),Array(0,0,1,1,0,1),Array(0,0,1,1,1,0),Array(0,1,0,0,1,1),Array(0,1,0,1,0,1),Array(0,1,0,1,1,0),Array(0,1,1,0,0,1),Array(0,1,1,0,1,0),Array(0,1,1,1,0,0),Array(1,0,0,0,1,1),Array(1,0,0,1,0,1),Array(1,0,0,1,1,0),Array(1,0,1,0,0,1),Array(1,0,1,0,1,0),Array(1,0,1,1,0,0),Array(1,1,0,0,0,1),Array(1,1,0,0,1,0),Array(1,1,0,1,0,0),Array(1,1,1,0,0,0));
var eye = 9300002;
var necki = 9300000;
var slime = 9300003;
var monsterIds = Array(eye, eye, eye, necki, necki, necki, necki, necki, necki, slime);
var prizeIdScroll = Array(2040502, 2040505,					// Overall DEX and DEF
    2040802,										// Gloves for DEX
    2040002, 2040402, 2040602);						// Helmet, Topwear and Bottomwear for DEF
var prizeIdUse = Array(2000001, 2000002, 2000003, 2000006,	// Orange, White and Blue Potions and Mana Elixir
    2000004, 2022000, 2022003);						// Elixir, Pure Water and Unagi
var prizeQtyUse = Array(80, 80, 80, 50,
    5, 15, 15);
var prizeIdEquip = Array(1032004, 1032005, 1032009,			// Level 20-25 Earrings
    1032006, 1032007, 1032010,						// Level 30 Earrings
    1032002,										// Level 35 Earring
    1002026, 1002089, 1002090);						// Bamboo Hats
var prizeIdEtc = Array(4010000, 4010001, 4010002, 4010003,	// Mineral Ores
    4010004, 4010005, 4010006,						// Mineral Ores
    4020000, 4020001, 4020002, 4020003,				// Jewel Ores
    4020004, 4020005, 4020006,						// Jewel Ores
    4020007, 4020008, 4003000);						// Diamond and Black Crystal Ores and Screws
var prizeQtyEtc = Array(15, 15, 15, 15,
    8, 8, 8,
    8, 8, 8, 8,
    8, 8, 8,
    3, 3, 30);

function start() {
    status = -1;
    mapId = cm.getMapId();
    if (mapId == 103000800)
	curMap = 1;
    else if (mapId == 103000801)
	curMap = 2;
    else if (mapId == 103000802)
	curMap = 3;
    else if (mapId == 103000803)
	curMap = 4;
    else if (mapId == 103000804)
	curMap = 5;
    playerStatus = cm.isLeader();
    preamble = null;
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
    
    if (curMap == 1) { // First Stage.
	if (playerStatus) { // Check if player is leader
	    if (status == 0) {
		var eim = cm.getEventInstance();
		party = eim.getPlayers();
		preamble = eim.getProperty("leader1stpreamble");

		if (preamble == null) {
		    cm.sendNext("Hello. Welcome to the first stage. Look around and you'll see Ligators wandering around. When you defeat them, they will cough up a #bcoupon#k. Every member of the party other than the leader should talk to me, geta  question, and gather up the same number of #bcoupons#k as the answer to the question I'll give to them.\r\nIf you gather up the right amount of #bcoupons#k, I'll give the #bpass#k to that player. Once all the party members other than the leader gather up the #bpasses#k and give them to the leader, the leader will hand over the #bpasses#k to me, clearing the stage in the process. The faster you take care of the stages, the more stages you'll be able to challenge. So I suggest you take care of things quickly and swiftly. Well then, best of luck to you.");
		    eim.setProperty("leader1stpreamble", "done");
		    cm.dispose();
		} else { // Check how many they have compared to number of party members
		    // Check for stage completed
		    var complete = eim.getProperty(curMap.toString() + "stageclear");
		    if (complete != null) {
			cm.sendNext("Please hurry on to the next stage, the portal opened!");
			cm.dispose();
		    } else {
			var numpasses = party.size() - 1;
			var strpasses = "#b" + numpasses.toString() + " passes#k";
			if (!cm.haveItem(4001008, numpasses)) {
			    cm.sendNext("I'm sorry, but you are short on the number of passes. You need to give me the right number of passes; it should be the number of members of your party minus the leader, " + strpasses + " to clear the stage. Tell your party members to solve the questions, gather up the passes, and give them to you.");
			    cm.dispose();
			} else {
			    cm.sendNext("You gathered up " + strpasses + "! Congratulations on clearing the stage! I'll make the portal that sends you to the next stage. There's a time limit on getting there, so please hurry. Best of luck to you all!");
			    clear(1,eim,cm);
			    cm.givePartyExp(100, party);
			    cm.gainItem(4001008, -numpasses);
			    cm.dispose();
			// TODO: Make the shiny thing flash
			}
		    }
		}
	    }
	} else { // Not leader
	    var eim = cm.getChar().getEventInstance();
	    pstring = "member1stpreamble" + cm.getChar().getId().toString();
	    preamble = eim.getProperty(pstring);
	    if (status == 0 && preamble == null) {
		var qstring = "member1st" + cm.getChar().getId().toString();
		var question = eim.getProperty(qstring);
		if (question == null) {
		    // Select a random question to ask the player.
		    var questionNum = Math.floor(Math.random() * questions.length);
		    eim.setProperty(qstring, questionNum.toString());
		}
		cm.sendNext("Here, you need to collect #bcoupons#k by defeating the same number of Ligators as the answer to the questions asked individually.");
	    } else if (status == 0) { // Otherwise, check for stage completed
		var complete = eim.getProperty(curMap.toString() + "stageclear");
		if (complete != null) {
		    cm.sendNext("Please hurry on to the next stage, the portal opened!");
		    cm.dispose();
		} else {
		    // Reply to player correct/incorrect response to the question they have been asked
		    var qstring = "member1st" + cm.getChar().getId().toString();
		    var numcoupons = qanswers[parseInt(eim.getProperty(qstring))];
		    var qcorr = cm.haveItem(4001007,(numcoupons+1));
		    var enough = false;
		    if (!qcorr) { // Not too many
			qcorr = cm.haveItem(4001007,numcoupons);
			if (qcorr) { // Just right
			    cm.sendNext("That's the right answer! For that you have just received a #bpass#k. Please hand it to the leader of the party.");
			    cm.gainItem(4001007, -numcoupons);
			    cm.gainItem(4001008, 1);
			    enough = true;
			}
		    }
		    if (!enough) {
			cm.sendNext("I'm sorry, but that is not the right answer! Please have the correct number of coupons in your inventory.");
		    }
		    cm.dispose();
		}
	    } else if (status == 1) {
		if (preamble == null) {
		    var qstring = "member1st" + cm.getChar().getId().toString();
		    var question = parseInt(eim.getProperty(qstring));
		    cm.sendNextPrev(questions[question]);
		} else { // Shouldn't happen, if it does then just dispose
		    cm.dispose();
		}
	    } else if (status == 2) { // Preamble completed
		eim.setProperty(pstring,"done");
		cm.dispose();
	    } else { // Shouldn't happen, but still...
		eim.setProperty(pstring,"done"); // Just to be sure
		cm.dispose();
	    }
	} // End first map scripts
    } else if (2 <= curMap && 4 >= curMap) {
	rectanglestages(cm);
    } else if (curMap == 5) { // Final stage
	var eim = cm.getChar().getEventInstance();
	var stage5done = eim.getProperty("5stageclear");
	if (stage5done == null) {
	    if (playerStatus) { // Leader
		var passes = cm.haveItem(4001008,10);
		if (passes) {
		    // Clear stage
		    cm.sendNext("Here's the portal that leads you to the last, bonus stage. It's a stage that allows you to defeat regular monsters a little easier. You'll be given a set amount of time to hunt as much as possible, but you can always leave the stage in the middle of it through the NPC. Again, congratulations on clearing all the stages. Take care...");
		    party = eim.getPlayers();
		    cm.gainItem(4001008, -10);
		    clear(5,eim,cm);
		    cm.givePartyExp(1500, party);
		    cm.dispose();
		} else { // Not done yet
		    cm.sendNext("Hello. Welcome to the 5th and final stage. Walk around the map and you'll be able to find some Boss monsters. Defeat all of them, gather up #bthe passes#k, and please get them to me. Once you earn your pass, the leader of your party will collect them, and then get them to me once the #bpasses#k are gathered up. The monsters may be familiar to you, but they may be much stronger than you think, so please be careful. Good luck!");
		}
		cm.dispose();
	    } else { // Members
		cm.sendNext("Welcome to the 5th and final stage.  Walk around the map and you will be able to find some Boss monsters.  Defeat them all, gather up the #bpasses#k, and give them to your leader.  Once you are done, return to me to collect your reward.");
		cm.dispose();
	    }
	} else { // Give rewards and warp to bonus
	    if (status == 0) {
		cm.sendNext("Incredible! You cleared all the stages to get to this point. Here's a small prize for your job well done. Before you accept it, however, please make sure your use and etc. inventories have empty slots available.\r\n#bYou will not receive a prize if you have no free slots!#k");
	    }
	    if (status == 1) {
		getPrize(eim,cm);
		cm.dispose();
	    }
	}
    } else { // No map found
	cm.sendNext("Invalid map, this means the stage is incomplete.");
	cm.dispose();
    }
}

function clear(stage, eim, cm) {
    eim.setProperty(stage.toString() + "stageclear","true");

    cm.showEffect(true, "quest/party/clear");
    cm.playSound(true, "Party1/Clear");
    cm.environmentChange(true, "gate");

    var mf = eim.getMapFactory();
    map = mf.getMap(103000800 + stage);
    var nextStage = eim.getMapFactory().getMap(103000800 + stage);
    var portal = nextStage.getPortal("next00");
    if (portal != null) {
	portal.setScriptName("kpq" + (stage+1).toString());
    }
}

function failstage(eim, cm) {
    cm.showEffect(true, "quest/party/wrong_kor");
    cm.playSound(true, "Party1/Failed");
}

function rectanglestages (cm) {
    // Debug makes these stages clear without being correct
    var debug = false;
    var eim = cm.getChar().getEventInstance();
    if (curMap == 2) {
	var nthtext = "2nd";
	var nthobj = "ropes";
	var nthverb = "hang";
	var nthpos = "hang on the ropes too low";
	var curcombo = stage2combos;
	var currect = stage2rects;
	var objset = [0,0,0,0];
    } else if (curMap == 3) {
	var nthtext = "3rd";
	var nthobj = "platforms";
	var nthverb = "stand";
	var nthpos = "stand too close to the edges";
	var curcombo = stage3combos;
	var currect = stage3rects;
	var objset = [0,0,0,0,0];
    } else if (curMap == 4) {
	var nthtext = "4th";
	var nthobj = "barrels";
	var nthverb = "stand";
	var nthpos = "stand too close to the edges";
	var curcombo = stage4combos;
	var currect = stage4rects;
	var objset = [0,0,0,0,0,0];
    }
    if (playerStatus) { // Check if player is leader
	if (status == 0) {
	    // Check for preamble
	    party = eim.getPlayers();
	    preamble = eim.getProperty("leader" + nthtext + "preamble");
	    if (preamble == null) {
		cm.sendNext("Hi. Welcome to the " + nthtext + " stage. Next to me, you'll see a number of " + nthobj + ". Out of these " + nthobj + ", #b3 are connected to the portal that sends you to the next stage#k. All you need to do is have #b3 party members find the correct " + nthobj + " and " + nthverb + " on them.#k\r\nBUT, it doesn't count as an answer if you " + nthpos + "; please be near the middle of the " + nthobj + " to be counted as a correct answer. Also, only 3 members of your party are allowed on the " + nthobj + ". Once they are " + nthverb + "ing on them, the leader of the party must #bdouble-click me to check and see if the answer's correct or not#k. Now, find the right " + nthobj + " to " + nthverb + " on!");
		eim.setProperty("leader" + nthtext + "preamble","done");
		var sequenceNum = Math.floor(Math.random() * curcombo.length);
		eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
		cm.dispose();
	    } else {
		// Otherwise, check for stage completed
		var complete = eim.getProperty(curMap.toString() + "stageclear");
		if (complete != null) {
		    var mapClear = curMap.toString() + "stageclear";
		    eim.setProperty(mapClear,"true"); // Just to be sure
		    cm.sendNext("Please hurry on to the next stage, the portal opened!");
		} else { // Check for people on ropes and their positions
		    var totplayers = 0;
		    for (i = 0; i < objset.length; i++) {
			for (j = 0; j < party.size(); j++) {
			    var present = currect[i].contains(party.get(j).getPosition());
			    if (present) {
				objset[i] = objset[i] + 1;
				totplayers = totplayers + 1;
			    }
			}
		    }
		    // Compare to correct positions
		    // First, are there 3 players on the correct positions?
		    if (totplayers == 3 || debug) {
			var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
			// Debug
			// Combo = curtestcombo;
			var testcombo = true;
			for (i = 0; i < objset.length; i++) {
			    if (combo[i] != objset[i])
				testcombo = false;
			}
			if (testcombo || debug) {
			    // Do clear
			    clear(curMap,eim,cm);
			    var exp = (Math.pow(2,curMap) * 50);
			    cm.givePartyExp(exp, party);
			    cm.dispose();
			} else { // Wrong
			    // Do wrong
			    failstage(eim,cm);
			    cm.dispose();
			}
		    } else {
			if (debug) {
			    var outstring = "Objects contain:"
			    for (i = 0; i < objset.length; i++) {
				outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
			    }
			    cm.sendNext(outstring);
			} else {
			    cm.sendNext("It looks like you haven't found the 3 " + nthobj + " just yet. Please think of a different combination of " + nthobj + ". Only 3 are allowed to " + nthverb + " on " + nthobj + ", and if you " + nthpos + " it may not count as an answer, so please keep that in mind. Keep going!");
			}
			cm.dispose();
		    }
		}
	    }
	} else {
	    var complete = eim.getProperty(curMap.toString() + "stageclear");
	    if (complete != null) {
		var target = eim.getMapInstance(103000800 + curMap);
		var targetPortal = target.getPortal("st00");
		cm.getChar().changeMap(target, targetPortal);
	    }
	    cm.dispose();
	}
    } else { // Not leader
	if (status == 0) {
	    var complete = eim.getProperty(curMap.toString() + "stageclear");
	    if (complete != null) {
		cm.sendNext("Please hurry on to the next stage, the portal opened!");
	    } else {
		cm.sendNext("Please have the party leader talk to me.");
		cm.dispose();
	    }
	} else {
	    var complete = eim.getProperty(curMap.toString() + "stageclear");
	    if (complete != null) {
		var target = eim.getMapInstance(103000800 + curMap);
		var targetPortal = target.getPortal("st00");
		cm.getChar().changeMap(target, targetPortal);
	    }
	    cm.dispose();
	}
    }
}

function getPrize(eim,cm) {
    var itemSetSel = Math.random();
    var itemSet;
    var itemSetQty;
    var hasQty = false;
    if (itemSetSel < 0.3)
	itemSet = prizeIdScroll;
    else if (itemSetSel < 0.6)
	itemSet = prizeIdEquip;
    else if (itemSetSel < 0.9) {
	itemSet = prizeIdUse;
	itemSetQty = prizeQtyUse;
	hasQty = true;
    } else {
	itemSet = prizeIdEtc;
	itemSetQty = prizeQtyEtc;
	hasQty = true;
    }
    var sel = Math.floor(Math.random()*itemSet.length);
    var qty = 1;
    if (hasQty)
	qty = itemSetQty[sel];
    cm.gainItem(itemSet[sel], qty);
    cm.gainNX(1000);
    cm.removeAll(4001007);
    cm.removeAll(4001008);
    cm.getPlayer().endPartyQuest(1201);
    cm.warp(103000805, "sp");
}