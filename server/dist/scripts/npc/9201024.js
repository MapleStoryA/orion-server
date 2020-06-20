var status = -1;
var belt = [1132000, 1132001, 1132002, 1132003, 1132004, 1132005, 1132006, 1132007, 1132008, 1132009];
var bandana = [1002391, 1002392, 1002393, 1002394, 1002395, 1002515, 1002516, 1002517, 1002518, 1002553];
var watklist = [7, 33, 55, 69, 107, 182, 229, 300, 421, 500];
var coconut = 4000465;
var storedsel;

function start() {
	cm.sendSimple("#e#bWelcome to the tier item shop. We currently have 2 tier items at the moment. We will have more with time. Please select a category#k~\r\n\r\n#L100#I would like to see belt tier items#l\r\n#L101#I would like you see bandana tier items#l\r\n\r\n#L102#What is a tier item and how does it work?#l")
}

function action(m,t,s) {
	if (m != -1) {
		status++;
	} else {
		cm.sendOk("#eSee you later!");
		cm.dispose();
	}
	if (status == 0) {
		if (s == 100) {
			var t1 = "Please, select the item you would like to obtain and i will then show you the requirement.\r\n\r\n";
			for (i = 0; i < belt.length; i++) {
				t1 += "#L"+i+"#"+i+"#v"+belt[i]+"# - "+watklist[i]+" watk\r\n";
			}
			cm.sendSimple(t1);	
		} else if (s == 101) {
			var t2 = "Please, select the item you would like to obtain and i will then show you the requirement.\r\n\r\n";
			var m = 10;
			for (i = 0; i <  bandana.length; i++) {
				t2 += "#L"+m+"#"+m+"#v"+bandana[i]+"# - "+watklist[i]+" watk\r\n";
				m++;
			}
			cm.sendSimple(t2);
		} else if (s == 102){
			cm.sendOk("#eTier items are items that require you to hunt alot of etc's to obtain but once you do get the, your damage output will be much better since those items are imbued with really good stats, especially Weapon Attack. \r\n\r\nOn top of the weapon attack, your equip will also have random stats in one of the 4 different possible stats (STR, INT, DEX, LUK). When you buy a tier item, you need to have the previous tier item of that category to afford it.")
		}
	} else if (status == 1) {
		t = "#eYou have selected the following item: #v"+belt[s]+"#.\r\n#bHere are the requirement you need to buy this item. #r#n\r\n\r\n";
		if (s == 0) {
			t +=+ "#vcoconut# x100\r\n#v4000052# x50\r\n#v4000032# x120\r\n\r\nAre you sure you want to buy this item?";
			cm.sendYesNo(t);
		} else if (s == 1) {
			t += "#vcoconut# x250\r\n#v4000052# x300\r\n#v4000069# x150\r\n#v4000037# x350\r\nAre you sure you want to buy this item?";
			cm.sendYesNo(t);
		} else if (s == 2) {
			cm.sendOk("soon");
			cm.dispose();
		}
	} else if (status == 2) {
		cm.dispose();
	}
}