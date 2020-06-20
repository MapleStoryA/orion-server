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
			cm.sendSimple("#eHello, Agent #bKitty#k here. #fUI/GuildBBS.img/GuildBBS/Emoticon/Basic/2#\r\nHave you gathered any #rAgent Point#k? \r\n\r\n#L0#I want to trade my #rpoints#k\r\n#L1#How many #rAgent Points#k do i have?\r\n#L2#How can i get #rAgent Points#k?#l");
	} else if (status == 1) {
		if (selection == 0) {
			cm.sendSimple("#e#dNOTE: NO REFUND. SO MAKE SURE YOU HAVE SPACE IN YOUR INVENTORY!!! \r\n\r\n #bEvery equip give 20 WATK. They are non nx, so don't worry.#r \r\n\r\n#L0##v1102174# 45 Agent Point \r\n#L1##v1002800# 25 Agent Points \r\n#L2##v1032058# 35 Agent Points \r\n#L3##v1082245# 40 Agent Points \r\n#L4##v1052167# 100 Agent Points \r\n#L5##v1072368# 40 Agent Points \r\n#L6##v1322013# 90 Agent Points");
	} else if(selection == 1) {
			cm.sendOk("#eYou currently have #r"+cm.getPlayer().getAgentPoint()+" #kagents points.")
			cm.dispose();
	} else if(selection == 2) {
			cm.sendOk("#eYou can get Agent Point by doing various action in MapleStory such as getting a MSI or trading an item with Dargoth. Just play the Game normally and at some point you'll end up getting enough :)");
			cm.dispose();
			}
		} else if (status == 2) {
			if (selection == 0) {
				if (cm.getPlayer().getAgentPoint() >= 45){
						cm.gainAgentPoints(-45);
						cm.gainItem(1102174, 1);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1102174, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
                        cm.gainAgentPoints(100);
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();
						}
		   } else if (selection == 1) {
				if (cm.getAgentPoints() >= 25){
						cm.gainAgentPoints(-25);
						cm.gainItem(1002800);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1002800, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();
						}
			} else if (selection == 2) {
				if (cm.getAgentPoints() >= 35){
						cm.gainAgentPoints(-35);
						cm.gainItem(1032058);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032058, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();
						}
			} else if (selection == 3) {
				if (cm.getAgentPoints() >= 40){
						cm.gainAgentPoints(-40);
						cm.gainItem(1082245);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1082245, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();	
						}
			} else if (selection == 4) {
				if (cm.getAgentPoints() >= 100){
						cm.gainAgentPoints(-100);
						cm.gainItem(1052167);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1052167, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();
						}
			} else if (selection == 5) {
				if (cm.getAgentPoints() >= 40){
						cm.gainAgentPoints(-40);
						cm.gainItem(1072368);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1072368, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();
						}
			} else if (selection == 6) {
				if (cm.getAgentPoints() >= 90){
						cm.gainAgentPoints(-90);
						cm.gainItem(1322013);
						Packages.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1322013, "watk", 20);
						cm.reloadChar();
						cm.sendOk("#eTransaction Successful!");
						cm.dispose();
					}else{
						cm.sendOk("#eYou do not have enough points for this! meowzor~~~");
						cm.dispose();
					}
				}
			}
		}
	}