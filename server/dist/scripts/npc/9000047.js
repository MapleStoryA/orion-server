function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
	cm.sendSimple("#eHello, i am in charge of the Fixed MSI! To get a Fixed MSI you need 32767 in every stats along with the required ETC for each Fixed MSI. \r\n\r\n #L0#Wolf Set#l \r\n\r\n\r\nMore Set will be added in the Future! Suggestion are appreciated");
	} else if(status == 1) {
		if (selection == 0) {
			cm.sendSimple("#e                                     .+*#rWOLF SET#k*+. \r\n\r\n#b#L0##v1002923#Treacherous Wolf Hat#l\r\n\r\n#v4000021# x20\r\n#v4000051# x160\r\n#v4000465# x180\r\n\r\n#L1##v1052194# Caveman Outfit#l\r\n\r\n#v4000440# x350\r\n#v4000021# x100\r\n#v4000465# x300\r\n\r\n#L2##v1072233# Bear Shoes#l\r\n\r\n#v4000021# x200\r\n#v4000283# x50\r\n#v4000465# x150");
			}
		} else if(status == 2) {
			if (selection == 0) {
				if (cm.getPlayer().getStr() > 32766 && cm.getPlayer().getDex() > 32766 && cm.getPlayer().getInt() > 32766 && cm.getPlayer().getLuk() > 32766 && cm.haveItem(4000021, 20) && cm.haveItem(4000051, 160) && cm.haveItem(4000465, 180)) {
					cm.getPlayer().setStr(4);
					cm.getPlayer().setDex(4);
					cm.getPlayer().setLuk(4);
					cm.getPlayer().setInt(4);
					cm.gainItem(4000021, -20);  //leather
					cm.gainItem(4000051, -160); //Hector Tail
					cm.gainItem(4000465, -180); //Coconut
					cm.gainItem(1002923); //FMSI
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1002923, "str", 32767); 
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1002923, "dex", 32767);
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1002923, "int", 32767);
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1002923, "luk", 32767);
					cm.reloadChar();
					cm.sendOk("#eGood job on your #rTreacherous Wolf Hat!");
					cm.serverNotice("[Pinnochio] Congratz to " + cm.getChar().getName() + " on getting his/hers Treacherous Wolf Hat!");
				}else{
					cm.sendOk("You do not meet one of the requirement.");
					cm.dispose();
					}
			}else if (selection == 1) {
			 if (cm.getPlayer().getStr() > 32766 && cm.getPlayer().getDex() > 32766 && cm.getPlayer().getInt() > 32766 && cm.getPlayer().getLuk() > 32766 && cm.haveItem(4000440, 350) && cm.haveItem(4000021, 100) && cm.haveItem(4000465, 300)) {
					cm.getPlayer().setStr(4);
					cm.getPlayer().setDex(4);
					cm.getPlayer().setLuk(4);
					cm.getPlayer().setInt(4);
					cm.gainItem(4000440, -350); //though leather
					cm.gainItem(4000021, -100); //leather
					cm.gainItem(4000465, -300); //coconut
					cm.gainItem(1052194); //FMSI
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1052194, "str", 32767); 
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1052194, "dex", 32767);
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1052194, "int", 32767);
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1052194, "luk", 32767);
					cm.reloadChar();
					cm.sendOk("#eGood job on your #rCaveman Outfit");
					cm.serverNotice("[Pinnochio] Congratz to " + cm.getChar().getName() + " on getting his/hers Caveman outfit!");
				}else{
					cm.sendOk("You do not meet one of the requirement.");
					cm.dispose();
					}
			}else if (selection == 2) {
			 if (cm.getPlayer().getStr() > 32766 && cm.getPlayer().getDex() > 32766 && cm.getPlayer().getInt() > 32766 && cm.getPlayer().getLuk() > 32766 && cm.haveItem(4000021, 200) && cm.haveItem(4000283, 50) && cm.haveItem(4000465, 150)) {
					cm.getPlayer().setStr(4);
					cm.getPlayer().setDex(4);
					cm.getPlayer().setLuk(4);
					cm.getPlayer().setInt(4);
					cm.gainItem(4000021, -200); //leather
					cm.gainItem(4000283, -50);  //bear foot
					cm.gainItem(4000465, -150); //coconut
					cm.gainItem(1072233); //FMSI
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1072233, "str", 32767); 
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1072233, "dex", 32767);
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1072233, "int", 32767);
					Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1072233, "luk", 32767);
					cm.reloadChar();
					cm.sendOk("#eGood job on your #rBear Shoes");
					cm.serverNotice("[Pinnochio] Congratz to " + cm.getChar().getName() + " on getting his/hers Bear Shoes");
				}else{
					cm.sendOk("You do not meet one of the requirement.");
					cm.dispose();
				}
			}
		}
	}
}