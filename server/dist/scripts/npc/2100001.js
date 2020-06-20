/* Author: aaroncsn (MapleSea Like)
	NPC Name: 		Muhammad
	Map(s): 		Ariant:The Town of Ariant(260000200)
	Description: 	Jewel Refiner
*/


load("nashorn:mozilla_compat.js");
importPackage(Packages.client);

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.sendNext("If you aren't in a hurry, then please come back in a bit. As you can see, there's so much work going on right now that I can't possibly give them to you on time.");
			cm.dispose();
			return;
		}
		if (mode == 0 && status >= 1){
			cm.dispose();
			return;
			}
		if (mode == 1)
			status++;
		else
			status--;
	if(status == 0)
		cm.sendYesNo("Are you here to refine the ores of a mineral or a jewel? It doesn't matter how many ores you have, if you don't have them refined by a master like me, then they won't see the light of day. What do you think, do you want to refine them right now?");
	if (status == 1 && mode == 1) {
		var selStr = "I like your attitude! Let's just take care of this right now. What kind of ores would you like to refine? #b";
		var options = new Array("Refine mineral ore","Refine jewel ores","Refine crystal ores");
		for (var i = 0; i < options.length; i++){
			selStr += "\r\n#L" + i + "# " + options[i] + "#l";
		}
		cm.sendSimple(selStr);
	}
	else if (status == 2 && mode == 1) {
		selectedType = selection;
		if (selectedType == 0){ //mineral refine
			var selStr = "Which mineral would you like to refine?#b";
			var minerals = new Array ("Bronze Plate","Steel Plate","Mithril Plate","Adamantium Plate","Silver Plate","Orihalcon Plate","Gold Plate","Lithium");
			for (var i = 0; i < minerals.length; i++){
				selStr += "\r\n#L" + i + "# " + minerals[i] + "#l";
			}
			cm.sendSimple(selStr);
			equip = false;
		}
		else if (selectedType == 1){ //jewel refine
			var selStr = "Which jewel would you like to refine?#b";
			var jewels = new Array ("Garnet","Amethyst","Aquamarine","Emerald","Opal","Sapphire","Topaz","Diamond","Black Crystal");
			for (var i = 0; i < jewels.length; i++){
				selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
			}
			cm.sendSimple(selStr);
			equip = false;
		}
		else if (selectedType == 2){ //Crystal refine
			var selStr = "A crystal? That's a rare item indeed. Don't worry, I can refine it just as well as others. Which crystal would you like to refine? #b";
			var crystals = new Array("Power Crystal","Wisdom Crystal","DEX Crystal","LUK Crystal");
			for (var i = 0; i < crystals.length; i++){
				selStr += "\r\n#L" + i + "# " + crystals[i] + "#l";
			}
			cm.sendSimple(selStr);
			equip = false;
		}
	}
	else if (status == 3 && mode == 1) {
		selectedItem = selection;
		if (equip)
		{
			selectedItem = selection;
			qty = 1;
		}
		else
			qty = selection;
			
		
		if(qty <= 0){
			cm.dispose();
			return;
		}
		
		if (selectedType == 0){ //mineral refine
			var itemSet = new Array(4011000,4011001,4011002,4011003,4011004,4011005,4011006,4011008);
			var matSet = new Array(4010000,4010001,4010002,4010003,4010004,4010005,4010006,4010007);
			var matQtySet = new Array(10,10,10,10,10,10,10,10);
			var costSet = new Array(270,270,270,450,450,450,720,270);
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			cost = costSet[selectedItem];
		}
		else if (selectedType == 1){ //jewel refine
			var itemSet = new Array(4021000,4021001,4021002,4021003,4021004,4021005,4021006,4021007,4021008);
			var matSet = new Array(4020000,4020001,4020002,4020003,4020004,4020005,4020006,4020007,4020008);
			var matQtySet = new Array(10,10,10,10,10,10,10,10,10);
			var costSet = new Array (450,450,450,450,450,450,450,900,2700);
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			cost = costSet[selectedItem];
		}
		else if (selectedType == 2){ //Crystal refine
			var itemSet = new Array(4005000,4005001,4005002,4005003);
			var matSet = new Array(4004000,4004001,4004002,4004003);
			var matQtySet = new Array(10,10,10,10);
			var costSet = new Array (4500,4500,4500,4500);
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			cost = costSet[selectedItem];
		}			
		var prompt = "To make #t" + item + "#s, I'll need the following materials. How many do you want to make?";
		
		if (mats instanceof Array){
			for(var i = 0; i < mats.length; i++){
				prompt += "\r\n#i"+mats[i]+"# " + matQty[i] * qty + " #t" + mats[i] + "#";
			}
		}
		else {
			prompt += "\r\n#i"+mats+"# " + matQty * qty + " #t" + mats + "#";
		}
		
		if (cost > 0)
			prompt += "\r\n#i4031138# " + cost * qty + " meso";
			
		cm.sendGetNumber(prompt,1,1,100)
	}
	else if (status == 4 && mode == 1) {
		var complete = true;
		
		if (cm.getMeso() < cost * qty)
			{
				cm.sendOk("I'm afraid you cannot afford my services.")
			}
			else
			{
				if (mats instanceof Array) {
					for(var i = 0; complete && i < mats.length; i++)
					{
						if (matQty[i] * qty == 1)	{
							if (!cm.haveItem(mats[i]))
							{
								complete = false;
							}
						}
						else {
							var count = 0;
							var iter = cm.getChar().getInventory(MapleInventoryType.ETC).listById(mats[i]).iterator();
							while (iter.hasNext()) {
								count += iter.next().getQuantity();
							}
							if (count < matQty[i] * qty)
								complete = false;
						}					
					}
				}
				else {
					var count = 0;
					var iter = cm.getInventory(4).listById(mats).iterator();
					while (iter.hasNext()) {
						count += iter.next().getQuantity();
					}
					if (count < matQty * qty)
						complete = false;
				}
			}
			
			if (!complete) 
				cm.sendNext("Please check and see if you have all the necessary items with you. If so, then please check your etc. inventory and see if.");
			else {
				if (mats instanceof Array) {
					for (var i = 0; i < mats.length; i++){
						cm.gainItem(mats[i], -matQty[i] * qty);
					}
				}
				else
					cm.gainItem(mats, -matQty * qty);
					
				cm.gainMeso(-cost * qty);
				cm.gainItem(item,qty);
				cm.sendOk("There, finished. What do you think, a piece of art, isn't it? Well, if you need anything else, you nkow where to find me.");
			}
		cm.dispose();
	}
	}
}